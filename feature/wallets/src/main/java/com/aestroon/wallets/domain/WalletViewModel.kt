package com.aestroon.wallets.domain

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.authentication.data.AuthRepository
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.data.model.WalletsSummary
import com.aestroon.common.data.repository.CurrencyRepository
import com.aestroon.common.data.serializable.Currency
import com.aestroon.common.utilities.network.ConnectivityObserver
import com.aestroon.wallets.data.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WalletsViewModel(
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository,
    private val currencyRepository: CurrencyRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _wallets = MutableStateFlow<List<WalletEntity>>(emptyList())
    val wallets: StateFlow<List<WalletEntity>> = _wallets.asStateFlow()

    private val _uiState = MutableStateFlow<WalletsUiState>(WalletsUiState.Idle)
    val uiState: StateFlow<WalletsUiState> = _uiState.asStateFlow()

    private val _allCurrencies = MutableStateFlow<List<Currency>>(emptyList())
    val allCurrencies: StateFlow<List<Currency>> = _allCurrencies.asStateFlow()

    val hasPendingSyncs: StateFlow<Boolean> = wallets.map { walletList ->
        walletList.any { !it.isSynced }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val networkStatus: StateFlow<ConnectivityObserver.Status> = connectivityObserver.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectivityObserver.Status.Unavailable)

    val baseCurrency = MutableStateFlow("HUF")

    val summary: StateFlow<WalletsSummary> = wallets.map { walletList ->
        val includedWallets = walletList.filter { it.included }
        val total = includedWallets.sumOf { it.balance }
        val breakdown = if (total > 0) {
            includedWallets.map { it to (it.balance.toFloat() / total.toFloat()) }
        } else { emptyList() }
        WalletsSummary(totalBalance = total, balanceBreakdown = breakdown)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WalletsSummary())

    init {
        observeWallets()
        observeNetworkAndSync()
        loadAllCurrencies()
    }

    private fun observeWallets() {
        viewModelScope.launch {
            _uiState.value = WalletsUiState.Loading
            authRepository.getUpdatedUser().getOrNull()?.id?.let { userId ->
                walletRepository.getWalletsForUser(userId).collect { walletList ->
                    _wallets.value = walletList
                    _uiState.value = WalletsUiState.Idle
                }
            } ?: run {
                _uiState.value = WalletsUiState.Error("Could not find user.")
            }
        }
    }

    private suspend fun fullSync() {
        authRepository.getUpdatedUser().getOrNull()?.id?.let { userId ->
            walletRepository.fetchRemoteWallets(userId)
            walletRepository.syncPendingWallets()
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
            val userId = authRepository.getUpdatedUser().getOrNull()?.id
            if (userId == null) {
                _uiState.value = WalletsUiState.Error("User not found."); return@launch
            }

            val balanceInCents = (balanceStr.replace(",", ".").toDoubleOrNull() ?: 0.0).toLong() * 100
            val goalAmountInCents = (goalAmountStr.replace(",", ".").toDoubleOrNull() ?: 0.0).toLong() * 100

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

    fun Color.toHexString(): String {
        return String.format("#%08X", this.toArgb())
    }
}