package com.aestroon.common.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.entity.CategoryEntity
import com.aestroon.common.data.entity.TransactionEntity
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.repository.CategoryRepository
import com.aestroon.common.data.repository.CurrencyConversionRepository
import com.aestroon.common.data.repository.TransactionRepository
import com.aestroon.common.data.repository.WalletRepository
import com.aestroon.common.presentation.TransactionUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModel(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository,
    val currencyConversionRepository: CurrencyConversionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionUiState>(TransactionUiState.Idle)
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    val baseCurrency: StateFlow<String> = currencyConversionRepository.baseCurrency
    val exchangeRates: StateFlow<Map<String, Double>?> = currencyConversionRepository.exchangeRates

    private val _transactionToEdit = MutableStateFlow<TransactionEntity?>(null)
    val transactionToEdit = _transactionToEdit.asStateFlow()

    val transactions: StateFlow<List<TransactionEntity>> = authRepository.userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            transactionRepository.getTransactionsForUser(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wallets: StateFlow<List<WalletEntity>> = authRepository.userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            walletRepository.getWalletsForUser(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = authRepository.userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            categoryRepository.getCategoriesForUser(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesMap: StateFlow<Map<String, CategoryEntity>> = categories.map { list ->
        list.associateBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val currentMonthTransactions = combine(
        transactions,
        baseCurrency,
        exchangeRates
    ) { transactions, base, rates ->
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        transactions
            .filter { transaction ->
                calendar.time = transaction.date
                calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear
            }
            .map { transaction ->
                val amount = transaction.amount / 100.0
                val convertedAmount = if (transaction.currency == base || rates == null) {
                    amount
                } else {
                    val rate = rates[transaction.currency] ?: 0.0
                    if (rate != 0.0) amount / rate else 0.0
                }
                transaction.copy(amount = (convertedAmount * 100).toLong())
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentMonthIncome: StateFlow<Double> = currentMonthTransactions.map { transactions ->
        transactions
            .filter { it.transactionType == TransactionType.INCOME }
            .sumOf { it.amount / 100.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentMonthExpense: StateFlow<Double> = currentMonthTransactions.map { transactions ->
        transactions
            .filter { it.transactionType == TransactionType.EXPENSE }
            .sumOf { it.amount / 100.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyProgress: StateFlow<Double> = combine(
        transactions,
        baseCurrency,
        exchangeRates
    ) { transactions, base, rates ->
        if (rates == null) return@combine 0.0

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val monthlyTransactions = transactions.filter {
            calendar.time = it.date
            calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear
        }

        monthlyTransactions.sumOf { transaction ->
            val amount = when (transaction.transactionType) {
                TransactionType.INCOME -> transaction.amount / 100.0
                TransactionType.EXPENSE -> -transaction.amount / 100.0
                TransactionType.TRANSFER -> 0.0
            }

            if (transaction.currency == base) {
                amount
            } else {
                val rate = rates[transaction.currency] ?: 0.0
                if (rate != 0.0) amount / rate else 0.0
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun syncTransactions() {
        viewModelScope.launch {
            authRepository.userIdFlow.firstOrNull()?.let { userId ->
                transactionRepository.syncTransactions(userId)
            }
        }
    }

    fun addTransaction(
        amount: Long, name: String, description: String?, date: Date,
        fromWallet: WalletEntity, category: CategoryEntity?,
        type: TransactionType, toWallet: WalletEntity?
    ) {
        viewModelScope.launch {
            _uiState.value = TransactionUiState.Loading
            val userId = authRepository.userIdFlow.first()
            if (userId == null) {
                _uiState.value = TransactionUiState.Error("User not found")
                return@launch
            }

            if (type == TransactionType.TRANSFER && toWallet == null) {
                _uiState.value = TransactionUiState.Error("Destination wallet is required for a transfer.")
                return@launch
            }

            val transaction = TransactionEntity(
                amount = amount, currency = fromWallet.currency, name = name,
                description = description, date = date, userId = userId,
                walletId = fromWallet.id, categoryId = category?.id,
                transactionType = type, toWalletId = toWallet?.id
            )

            transactionRepository.addTransaction(transaction)
                .onSuccess { _uiState.value = TransactionUiState.Idle }
                .onFailure {
                    _uiState.value = TransactionUiState.Error(it.message ?: "Failed to add transaction")
                }
        }
    }

    fun onEditTransactionClicked(transaction: TransactionEntity) {
        _transactionToEdit.value = transaction
    }

    fun onEditTransactionDialogDismiss() {
        _transactionToEdit.value = null
    }

    fun onEditTransactionConfirm(
        amountStr: String,
        name: String,
        description: String,
        category: CategoryEntity?,
    ) {
        viewModelScope.launch {
            val originalTransaction = _transactionToEdit.value ?: return@launch

            val amountInCents = (amountStr.replace(",", ".").toDoubleOrNull() ?: 0.0).times(100).toLong()

            val updatedTransaction = originalTransaction.copy(
                amount = amountInCents,
                name = name,
                description = description,
                categoryId = category?.id,
                isSynced = false
            )

            transactionRepository.updateTransaction(originalTransaction, updatedTransaction)
            onEditTransactionDialogDismiss()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }
}