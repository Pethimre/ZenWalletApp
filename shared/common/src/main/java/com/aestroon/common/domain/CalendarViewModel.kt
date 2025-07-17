package com.aestroon.common.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.repository.CategoryRepository
import com.aestroon.common.data.repository.CurrencyConversionRepository
import com.aestroon.common.data.repository.PlannedPaymentRepository
import com.aestroon.common.data.repository.TransactionRepository
import com.aestroon.common.data.repository.WalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

enum class UiTransactionType {
    RECORDED, UPCOMING, OVERDUE
}

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val walletsRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val plannedPaymentRepository: PlannedPaymentRepository,
    private val categoryRepository: CategoryRepository,
    val currencyConversionRepository: CurrencyConversionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _wallets = MutableStateFlow<List<WalletEntity>>(emptyList())
    val wallets: StateFlow<List<WalletEntity>> = _wallets.asStateFlow()

    private val _selectedWalletId = MutableStateFlow<String?>(null)

    private val _transactions = MutableStateFlow<List<TransactionUiModel>>(emptyList())
    val transactions: StateFlow<List<TransactionUiModel>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20
    private var canLoadMore = true

    val baseCurrency: StateFlow<String> = currencyConversionRepository.baseCurrency
    val exchangeRates: StateFlow<Map<String, Double>?> = currencyConversionRepository.exchangeRates

    init {
        viewModelScope.launch {
            val userId = authRepository.userIdFlow.firstOrNull() ?: return@launch
            walletsRepository.getWalletsForUser(userId).collect { walletList ->
                _wallets.value = walletList
            }
        }
    }

    fun onWalletSelected(walletId: String) {
        if (_selectedWalletId.value == walletId && !_isLoading.value) return

        _selectedWalletId.value = walletId
        currentPage = 0
        canLoadMore = true
        _transactions.value = emptyList()
        loadTransactions(isInitialLoad = true)
    }

    fun loadMoreTransactions() {
        if (_isLoading.value || _isLoadingMore.value || !canLoadMore) return
        loadTransactions(isInitialLoad = false)
    }

    private fun loadTransactions(isInitialLoad: Boolean) {
        viewModelScope.launch {
            if (isInitialLoad) _isLoading.value = true else _isLoadingMore.value = true

            val walletId = _selectedWalletId.value ?: return@launch
            val userId = authRepository.userIdFlow.firstOrNull() ?: return@launch
            val categories = categoryRepository.getCategoriesForUser(userId).first().associateBy { it.id }

            val plannedUiModels = if (isInitialLoad) {
                plannedPaymentRepository.getPlannedPayments(userId).first()
                    .filter { it.walletId == walletId }
                    .map {
                        val now = Calendar.getInstance()
                        val dueDate = Calendar.getInstance().apply { time = it.dueDate }
                        val type = if (dueDate.before(now)) UiTransactionType.OVERDUE else UiTransactionType.UPCOMING
                        TransactionUiModel(
                            id = it.id,
                            name = it.name,
                            date = it.dueDate,
                            amount = if (type == UiTransactionType.UPCOMING) it.amount / 100.0 else -(it.amount / 100.0),
                            currency = it.currency,
                            type = type,
                            category = it.categoryId?.let { catId -> categories[catId] }
                        )
                    }
            } else {
                emptyList()
            }

            val recordedTransactions = transactionRepository
                .getPaginatedTransactionsForWallet(walletId, pageSize, currentPage * pageSize)
                .first()

            if (recordedTransactions.size < pageSize) {
                canLoadMore = false
            }

            val recordedUiModels = recordedTransactions.map {
                TransactionUiModel(
                    id = it.id,
                    name = it.name,
                    date = it.date,
                    amount = when (it.transactionType) {
                        TransactionType.INCOME -> it.amount / 100.0
                        TransactionType.EXPENSE -> -(it.amount / 100.0)
                        TransactionType.TRANSFER -> 0.0
                    },
                    currency = it.currency,
                    type = UiTransactionType.RECORDED,
                    category = it.categoryId?.let { catId -> categories[catId] }
                )
            }

            val currentList = _transactions.value
            val newList = if (isInitialLoad) {
                (plannedUiModels + recordedUiModels)
            } else {
                (currentList + recordedUiModels)
            }

            _transactions.value = newList.sortedByDescending { it.date }
            currentPage++

            if (isInitialLoad) _isLoading.value = false else _isLoadingMore.value = false
        }
    }
}
