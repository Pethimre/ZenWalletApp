package com.aestroon.common.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.aestroon.common.data.TRANSACTIONS_TABLE_NAME
import com.aestroon.common.data.dao.TransactionDao
import com.aestroon.common.data.database.AppDatabase
import com.aestroon.common.data.entity.TransactionEntity
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.serializable.Transaction
import com.aestroon.common.utilities.network.ConnectivityObserver
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface TransactionRepository {
    fun getTransactionsForUser(userId: String): Flow<List<TransactionEntity>>
    fun getTransactionsForWallet(walletId: String): Flow<List<TransactionEntity>>
    fun getPaginatedTransactionsForWallet(walletId: String, limit: Int, offset: Int): Flow<List<TransactionEntity>>
    suspend fun addTransaction(transaction: TransactionEntity): Result<Unit>
    suspend fun updateTransaction(transaction: TransactionEntity): Result<Unit>
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

    override fun getTransactionsForUser(userId: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForUser(userId)
    }

    override fun getTransactionsForWallet(walletId: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForWallet(walletId)
    }

    override fun getPaginatedTransactionsForWallet(walletId: String, limit: Int, offset: Int): Flow<List<TransactionEntity>> {
        return transactionDao.getPaginatedTransactionsForWallet(walletId, limit, offset)
    }

    override suspend fun addTransaction(transaction: TransactionEntity): Result<Unit> = runCatching {
        db.withTransaction {
            updateWalletBalancesForTransaction(transaction, isReverting = false)
            transactionDao.insertTransaction(transaction.copy(isSynced = false))
        }
        syncTransactions(transaction.userId)
    }

    override suspend fun updateTransaction(transaction: TransactionEntity): Result<Unit> = runCatching {
        db.withTransaction {
            // Note: This simple update doesn't revert old wallet balances.
            // A more robust implementation would be needed for editing amounts/types.
            transactionDao.updateTransaction(transaction.copy(isSynced = false))
        }
        syncTransactions(transaction.userId)
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
            Log.d("TransactionSync", "Skipping sync: Network not available.")
            return@runCatching
        }

        try {
            Log.d("TransactionSync", "Starting transaction sync for user: $userId")

            // Upload unsynced transactions
            val unsynced = transactionDao.getUnsyncedTransactions().first()
            if (unsynced.isNotEmpty()) {
                Log.d("TransactionSync", "Found ${unsynced.size} unsynced transactions to upload.")
                val networkTransactions = unsynced.map {
                    Transaction(
                        id = it.id, amount = it.amount, currency = it.currency, name = it.name,
                        description = it.description, created_at = it.date, user_id = it.userId,
                        wallet_id = it.walletId, category_id = it.categoryId,
                        transaction_type = it.transactionType.name, to_wallet_id = it.toWalletId
                    )
                }
                postgrest.from(TRANSACTIONS_TABLE_NAME).upsert(networkTransactions)
                unsynced.forEach { transactionDao.markTransactionAsSynced(it.id) }
                Log.d("TransactionSync", "Upload successful.")
            } else {
                Log.d("TransactionSync", "No local transactions to upload.")
            }

            // Download remote transactions
            Log.d("TransactionSync", "Fetching remote transactions...")
            val remote = postgrest.from(TRANSACTIONS_TABLE_NAME).select {
                filter { eq("user_id", userId) }
            }.decodeList<Transaction>()
            Log.d("TransactionSync", "Successfully fetched and decoded ${remote.size} remote transactions.")

            db.withTransaction {
                remote.forEach {
                    val entity = TransactionEntity(
                        id = it.id, amount = it.amount, currency = it.currency, name = it.name,
                        description = it.description, date = it.created_at, userId = it.user_id,
                        walletId = it.wallet_id, categoryId = it.category_id,
                        transactionType = TransactionType.valueOf(it.transaction_type),
                        toWalletId = it.to_wallet_id, isSynced = true
                    )
                    transactionDao.insertTransaction(entity)
                }
            }
            Log.d("TransactionSync", "Local database updated with remote transactions.")

        } catch (e: Exception) {
            // This will catch any error during the sync process (networking, JSON parsing, etc.)
            Log.e("TransactionSync", "TRANSACTION SYNC FAILED", e)
            throw e // Re-throw to let the caller know it failed
        }
    }

    private suspend fun updateWalletBalancesForTransaction(transaction: TransactionEntity, isReverting: Boolean) {
        val fromWallet = walletRepository.getWalletById(transaction.walletId).first() ?: return
        val sign = if (isReverting) -1 else 1

        when (transaction.transactionType) {
            TransactionType.INCOME -> {
                val updatedWallet = fromWallet.copy(balance = fromWallet.balance + (transaction.amount * sign))
                walletRepository.updateWallet(updatedWallet).getOrThrow()
            }
            TransactionType.EXPENSE -> {
                val updatedWallet = fromWallet.copy(balance = fromWallet.balance - (transaction.amount * sign))
                walletRepository.updateWallet(updatedWallet).getOrThrow()
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

    private suspend fun convertCurrency(amount: Long, fromCurrency: String, toCurrency: String): Result<Long> {
        if (fromCurrency == toCurrency) return Result.success(amount)
        return currencyRepository.getExchangeRates(fromCurrency).map { rates ->
            val conversionRate = rates.conversion_rates[toCurrency]
                ?: throw IllegalStateException("Exchange rate for $toCurrency not found.")
            (amount * conversionRate).toLong()
        }
    }
}
