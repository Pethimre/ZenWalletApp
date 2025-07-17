package com.aestroon.common.domain

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.data.model.MonthlyCashFlow
import com.aestroon.common.data.model.WalletsSummary
import com.aestroon.common.data.repository.CurrencyConversionRepository
import com.aestroon.common.data.repository.CurrencyRepository
import com.aestroon.common.data.repository.TransactionRepository
import com.aestroon.common.data.serializable.Currency
import com.aestroon.common.utilities.network.ConnectivityObserver
import com.aestroon.common.data.repository.WalletRepository
import com.aestroon.common.presentation.state.WalletsUiState
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WalletMonthlySummary(
    val income: Double,
    val expense: Double
)

@OptIn(ExperimentalCoroutinesApi::class)
class WalletsViewModel(
    private val walletRepository: WalletRepository,
    private val currencyRepository: CurrencyRepository,
    private val currencyConversionRepository: CurrencyConversionRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WalletsUiState>(WalletsUiState.Idle)
    val uiState: StateFlow<WalletsUiState> = _uiState.asStateFlow()

    private val _allCurrencies = MutableStateFlow<List<Currency>>(emptyList())
    val allCurrencies: StateFlow<List<Currency>> = _allCurrencies.asStateFlow()

    private val _monthlySummary = MutableStateFlow<WalletMonthlySummary?>(null)
    val monthlySummary: StateFlow<WalletMonthlySummary?> = _monthlySummary.asStateFlow()

    val baseCurrency: StateFlow<String> = currencyConversionRepository.baseCurrency
    val exchangeRates: StateFlow<Map<String, Double>?> = currencyConversionRepository.exchangeRates

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

    val summary: StateFlow<WalletsSummary> = combine(wallets, exchangeRates, baseCurrency) { walletList, rates, base ->
        val includedWallets = walletList.filter { it.included }
        var totalInBase = 0.0
        if (rates != null) {
            includedWallets.forEach { wallet ->
                val conversionRate = if (wallet.currency == base) 1.0 else (rates[base] ?: 1.0) / (rates[wallet.currency] ?: 1.0)
                totalInBase += (wallet.balance / 100.0) * conversionRate
            }
        }
        val totalOriginal = includedWallets.sumOf { it.balance }.toDouble()
        val breakdown = if (totalOriginal > 0) {
            includedWallets.map { it to (it.balance / totalOriginal).toFloat() }
        } else { emptyList() }

        WalletsSummary(totalBalance = (totalInBase * 100).toLong(), balanceBreakdown = breakdown)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WalletsSummary())

    val monthlyCashFlow: StateFlow<List<MonthlyCashFlow>> = authRepository.userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            combine(
                transactionRepository.getTransactionsForUser(userId),
                baseCurrency,
                exchangeRates
            ) { transactions, base, rates ->
                if (rates == null) return@combine emptyList()

                val calendar = Calendar.getInstance()
                (0..3).map { monthOffset ->
                    calendar.time = Date()
                    calendar.add(Calendar.MONTH, -monthOffset)
                    val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
                    val month = calendar.get(Calendar.MONTH)
                    val year = calendar.get(Calendar.YEAR)

                    var income = 0.0
                    var expense = 0.0

                    transactions.filter {
                        val transactionCal = Calendar.getInstance().apply { time = it.date }
                        transactionCal.get(Calendar.MONTH) == month && transactionCal.get(Calendar.YEAR) == year
                    }.forEach { transaction ->
                        val amount = transaction.amount / 100.0
                        val convertedAmount = if (transaction.currency == base) {
                            amount
                        } else {
                            val rate = rates[transaction.currency] ?: 0.0
                            if (rate != 0.0) amount / rate else 0.0
                        }

                        if (transaction.transactionType == TransactionType.INCOME) {
                            income += convertedAmount
                        } else if (transaction.transactionType == TransactionType.EXPENSE) {
                            expense += convertedAmount
                        }
                    }
                    MonthlyCashFlow(monthName, income.toFloat(), expense.toFloat())
                }.reversed()
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadAllCurrencies()
        observeNetworkAndSync()
    }

    fun loadMonthlySummaryFor(walletId: String?) {
        if (walletId == null) {
            _monthlySummary.value = null
            return
        }
        viewModelScope.launch {
            // This logic is specifically for a single wallet, used when a card is expanded.
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
