package com.aestroon.common.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.entity.CategoryEntity
import com.aestroon.common.data.entity.TransactionEntity
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.repository.CategoryRepository
import com.aestroon.common.data.repository.TransactionRepository
import com.aestroon.common.data.repository.WalletRepository
import com.aestroon.common.presentation.TransactionUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModel(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionUiState>(TransactionUiState.Idle)
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<TransactionEntity>> = authRepository.userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            transactionRepository.getTransactionsForUser(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val wallets: StateFlow<List<WalletEntity>> = authRepository.userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            walletRepository.getWalletsForUser(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: StateFlow<List<CategoryEntity>> = authRepository.userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            categoryRepository.getCategoriesForUser(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoriesMap: StateFlow<Map<String, CategoryEntity>> = categories.map { list ->
        list.associateBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
                .onFailure { _uiState.value = TransactionUiState.Error("Failed to delete transaction.") }
        }
    }
}