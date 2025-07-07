package com.aestroon.common.data.repository

import com.aestroon.common.data.TRANSACTIONS_TABLE_NAME
import com.aestroon.common.data.dao.TransactionDao
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
    suspend fun addTransaction(transaction: TransactionEntity): Result<Unit>
    suspend fun updateTransaction(transaction: TransactionEntity): Result<Unit>
    suspend fun deleteTransaction(transaction: TransactionEntity): Result<Unit>
    suspend fun syncTransactions(userId: String): Result<Unit>
}

class TransactionRepositoryImpl(
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

    override suspend fun addTransaction(transaction: TransactionEntity): Result<Unit> = runCatching {
        updateWalletBalancesForTransaction(transaction, isReverting = false)
        transactionDao.insertTransaction(transaction.copy(isSynced = false))
        syncTransactions(transaction.userId)
    }

    override suspend fun updateTransaction(transaction: TransactionEntity): Result<Unit> {
        return runCatching {
            transactionDao.updateTransaction(transaction.copy(isSynced = false))
            syncTransactions(transaction.userId)
        }
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity): Result<Unit> = runCatching {
        updateWalletBalancesForTransaction(transaction, isReverting = true)
        transactionDao.deleteTransactionById(transaction.id)
        if (connectivityObserver.observe().first() == ConnectivityObserver.Status.Available) {
            postgrest.from(TRANSACTIONS_TABLE_NAME).delete { filter { eq("id", transaction.id) } }
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

    override suspend fun syncTransactions(userId: String): Result<Unit> = runCatching {
        if (connectivityObserver.observe().first() != ConnectivityObserver.Status.Available) {
            return@runCatching
        }

        val unsynced = transactionDao.getUnsyncedTransactions().first()
        if (unsynced.isNotEmpty()) {
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
        }

        val remote = postgrest.from(TRANSACTIONS_TABLE_NAME).select {
            filter { eq("user_id", userId) }
        }.decodeList<Transaction>()

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
}
