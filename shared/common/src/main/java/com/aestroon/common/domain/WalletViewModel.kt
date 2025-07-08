package com.aestroon.common.domain

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.data.model.WalletsSummary
import com.aestroon.common.data.repository.CurrencyRepository
import com.aestroon.common.data.repository.TransactionRepository
import com.aestroon.common.data.serializable.Currency
import com.aestroon.common.utilities.network.ConnectivityObserver
import com.aestroon.common.data.repository.WalletRepository
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
import java.util.Calendar

data class WalletMonthlySummary(
    val income: Double,
    val expense: Double
)

class WalletsViewModel(
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository,
    private val currencyRepository: CurrencyRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WalletsUiState>(WalletsUiState.Idle)
    val uiState: StateFlow<WalletsUiState> = _uiState.asStateFlow()

    private val _allCurrencies = MutableStateFlow<List<Currency>>(emptyList())
    val allCurrencies: StateFlow<List<Currency>> = _allCurrencies.asStateFlow()

    private val _monthlySummary = MutableStateFlow<WalletMonthlySummary?>(null)
    val monthlySummary: StateFlow<WalletMonthlySummary?> = _monthlySummary.asStateFlow()

    val baseCurrency = MutableStateFlow("HUF")

    @OptIn(ExperimentalCoroutinesApi::class)
    val wallets: StateFlow<List<WalletEntity>> = authRepository.userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            walletRepository.getWalletsForUser(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasPendingSyncs: StateFlow<Boolean> = wallets.map { walletList ->
        walletList.any { !it.isSynced }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val networkStatus: StateFlow<ConnectivityObserver.Status> = connectivityObserver.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectivityObserver.Status.Unavailable)

    val summary: StateFlow<WalletsSummary> = wallets.map { walletList ->
        val includedWallets = walletList.filter { it.included }
        val total = includedWallets.sumOf { it.balance }
        val breakdown = if (total > 0) {
            includedWallets.map { it to (it.balance.toFloat() / total.toFloat()) }
        } else { emptyList() }
        WalletsSummary(totalBalance = total, balanceBreakdown = breakdown)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WalletsSummary())

    init {
        loadAllCurrencies()
        observeNetworkAndSync()
    }

    private fun observeNetworkAndSync() {
        viewModelScope.launch {
            networkStatus.collect { status ->
                if (status == ConnectivityObserver.Status.Available) {
                    fullSync()
                }
            }
        }
    }

    private fun loadAllCurrencies() {
        viewModelScope.launch {
            currencyRepository.getSupportedCurrencies()
                .onSuccess { _allCurrencies.value = it }
        }
    }

    fun onEnterScreen() {
        viewModelScope.launch {
            fullSync()
        }
    }

    private suspend fun fullSync() {
        authRepository.userIdFlow.firstOrNull()?.let { userId ->
            walletRepository.fetchRemoteWallets(userId)
            walletRepository.syncPendingWallets()
        }
    }

    fun loadMonthlySummaryFor(walletId: String?) {
        if (walletId == null) {
            _monthlySummary.value = null
            return
        }
        viewModelScope.launch {
            transactionRepository.getTransactionsForWallet(walletId).firstOrNull()?.let { transactions ->
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)

                val monthlyTransactions = transactions.filter {
                    calendar.time = it.date
                    calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear
                }

                val income = monthlyTransactions
                    .filter { it.transactionType == TransactionType.INCOME }
                    .sumOf { it.amount / 100.0 }

                val expense = monthlyTransactions
                    .filter { it.transactionType == TransactionType.EXPENSE }
                    .sumOf { it.amount / 100.0 }

                _monthlySummary.value = WalletMonthlySummary(income, expense)
            }
        }
    }

    fun addOrUpdateWallet(
        existingWallet: WalletEntity?,
        name: String,
        balanceStr: String,
        goalAmountStr: String,
        color: Color,
        currency: String,
        iconName: String,
        included: Boolean
    ) {
        viewModelScope.launch {
            val userId = authRepository.userIdFlow.first()
            if (userId == null) {
                _uiState.value = WalletsUiState.Error("User not found."); return@launch
            }

            val balanceInCents = (balanceStr.replace(",", ".").toDoubleOrNull() ?: 0.0).times(100).toLong()
            val goalAmountInCents = (goalAmountStr.replace(",", ".").toDoubleOrNull() ?: 0.0).times(100).toLong()

            val result = if (existingWallet == null) {
                val walletEntity = WalletEntity(
                    displayName = name,
                    balance = balanceInCents,
                    goalAmount = goalAmountInCents,
                    color = color.toHexString(),
                    currency = currency,
                    ownerId = userId,
                    iconName = iconName,
                    included = included
                )
                walletRepository.addWallet(walletEntity)
            } else {
                val updatedWallet = existingWallet.copy(
                    displayName = name,
                    balance = balanceInCents,
                    goalAmount = goalAmountInCents,
                    color = color.toHexString(),
                    currency = currency,
                    iconName = iconName,
                    included = included
                )
                walletRepository.updateWallet(updatedWallet)
            }

            result.onSuccess {
                if (networkStatus.value == ConnectivityObserver.Status.Available) {
                    walletRepository.syncPendingWallets()
                }
            }.onFailure { _uiState.value = WalletsUiState.Error("Failed to save wallet: ${it.message}") }
        }
    }

    fun deleteWallet(wallet: WalletEntity) {
        viewModelScope.launch {
            walletRepository.deleteWallet(wallet).onFailure {
                _uiState.value = WalletsUiState.Error("Failed to delete wallet.")
            }
        }
    }

    private fun Color.toHexString(): String {
        return String.format("#%08X", this.toArgb())
    }
}