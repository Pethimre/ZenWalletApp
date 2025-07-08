package com.aestroon.common.data.repository

import com.aestroon.common.data.dao.PlannedPaymentDao
import com.aestroon.common.data.entity.PlannedPaymentEntity
import com.aestroon.common.data.entity.RecurrenceType
import com.aestroon.common.data.entity.TransactionEntity
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.serializable.PlannedPayment
import com.aestroon.common.utilities.network.ConnectivityObserver
import kotlinx.coroutines.flow.Flow
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

interface PlannedPaymentRepository {
    fun getPlannedPayments(userId: String): Flow<List<PlannedPaymentEntity>>
    suspend fun addOrUpdatePlannedPayment(payment: PlannedPaymentEntity): Result<Unit>
    suspend fun processPayment(payment: PlannedPaymentEntity): Result<Unit>
    suspend fun skipPayment(payment: PlannedPaymentEntity): Result<Unit>
    suspend fun deletePayment(payment: PlannedPaymentEntity): Result<Unit>
    suspend fun syncPlannedPayments(userId: String): Result<Unit>
}

class PlannedPaymentRepositoryImpl(
    private val plannedPaymentDao: PlannedPaymentDao,
    private val transactionRepository: TransactionRepository,
    private val postgrest: Postgrest,
    private val connectivityObserver: ConnectivityObserver
) : PlannedPaymentRepository {

    override fun getPlannedPayments(userId: String): Flow<List<PlannedPaymentEntity>> {
        return plannedPaymentDao.getPlannedPaymentsForUser(userId)
    }

    override suspend fun addOrUpdatePlannedPayment(payment: PlannedPaymentEntity): Result<Unit> = runCatching {
        // Insert locally first, marked as unsynced
        plannedPaymentDao.insertPlannedPayment(payment.copy(isSynced = false))
        // Attempt to sync with remote
        syncPlannedPayments(payment.userId)
    }

    override suspend fun processPayment(payment: PlannedPaymentEntity): Result<Unit> = runCatching {
        val transaction = TransactionEntity(
            amount = payment.amount,
            currency = payment.currency,
            name = payment.name,
            description = "Planned: ${payment.description.orEmpty()}",
            date = Date(), // Use current date for the actual payment
            userId = payment.userId,
            walletId = payment.walletId,
            categoryId = payment.categoryId,
            transactionType = TransactionType.EXPENSE,
            toWalletId = null
        )
        transactionRepository.addTransaction(transaction).getOrThrow()

        if (payment.recurrenceType == RecurrenceType.ONCE) {
            deletePayment(payment)
        } else {
            val nextDueDate = calculateNextDueDate(payment.dueDate, payment.recurrenceType, payment.recurrenceValue)
            val updatedPayment = payment.copy(dueDate = nextDueDate, isSynced = false)
            plannedPaymentDao.updatePlannedPayment(updatedPayment)
        }
        syncPlannedPayments(payment.userId)
    }

    override suspend fun skipPayment(payment: PlannedPaymentEntity): Result<Unit> = runCatching {
        if (payment.recurrenceType == RecurrenceType.ONCE) {
            deletePayment(payment)
        } else {
            val nextDueDate = calculateNextDueDate(payment.dueDate, payment.recurrenceType, payment.recurrenceValue)
            val updatedPayment = payment.copy(dueDate = nextDueDate, isSynced = false)
            plannedPaymentDao.updatePlannedPayment(updatedPayment)
        }
        syncPlannedPayments(payment.userId)
    }

    override suspend fun deletePayment(payment: PlannedPaymentEntity): Result<Unit> = runCatching {
        plannedPaymentDao.deletePlannedPaymentById(payment.id)
        // Attempt to delete from remote if online
        if (connectivityObserver.observe().first() == ConnectivityObserver.Status.Available) {
            postgrest.from("Planned_payments").delete { filter { eq("id", payment.id) } }
        }
    }

    override suspend fun syncPlannedPayments(userId: String): Result<Unit> = runCatching {
        if (connectivityObserver.observe().first() != ConnectivityObserver.Status.Available) {
            return@runCatching // Exit if offline
        }

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())

        // 1. Push local unsynced changes to remote
        val unsyncedPayments = plannedPaymentDao.getUnsyncedPayments().first()
        if (unsyncedPayments.isNotEmpty()) {
            val networkPayments = unsyncedPayments.map {
                PlannedPayment(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    due_date = isoFormat.format(it.dueDate),
                    amount = it.amount,
                    currency = it.currency,
                    recurrence_type = it.recurrenceType.name,
                    recurrence_value = it.recurrenceValue,
                    user_id = it.userId,
                    wallet_id = it.walletId,
                    category_id = it.categoryId
                )
            }
            postgrest.from("Planned_payments").upsert(networkPayments)
            // Mark them as synced locally
            unsyncedPayments.forEach { plannedPaymentDao.markPaymentAsSynced(it.id) }
        }

        // 2. Pull remote changes and update local database
        val remotePayments = postgrest.from("Planned_payments").select {
            filter { eq("user_id", userId) }
        }.decodeList<PlannedPayment>()

        remotePayments.forEach { remotePayment ->
            val entity = PlannedPaymentEntity(
                id = remotePayment.id,
                name = remotePayment.name,
                description = remotePayment.description,
                dueDate = isoFormat.parse(remotePayment.due_date) ?: Date(),
                amount = remotePayment.amount,
                currency = remotePayment.currency,
                recurrenceType = RecurrenceType.valueOf(remotePayment.recurrence_type),
                recurrenceValue = remotePayment.recurrence_value,
                userId = remotePayment.user_id,
                walletId = remotePayment.wallet_id,
                categoryId = remotePayment.category_id,
                isSynced = true // Mark as synced
            )
            plannedPaymentDao.insertPlannedPayment(entity)
        }
    }

    private fun calculateNextDueDate(currentDueDate: Date, type: RecurrenceType, value: Int): Date {
        val calendar = Calendar.getInstance().apply { time = currentDueDate }
        when (type) {
            RecurrenceType.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, value)
            RecurrenceType.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, value)
            RecurrenceType.MONTHLY -> calendar.add(Calendar.MONTH, value)
            RecurrenceType.YEARLY -> calendar.add(Calendar.YEAR, value)
            RecurrenceType.ONCE -> { /* Do nothing */ }
        }
        return calendar.time
    }
}
