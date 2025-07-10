package com.aestroon.common.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.entity.CategoryEntity
import com.aestroon.common.data.entity.PlannedPaymentEntity
import com.aestroon.common.data.entity.RecurrenceType
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.repository.CategoryRepository
import com.aestroon.common.data.repository.CurrencyConversionRepository
import com.aestroon.common.data.repository.PlannedPaymentRepository
import com.aestroon.common.data.repository.WalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class PlannedPaymentsViewModel(
    private val plannedPaymentRepository: PlannedPaymentRepository,
    private val authRepository: AuthRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val currencyConversionRepository: CurrencyConversionRepository,
) : ViewModel() {

    val baseCurrency: StateFlow<String> = currencyConversionRepository.baseCurrency
    val exchangeRates: StateFlow<Map<String, Double>?> = currencyConversionRepository.exchangeRates

    @OptIn(ExperimentalCoroutinesApi::class)
    val plannedPayments: StateFlow<List<PlannedPaymentEntity>> = authRepository.userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            plannedPaymentRepository.getPlannedPayments(userId)
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

    fun syncPlannedPayments() {
        viewModelScope.launch {
            authRepository.userIdFlow.firstOrNull()?.let { userId ->
                plannedPaymentRepository.syncPlannedPayments(userId)
            }
        }
    }

    fun addOrUpdatePayment(
        existingPayment: PlannedPaymentEntity?,
        name: String,
        description: String?,
        dueDate: Date,
        amount: Long,
        wallet: WalletEntity,
        category: CategoryEntity?,
        recurrenceType: RecurrenceType,
        recurrenceValue: Int,
        transactionType: TransactionType,
        toWallet: WalletEntity?
    ) {
        viewModelScope.launch {
            val userId = authRepository.userIdFlow.first() ?: return@launch

            val payment = existingPayment?.copy(
                name = name,
                description = description,
                dueDate = dueDate,
                amount = amount,
                currency = wallet.currency,
                recurrenceType = recurrenceType,
                recurrenceValue = recurrenceValue,
                walletId = wallet.id,
                categoryId = if (transactionType == TransactionType.TRANSFER) null else category?.id,
                transactionType = transactionType,
                toWalletId = if (transactionType == TransactionType.TRANSFER) toWallet?.id else null
            ) ?: PlannedPaymentEntity(
                name = name,
                description = description,
                dueDate = dueDate,
                amount = amount,
                currency = wallet.currency,
                recurrenceType = recurrenceType,
                recurrenceValue = recurrenceValue,
                userId = userId,
                walletId = wallet.id,
                categoryId = if (transactionType == TransactionType.TRANSFER) null else category?.id,
                transactionType = transactionType,
                toWalletId = if (transactionType == TransactionType.TRANSFER) toWallet?.id else null
            )
            plannedPaymentRepository.addOrUpdatePlannedPayment(payment)
        }
    }

    fun pay(payment: PlannedPaymentEntity) {
        viewModelScope.launch {
            plannedPaymentRepository.processPayment(payment)
        }
    }

    fun skip(payment: PlannedPaymentEntity) {
        viewModelScope.launch {
            plannedPaymentRepository.skipPayment(payment)
        }
    }

    fun deletePayment(payment: PlannedPaymentEntity) {
        viewModelScope.launch {
            plannedPaymentRepository.deletePayment(payment)
        }
    }
}
