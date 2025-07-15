package com.aestroon.common.domain

import HeldInstrument
import Instrument
import PortfolioAccount
import PortfolioAssetType
import PortfolioSummary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.ShowChart
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.entity.PortfolioEntity
import com.aestroon.common.data.entity.PortfolioInstrumentEntity
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.repository.CurrencyConversionRepository
import com.aestroon.common.data.repository.MarketDataRepository
import com.aestroon.common.data.repository.PortfolioRepository
import com.aestroon.common.data.repository.TimeRange
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PortfolioWithInstruments(
    val portfolio: PortfolioEntity,
    val instruments: List<PortfolioInstrumentEntity>
)

class PortfolioViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val authRepository: AuthRepository,
    private val marketDataRepository: MarketDataRepository,
    private val currencyConversionRepository: CurrencyConversionRepository
) : ViewModel() {

    private val _showAddAccountDialog = MutableStateFlow<String?>(null)
    val showAddAccountDialog = _showAddAccountDialog.asStateFlow()
    private val _showAddInstrumentDialog = MutableStateFlow<PortfolioAccount?>(null)
    val showAddInstrumentDialog = _showAddInstrumentDialog.asStateFlow()
    private val _showEditAccountDialog = MutableStateFlow<PortfolioAccount?>(null)
    val showEditAccountDialog = _showEditAccountDialog.asStateFlow()
    private val _showEditInstrumentDialog = MutableStateFlow<Pair<PortfolioAccount, HeldInstrument>?>(null)
    val showEditInstrumentDialog = _showEditInstrumentDialog.asStateFlow()

    private val _chartData = MutableStateFlow<List<Double>>(emptyList())
    val chartData: StateFlow<List<Double>> = _chartData.asStateFlow()
    private val _isChartLoading = MutableStateFlow(false)
    val isChartLoading: StateFlow<Boolean> = _isChartLoading.asStateFlow()
    private val _liveInstrumentPrices = MutableStateFlow<Map<String, Double>>(emptyMap())

    private val _marketDataError = MutableStateFlow<String?>(null)
    val marketDataError: StateFlow<String?> = _marketDataError.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.userIdFlow.filterNotNull().firstOrNull()?.let { userId ->
                portfolioRepository.syncAll(userId)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val portfolioWithInstruments: StateFlow<List<PortfolioWithInstruments>> = authRepository.userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            portfolioRepository.getPortfolios(userId).flatMapLatest { portfolios ->
                if (portfolios.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val flows = portfolios.map { portfolio ->
                        portfolioRepository.getInstrumentsForPortfolio(portfolio.id)
                            .map { instruments -> PortfolioWithInstruments(portfolio, instruments) }
                    }
                    combine(flows) { it.toList() }
                }
            }
        }
        .onEach { updateAllLivePrices(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<PortfolioAccount>> = combine(
        portfolioWithInstruments,
        _liveInstrumentPrices,
        currencyConversionRepository.exchangeRates
    ) { list, pricesInUsd, rates ->
        if (rates.isNullOrEmpty()) return@combine emptyList()
        val baseCurrency = currencyConversionRepository.baseCurrency.value
        val usdRateInBase = rates["USD"] ?: 1.0

        list.map { portfolioWithInstruments ->
            val heldInstruments = portfolioWithInstruments.instruments.map { entity ->
                val assetType = PortfolioAssetType.valueOf(portfolioWithInstruments.portfolio.type)
                val rateForPurchaseCurrency = rates[entity.currency] ?: 1.0
                val averageBuyPriceInBase = entity.averageBuyPrice / rateForPurchaseCurrency
                val livePriceInUsd = pricesInUsd[entity.id]
                val livePriceInBase = if (livePriceInUsd != null) livePriceInUsd / usdRateInBase else averageBuyPriceInBase

                HeldInstrument(
                    instrument = Instrument(
                        id = entity.id, name = entity.name, symbol = entity.symbol,
                        currentPrice = livePriceInBase, currency = baseCurrency, type = assetType,
                        icon = when (assetType) {
                            PortfolioAssetType.STOCKS -> Icons.Default.ShowChart
                            PortfolioAssetType.CRYPTO -> Icons.Default.CurrencyBitcoin
                            PortfolioAssetType.BONDS -> Icons.Default.AccountBalance
                            else -> null
                        },
                        maturityDate = entity.maturityDate?.let { Date(it) },
                        couponRate = entity.couponRate
                    ),
                    quantity = entity.quantity, averageBuyPrice = averageBuyPriceInBase
                )
            }
            PortfolioAccount(
                id = portfolioWithInstruments.portfolio.id,
                accountName = portfolioWithInstruments.portfolio.name,
                accountType = PortfolioAssetType.valueOf(portfolioWithInstruments.portfolio.type),
                instruments = heldInstruments
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallSummary: StateFlow<PortfolioSummary> = accounts.map { portfolioAccounts ->
        val totalValue = portfolioAccounts.sumOf { it.totalValue }
        val totalCost = portfolioAccounts.sumOf { it.totalCostBasis }
        val totalProfitLoss = totalValue - totalCost
        val totalProfitLossPercentage = if (totalCost == 0.0) 0.0 else (totalProfitLoss / totalCost) * 100
        val breakdown = portfolioAccounts.groupBy { it.accountType }.mapValues { entry -> entry.value.sumOf { it.totalValue } }
        val historicalData = if (totalValue > 0) List(30) { totalValue } else emptyList()
        PortfolioSummary(totalValue, totalProfitLoss, totalProfitLossPercentage, breakdown, historicalData)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PortfolioSummary(0.0, 0.0, 0.0, emptyMap(), emptyList()))

    private fun updateAllLivePrices(portfolios: List<PortfolioWithInstruments>) {
        viewModelScope.launch {
            val newPrices = _liveInstrumentPrices.value.toMutableMap()
            portfolios.forEach { portfolioWithInstruments ->
                val portfolioType = PortfolioAssetType.valueOf(portfolioWithInstruments.portfolio.type)
                portfolioWithInstruments.instruments.forEach { instrument ->
                    if (newPrices[instrument.id] == null) {
                        val result = when (portfolioType) {
                            PortfolioAssetType.CRYPTO -> marketDataRepository.getHistoricalCryptoData(instrument.symbol, 1)
                            PortfolioAssetType.STOCKS -> marketDataRepository.getHistoricalStockData(instrument.symbol)
                            else -> Result.success(emptyList())
                        }
                        result.onSuccess { priceData ->
                            priceData.lastOrNull()?.let { newPrices[instrument.id] = it }
                        }
                    }
                }
            }
            _liveInstrumentPrices.value = newPrices
        }
    }

    fun fetchHistoricalData(instrument: HeldInstrument, range: TimeRange) {
        viewModelScope.launch {
            _isChartLoading.value = true

            val result = when (instrument.instrument.type) {
                PortfolioAssetType.CRYPTO -> marketDataRepository.getHistoricalCryptoData(instrument.instrument.symbol, range.days)
                PortfolioAssetType.STOCKS -> marketDataRepository.getHistoricalStockData(instrument.instrument.symbol)
                else -> Result.failure(IllegalArgumentException("Data not available for this asset type."))
            }

            result.onSuccess { data ->
                _chartData.value = data
            }.onFailure {
                _chartData.value = emptyList()
            }
            _isChartLoading.value = false
        }
    }

    fun clearChartData() {
        _chartData.value = emptyList()
        _marketDataError.value = null
    }

    fun onDataErrorShown() {
        _marketDataError.value = null
    }

    fun onAddAccountClicked(assetType: String) { _showAddAccountDialog.value = assetType }
    fun onAddAccountDialogDismiss() { _showAddAccountDialog.value = null }
    fun onAddAccountConfirm(accountName: String, assetType: String) {
        viewModelScope.launch {
            val userId = authRepository.userIdFlow.first() ?: return@launch
            val portfolio = PortfolioEntity(name = accountName, description = null, balance = 0L, color = "#808080", iconName = null, type = assetType, userId = userId)
            portfolioRepository.addPortfolio(portfolio)
            onAddAccountDialogDismiss()
        }
    }

    fun onAddInstrumentClicked(account: PortfolioAccount) { _showAddInstrumentDialog.value = account }
    fun onAddInstrumentDialogDismiss() { _showAddInstrumentDialog.value = null }
    fun onAddInstrumentConfirm(account: PortfolioAccount, symbol: String, name: String, currency: String, quantity: Double, price: Double, maturityDateStr: String, couponRate: Double?) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val maturityDate: Date? = try { if (maturityDateStr.isNotBlank()) dateFormat.parse(maturityDateStr) else null } catch (e: Exception) { null }
            val instrument = PortfolioInstrumentEntity(portfolioId = account.id, symbol = symbol.uppercase(), name = name, quantity = quantity, averageBuyPrice = price, currency = currency, maturityDate = maturityDate?.time, couponRate = couponRate)
            portfolioRepository.addInstrument(instrument)
            onAddInstrumentDialogDismiss()
        }
    }

    fun onEditAccountClicked(account: PortfolioAccount) { _showEditAccountDialog.value = account }
    fun onEditAccountDialogDismiss() { _showEditAccountDialog.value = null }
    fun onEditAccountConfirm(id: String, name: String, type: String) {
        viewModelScope.launch {
            val originalPortfolio = portfolioWithInstruments.value.find { it.portfolio.id == id }?.portfolio
            if (originalPortfolio != null) {
                val updatedPortfolio = originalPortfolio.copy(name = name, type = type, isSynced = false)
                portfolioRepository.updatePortfolio(updatedPortfolio)
            }
            onEditAccountDialogDismiss()
        }
    }

    fun onEditInstrumentClicked(account: PortfolioAccount, instrument: HeldInstrument) { _showEditInstrumentDialog.value = account to instrument }
    fun onEditInstrumentDialogDismiss() { _showEditInstrumentDialog.value = null }
    fun onEditInstrumentConfirm(account: PortfolioAccount, instrument: HeldInstrument, symbol: String, name: String, currency: String, quantity: Double, price: Double, maturityDateStr: String, couponRate: Double?) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val maturityDate: Date? = try { if (maturityDateStr.isNotBlank()) dateFormat.parse(maturityDateStr) else null } catch (e: Exception) { null }
            val updatedInstrument = PortfolioInstrumentEntity(id = instrument.instrument.id, portfolioId = account.id, symbol = symbol.uppercase(), name = name, quantity = quantity, averageBuyPrice = price, currency = currency, maturityDate = maturityDate?.time, couponRate = couponRate, isSynced = false)
            portfolioRepository.updateInstrument(updatedInstrument)
            onEditInstrumentDialogDismiss()
        }
    }

    fun onDeleteAccount(portfolioId: String) { viewModelScope.launch { portfolioRepository.deletePortfolio(portfolioId) } }
    fun onDeleteInstrument(instrumentId: String) { viewModelScope.launch { portfolioRepository.deleteInstrument(instrumentId) } }
}
