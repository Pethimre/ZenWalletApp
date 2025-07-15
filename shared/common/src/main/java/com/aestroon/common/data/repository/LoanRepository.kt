package com.aestroon.common.data.repository

import android.util.Log
import com.aestroon.common.data.dao.LoanDao
import com.aestroon.common.data.dao.LoanEntryDao
import com.aestroon.common.data.dao.TransactionDao
import com.aestroon.common.data.dao.WalletDao
import com.aestroon.common.data.entity.LoanEntity
import com.aestroon.common.data.entity.LoanEntryEntity
import com.aestroon.common.data.entity.LoanType
import com.aestroon.common.data.entity.TransactionEntity
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.data.serializable.Loan
import com.aestroon.common.data.serializable.LoanEntry
import com.aestroon.common.utilities.network.ConnectivityObserver
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date

interface LoanRepository {
    fun getLoans(userId: String): Flow<List<LoanEntity>>
    fun getLoanById(loanId: String): Flow<LoanEntity?>
    fun getLoanEntries(loanId: String): Flow<List<LoanEntryEntity>>

    suspend fun addOrUpdateLoan(loan: LoanEntity): Result<Unit>
    suspend fun deleteLoan(loan: LoanEntity): Result<Unit>

    suspend fun addLoanEntry(
        loan: LoanEntity,
        wallet: WalletEntity,
        amount: Long,
        date: Date,
        note: String?,
        isInterest: Boolean,
        createMainTransaction: Boolean
    ): Result<Unit>

    suspend fun deleteLoanEntry(entry: LoanEntryEntity): Result<Unit>

    suspend fun syncLoans(userId: String): Result<Unit>
    suspend fun fetchRemoteLoans(userId: String): Result<Unit>
}

class LoanRepositoryImpl(
    private val postgrest: Postgrest,
    private val loanDao: LoanDao,
    private val loanEntryDao: LoanEntryDao,
    private val transactionDao: TransactionDao,
    private val walletDao: WalletDao,
    private val authRepository: AuthRepository,
    private val connectivityObserver: ConnectivityObserver
) : LoanRepository {

    override fun getLoans(userId: String): Flow<List<LoanEntity>> = loanDao.getLoansForUser(userId)
    override fun getLoanById(loanId: String): Flow<LoanEntity?> = loanDao.getLoanById(loanId)
    override fun getLoanEntries(loanId: String): Flow<List<LoanEntryEntity>> = loanEntryDao.getEntriesForLoan(loanId)

    override suspend fun addOrUpdateLoan(loan: LoanEntity): Result<Unit> = runCatching {
        loanDao.insertLoan(loan.copy(isSynced = false))
        syncLoans(loan.userId)
    }

    override suspend fun deleteLoan(loan: LoanEntity): Result<Unit> = runCatching {
        loanDao.deleteLoanById(loan.id)
        if (connectivityObserver.observe().first() == ConnectivityObserver.Status.Available) {
            postgrest.from("Loans").delete { filter { eq("id", loan.id) } }
        }
    }

    override suspend fun addLoanEntry(
        loan: LoanEntity,
        wallet: WalletEntity,
        amount: Long,
        date: Date,
        note: String?,
        isInterest: Boolean,
        createMainTransaction: Boolean
    ): Result<Unit> = runCatching {
        val userId = authRepository.userIdFlow.first() ?: throw IllegalStateException("User not logged in")
        var transactionId: String? = null

        val transactionType = if (loan.type == LoanType.LENT) TransactionType.INCOME else TransactionType.EXPENSE

        val updatedLoan = loan.copy(remaining = loan.remaining - amount, isSynced = false)
        loanDao.updateLoan(updatedLoan)

        if (createMainTransaction) {
            val transactionName = if (loan.type == LoanType.LENT) "Repayment from ${loan.name}" else "Payment to ${loan.name}"
            val transaction = TransactionEntity(
                amount = amount,
                currency = wallet.currency,
                name = transactionName,
                description = note,
                date = date,
                userId = userId,
                walletId = wallet.id,
                categoryId = null,
                transactionType = transactionType,
                toWalletId = null
            )
            transactionDao.insertTransaction(transaction.copy(isSynced = false))
            transactionId = transaction.id

            val newBalance = if (transactionType == TransactionType.INCOME) wallet.balance + amount else wallet.balance - amount
            walletDao.insertWallet(wallet.copy(balance = newBalance, isSynced = false))
        }

        val entry = LoanEntryEntity(
            loanId = loan.id,
            transactionId = transactionId,
            userId = userId,
            walletId = wallet.id,
            amount = amount,
            date = date,
            note = note,
            isInterest = isInterest,
            isSynced = false
        )
        loanEntryDao.insertEntry(entry)
        syncLoans(userId)
    }

    override suspend fun deleteLoanEntry(entry: LoanEntryEntity): Result<Unit> = runCatching {
        val loan = loanDao.getLoanById(entry.loanId).first() ?: return@runCatching
        val wallet = entry.walletId?.let { walletDao.getWalletById(it).first() }

        loanDao.updateLoan(loan.copy(remaining = loan.remaining + entry.amount, isSynced = false))

        if (entry.transactionId != null && wallet != null) {
            transactionDao.deleteTransactionById(entry.transactionId)
            val transactionType = if (loan.type == LoanType.LENT) TransactionType.INCOME else TransactionType.EXPENSE
            val revertedBalance = if (transactionType == TransactionType.INCOME) wallet.balance - entry.amount else wallet.balance + entry.amount
            walletDao.insertWallet(wallet.copy(balance = revertedBalance, isSynced = false))
        }

        loanEntryDao.deleteEntryById(entry.id)

        if (connectivityObserver.observe().first() == ConnectivityObserver.Status.Available) {
            postgrest.from("Loan_entry").delete { filter { eq("id", entry.id) } }
            syncLoans(entry.userId)
            wallet?.let { WalletRepositoryImpl(postgrest, walletDao, connectivityObserver).syncPendingWallets() }
        }
    }

    override suspend fun fetchRemoteLoans(userId: String): Result<Unit> = runCatching {
        if (connectivityObserver.observe().first() != ConnectivityObserver.Status.Available) return@runCatching

        Log.d("LoanRepo", "Fetching remote loans for user $userId")
        val remoteLoans = postgrest.from("Loans").select().decodeList<Loan>()
        remoteLoans.forEach { loan ->
            val entity = loan.toEntity()
            loanDao.insertLoan(entity.copy(isSynced = true))
        }

        val remoteEntries = postgrest.from("Loan_entry").select().decodeList<LoanEntry>()
        remoteEntries.forEach { entry ->
            loanEntryDao.insertEntry(entry.toEntity().copy(isSynced = true))
        }
    }

    override suspend fun syncLoans(userId: String): Result<Unit> = runCatching {
        if (connectivityObserver.observe().first() != ConnectivityObserver.Status.Available) return@runCatching

        val unsyncedLoans = loanDao.getUnsyncedLoans().first()
        if (unsyncedLoans.isNotEmpty()) {
            val networkLoans = unsyncedLoans.map { it.toNetworkModel() }
            postgrest.from("Loans").upsert(networkLoans)
            unsyncedLoans.forEach { loanDao.markLoanAsSynced(it.id) }
        }

        val unsyncedEntries = loanEntryDao.getUnsyncedEntries().first()
        if (unsyncedEntries.isNotEmpty()) {
            val networkEntries = unsyncedEntries.map { it.toNetworkModel() }
            postgrest.from("Loan_entry").upsert(networkEntries)
            unsyncedEntries.forEach { loanEntryDao.markEntryAsSynced(it.id) }
        }
    }

    private fun sanitizeColor(colorString: String?): String {
        val defaultColor = "#6200EE"
        if (colorString.isNullOrBlank()) return defaultColor
        return try {
            android.graphics.Color.parseColor(colorString)
            colorString
        } catch (e: IllegalArgumentException) {
            // The color string is invalid, return the default.
            defaultColor
        }
    }

    private fun LoanEntity.toNetworkModel() = Loan(id, name, description, principal, remaining, iconName, color, type.name, userId)

    private fun Loan.toEntity() = LoanEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        principal = this.principal,
        remaining = this.remaining,
        color = sanitizeColor(this.color),
        iconName = this.icon_name,
        type = try { LoanType.valueOf(this.type) } catch (e: Exception) { LoanType.LENT },
        userId = this.user_id
    )

    private fun LoanEntryEntity.toNetworkModel() = LoanEntry(id, loanId, transactionId, userId, walletId ?: "", amount, date, note, isInterest)
    private fun LoanEntry.toEntity() = LoanEntryEntity(id, loan_id, transaction_id, user_id, wallet_id, amount, date, note, is_interest)
}
