package com.aestroon.common.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.aestroon.common.data.dao.TransactionDao
import com.aestroon.common.data.database.AppDatabase
import com.aestroon.common.data.entity.TransactionEntity
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.serializable.Transaction
import com.aestroon.common.data.toNetworkModel
import com.aestroon.common.utilities.network.ConnectivityObserver
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface TransactionRepository {
    fun getTransactionsForUser(userId: String): Flow<List<TransactionEntity>>
    fun getTransactionsForWallet(walletId: String): Flow<List<TransactionEntity>>
    fun getPaginatedTransactionsForWallet(walletId: String, limit: Int, offset: Int): Flow<List<TransactionEntity>>
    suspend fun addTransaction(transaction: TransactionEntity): Result<Unit>
    suspend fun updateTransaction(originalTransaction: TransactionEntity, updatedTransaction: TransactionEntity): Result<Unit>
    suspend fun deleteTransaction(transaction: TransactionEntity): Result<Unit>
    suspend fun syncTransactions(userId: String): Result<Unit>
}

class TransactionRepositoryImpl(
    private val db: AppDatabase,
    private val postgrest: Postgrest,
    private val transactionDao: TransactionDao,
    private val walletRepository: WalletRepository,
    private val currencyRepository: CurrencyRepository,
    private val connectivityObserver: ConnectivityObserver
) : TransactionRepository {

    private val TRANSACTIONS_TABLE_NAME = "Transactions"

    override fun getTransactionsForUser(userId: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForUser(userId)
    }

    override fun getTransactionsForWallet(walletId: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForWallet(walletId)
    }

    override fun getPaginatedTransactionsForWallet(
        walletId: String, limit: Int, offset: Int
    ): Flow<List<TransactionEntity>> {
        return transactionDao.getPaginatedTransactionsForWallet(walletId, limit, offset)
    }

    override suspend fun addTransaction(transaction: TransactionEntity): Result<Unit> = runCatching {
        db.withTransaction {
            updateWalletBalancesForTransaction(transaction, isReverting = false)
            transactionDao.insertTransaction(transaction.copy(isSynced = false))
        }
        syncTransactions(transaction.userId)
    }

    override suspend fun updateTransaction(
        originalTransaction: TransactionEntity, updatedTransaction: TransactionEntity
    ): Result<Unit> = runCatching {
        db.withTransaction {
            updateWalletBalancesForTransaction(originalTransaction, isReverting = true)
            updateWalletBalancesForTransaction(updatedTransaction, isReverting = false)
            transactionDao.updateTransaction(updatedTransaction.copy(isSynced = false))
        }
        syncTransactions(updatedTransaction.userId)
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity): Result<Unit> = runCatching {
        db.withTransaction {
            updateWalletBalancesForTransaction(transaction, isReverting = true)
            transactionDao.deleteTransactionById(transaction.id)
        }
        if (connectivityObserver.observe().first() == ConnectivityObserver.Status.Available) {
            postgrest.from(TRANSACTIONS_TABLE_NAME).delete {
                filter { eq("id", transaction.id) }
            }
        }
    }

    override suspend fun syncTransactions(userId: String): Result<Unit> = runCatching {
        if (connectivityObserver.observe().first() != ConnectivityObserver.Status.Available) {
            return@runCatching
        }

        // Sync local to remote using the central mapper
        transactionDao.getUnsyncedTransactions().first().takeIf { it.isNotEmpty() }?.let { unsynced ->
            postgrest.from(TRANSACTIONS_TABLE_NAME).upsert(unsynced.map(TransactionEntity::toNetworkModel))
            unsynced.forEach { transactionDao.markTransactionAsSynced(it.id) }
        }

        // Sync remote to local using the central mapper
        val remoteTransactions = postgrest.from(TRANSACTIONS_TABLE_NAME).select {
            filter { eq("user_id", userId) }
        }.decodeList<Transaction>()

        db.withTransaction {
            remoteTransactions.forEach { remoteTransaction ->
                transactionDao.insertTransaction(remoteTransaction.toEntity())
            }
        }
        Unit // Explicitly return Unit
    }.onFailure {
        Log.e("TransactionSync", "TRANSACTION SYNC FAILED", it)
    }

    private suspend fun updateWalletBalancesForTransaction(
        transaction: TransactionEntity, isReverting: Boolean
    ) {
        val fromWallet = walletRepository.getWalletById(transaction.walletId).first() ?: return
        val sign = if (isReverting) -1 else 1

        when (transaction.transactionType) {
            TransactionType.INCOME -> {
                val newBalance = fromWallet.balance + (transaction.amount * sign)
                walletRepository.updateWallet(fromWallet.copy(balance = newBalance)).getOrThrow()
            }
            TransactionType.EXPENSE -> {
                val newBalance = fromWallet.balance - (transaction.amount * sign)
                walletRepository.updateWallet(fromWallet.copy(balance = newBalance)).getOrThrow()
            }
            TransactionType.TRANSFER -> {
                val toWalletId = transaction.toWalletId ?: return
                val toWallet = walletRepository.getWalletById(toWalletId).first() ?: return

                val amountToTransfer = if (fromWallet.currency != toWallet.currency) {
                    convertCurrency(transaction.amount, fromWallet.currency, toWallet.currency).getOrThrow()
                } else {
                    transaction.amount
                }

                val updatedFromWallet = fromWallet.copy(balance = fromWallet.balance - (transaction.amount * sign))
                val updatedToWallet = toWallet.copy(balance = toWallet.balance + (amountToTransfer * sign))

                walletRepository.updateWallet(updatedFromWallet).getOrThrow()
                walletRepository.updateWallet(updatedToWallet).getOrThrow()
            }
        }
    }

    fun Transaction.toEntity() = TransactionEntity(
        id = this.id,
        amount = this.amount,
        currency = this.currency,
        name = this.name,
        description = this.description,
        date = this.date,
        userId = this.userId,
        walletId = this.walletId,
        categoryId = this.categoryId,
        transactionType = try {
            TransactionType.valueOf(this.transactionType)
        } catch (e: Exception) {
            TransactionType.EXPENSE
        },
        toWalletId = this.toWalletId,
        isSynced = true
    )

    private suspend fun convertCurrency(amount: Long, fromCurrency: String, toCurrency: String): Result<Long> {
        if (fromCurrency == toCurrency) return Result.success(amount)
        return currencyRepository.getExchangeRates(fromCurrency).map { rates ->
            val conversionRate = rates.conversion_rates[toCurrency]
                ?: throw IllegalStateException("Exchange rate for $toCurrency not found.")
            (amount * conversionRate).toLong()
        }
    }
}
