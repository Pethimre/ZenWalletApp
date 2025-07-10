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
        plannedPaymentDao.insertPlannedPayment(payment.copy(isSynced = false))
        syncPlannedPayments(payment.userId)
    }

    override suspend fun processPayment(payment: PlannedPaymentEntity): Result<Unit> = runCatching {
        val transaction = TransactionEntity(
            amount = payment.amount,
            currency = payment.currency,
            name = payment.name,
            description = "Planned: ${payment.description.orEmpty()}",
            date = Date(),
            userId = payment.userId,
            walletId = payment.walletId,
            categoryId = payment.categoryId,
            transactionType = payment.transactionType,
            toWalletId = payment.toWalletId,
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
        if (connectivityObserver.observe().first() == ConnectivityObserver.Status.Available) {
            postgrest.from("Planned_payments").delete { filter { eq("id", payment.id) } }
        }
    }

    override suspend fun syncPlannedPayments(userId: String): Result<Unit> = runCatching {
        if (connectivityObserver.observe().first() != ConnectivityObserver.Status.Available) {
            return@runCatching
        }

        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

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
                    category_id = it.categoryId,
                    transaction_type = it.transactionType.name,
                    to_wallet_id = it.toWalletId
                )
            }
            postgrest.from("Planned_payments").upsert(networkPayments)
            unsyncedPayments.forEach { plannedPaymentDao.markPaymentAsSynced(it.id) }
        }

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
                transactionType = TransactionType.valueOf(remotePayment.transaction_type),
                toWalletId = remotePayment.to_wallet_id,
                isSynced = true
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
