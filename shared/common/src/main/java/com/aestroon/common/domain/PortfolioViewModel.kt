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
import androidx.lifecycle.SavedStateHandle
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
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PortfolioWithInstruments(
    val portfolio: PortfolioEntity,
    val instruments: List<PortfolioInstrumentEntity>
)

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val authRepository: AuthRepository,
    private val marketDataRepository: MarketDataRepository,
    private val currencyConversionRepository: CurrencyConversionRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // region Dialog Visibility State
    private val _showAddAccountDialog = MutableStateFlow<String?>(null)
    val showAddAccountDialog = _showAddAccountDialog.asStateFlow()

    private val _showEditAccountDialog = MutableStateFlow<PortfolioAccount?>(null)
    val showEditAccountDialog = _showEditAccountDialog.asStateFlow()

    val showAddInstrumentDialogFor: StateFlow<PortfolioAccount?> =
        savedStateHandle.getStateFlow<String?>(ADD_INSTRUMENT_ACCOUNT_ID, null)
            .flatMapLatest { accountId ->
                if (accountId == null) flowOf(null)
                else accounts.map { accs -> accs.find { it.id == accountId } }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val showEditInstrumentDialogFor: StateFlow<Pair<PortfolioAccount, HeldInstrument>?> =
        savedStateHandle.getStateFlow<String?>(EDIT_INSTRUMENT_ID, null)
            .flatMapLatest { instrumentId ->
                if (instrumentId == null) flowOf(null)
                else accounts.map { accs ->
                    var result: Pair<PortfolioAccount, HeldInstrument>? = null
                    for (acc in accs) {
                        val instrument = acc.instruments.find { it.instrument.id == instrumentId }
                        if (instrument != null) {
                            result = acc to instrument
                            break
                        }
                    }
                    result
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    // endregion

    // region Add/Edit Instrument Dialog State
    val dialogSymbol = savedStateHandle.getStateFlow(DIALOG_SYMBOL, "")
    val dialogName = savedStateHandle.getStateFlow(DIALOG_NAME, "")
    val dialogCurrency = savedStateHandle.getStateFlow(DIALOG_CURRENCY, "HUF")
    val dialogQuantity = savedStateHandle.getStateFlow(DIALOG_QUANTITY, "")
    val dialogPrice = savedStateHandle.getStateFlow(DIALOG_PRICE, "")
    val dialogMaturityDate = savedStateHandle.getStateFlow(DIALOG_MATURITY, "")
    val dialogCouponRate = savedStateHandle.getStateFlow(DIALOG_COUPON, "")
    val dialogLookupPrice = savedStateHandle.getStateFlow(DIALOG_LOOKUP, true)
    val dialogCurrentPrice = savedStateHandle.getStateFlow(DIALOG_CURRENT_PRICE, "")

    fun onDialogSymbolChange(value: String) { savedStateHandle[DIALOG_SYMBOL] = value }
    fun onDialogNameChange(value: String) { savedStateHandle[DIALOG_NAME] = value }
    fun onDialogCurrencyChange(value: String) { savedStateHandle[DIALOG_CURRENCY] = value.uppercase() }
    fun onDialogQuantityChange(value: String) { savedStateHandle[DIALOG_QUANTITY] = value }
    fun onDialogPriceChange(value: String) { savedStateHandle[DIALOG_PRICE] = value }
    fun onDialogMaturityDateChange(value: String) { savedStateHandle[DIALOG_MATURITY] = value }
    fun onDialogCouponRateChange(value: String) { savedStateHandle[DIALOG_COUPON] = value }
    fun onDialogLookupPriceChange(value: Boolean) { savedStateHandle[DIALOG_LOOKUP] = value }
    fun onDialogCurrentPriceChange(value: String) { savedStateHandle[DIALOG_CURRENT_PRICE] = value }

    private fun clearDialogState() {
        savedStateHandle[DIALOG_SYMBOL] = ""
        savedStateHandle[DIALOG_NAME] = ""
        savedStateHandle[DIALOG_CURRENCY] = "HUF"
        savedStateHandle[DIALOG_QUANTITY] = ""
        savedStateHandle[DIALOG_PRICE] = ""
        savedStateHandle[DIALOG_MATURITY] = ""
        savedStateHandle[DIALOG_COUPON] = ""
        savedStateHandle[DIALOG_LOOKUP] = true
        savedStateHandle[DIALOG_CURRENT_PRICE] = ""
    }
    // endregion

    // region UI State (Chart, Loading, etc.)
    private val _chartData = MutableStateFlow<List<Double>>(emptyList())
    val chartData: StateFlow<List<Double>> = _chartData.asStateFlow()
    private val _isChartLoading = MutableStateFlow(false)
    val isChartLoading: StateFlow<Boolean> = _isChartLoading.asStateFlow()
    private val _liveInstrumentPrices = MutableStateFlow<Map<String, Double>>(emptyMap())

    private val _marketDataError = MutableStateFlow<String?>(null)
    val marketDataError: StateFlow<String?> = _marketDataError.asStateFlow()
    // endregion

    init {
        viewModelScope.launch {
            authRepository.userIdFlow.filterNotNull().firstOrNull()?.let { userId ->
                portfolioRepository.syncAll(userId)
            }
        }
    }

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

        list.map { portfolioWithInstruments ->
            val heldInstruments = portfolioWithInstruments.instruments.map { entity ->
                val assetType = PortfolioAssetType.valueOf(portfolioWithInstruments.portfolio.type)
                val rateForInstrumentCurrency = rates[entity.currency] ?: 1.0
                val averageBuyPriceInBase = entity.averageBuyPrice / rateForInstrumentCurrency
                val livePriceInUsd = pricesInUsd[entity.id]
                val usdToBaseRate = rates["USD"] ?: 1.0

                val currentPriceInBase = when {
                    !entity.lookupPrice -> {
                        val manualPrice = entity.lastUpdatedPrice ?: entity.averageBuyPrice
                        manualPrice / rateForInstrumentCurrency
                    }
                    livePriceInUsd != null -> livePriceInUsd / usdToBaseRate
                    else -> averageBuyPriceInBase
                }

                HeldInstrument(
                    instrument = Instrument(
                        id = entity.id, name = entity.name, symbol = entity.symbol,
                        currentPrice = currentPriceInBase,
                        currency = baseCurrency,
                        type = assetType,
                        icon = when (assetType) {
                            PortfolioAssetType.STOCKS -> Icons.Default.ShowChart
                            PortfolioAssetType.CRYPTO -> Icons.Default.CurrencyBitcoin
                            PortfolioAssetType.BONDS -> Icons.Default.AccountBalance
                            else -> null
                        },
                        maturityDate = entity.maturityDate?.let { Date(it) },
                        couponRate = entity.couponRate
                    ),
                    quantity = entity.quantity,
                    averageBuyPrice = averageBuyPriceInBase,
                    lookupPrice = entity.lookupPrice
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
                    if (instrument.lookupPrice && newPrices[instrument.id] == null) {
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

    fun onAddInstrumentClicked(account: PortfolioAccount) {
        savedStateHandle[ADD_INSTRUMENT_ACCOUNT_ID] = account.id
    }
    fun onAddInstrumentDialogDismiss() {
        savedStateHandle[ADD_INSTRUMENT_ACCOUNT_ID] = null
        clearDialogState()
    }

    fun onAddInstrumentConfirm() {
        viewModelScope.launch {
            val account = showAddInstrumentDialogFor.value ?: return@launch
            val instrument = PortfolioInstrumentEntity(
                portfolioId = account.id,
                symbol = dialogSymbol.value.uppercase(),
                name = if (dialogName.value.isNotBlank()) dialogName.value else dialogSymbol.value,
                quantity = dialogQuantity.value.toDoubleOrNull() ?: 0.0,
                averageBuyPrice = dialogPrice.value.toDoubleOrNull() ?: 0.0,
                currency = dialogCurrency.value,
                maturityDate = try { if (dialogMaturityDate.value.isNotBlank()) dateFormatter.parse(dialogMaturityDate.value)?.time else null } catch (e: Exception) { null },
                couponRate = dialogCouponRate.value.toDoubleOrNull(),
                lookupPrice = dialogLookupPrice.value,
                lastUpdatedPrice = if (!dialogLookupPrice.value) dialogCurrentPrice.value.toDoubleOrNull() else null,
                lastUpdatedDate = if (!dialogLookupPrice.value) System.currentTimeMillis() else null
            )
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

    fun onEditInstrumentClicked(account: PortfolioAccount, instrument: HeldInstrument) {
        // Populate state holders with existing instrument data
        savedStateHandle[DIALOG_SYMBOL] = instrument.instrument.symbol
        savedStateHandle[DIALOG_NAME] = instrument.instrument.name
        savedStateHandle[DIALOG_CURRENCY] = instrument.instrument.currency
        savedStateHandle[DIALOG_QUANTITY] = instrument.quantity.toString()
        savedStateHandle[DIALOG_PRICE] = instrument.averageBuyPrice.toString()
        savedStateHandle[DIALOG_MATURITY] = instrument.instrument.maturityDate?.let { dateFormatter.format(it) } ?: ""
        savedStateHandle[DIALOG_COUPON] = instrument.instrument.couponRate?.toString() ?: ""
        savedStateHandle[DIALOG_LOOKUP] = instrument.lookupPrice
        savedStateHandle[DIALOG_CURRENT_PRICE] = instrument.instrument.currentPrice.toString()

        // Show the dialog
        savedStateHandle[EDIT_INSTRUMENT_ID] = instrument.instrument.id
    }
    fun onEditInstrumentDialogDismiss() {
        savedStateHandle[EDIT_INSTRUMENT_ID] = null
        clearDialogState()
    }

    fun onEditInstrumentConfirm() {
        viewModelScope.launch {
            val (account, instrument) = showEditInstrumentDialogFor.value ?: return@launch
            val updatedInstrument = PortfolioInstrumentEntity(
                id = instrument.instrument.id,
                portfolioId = account.id,
                symbol = dialogSymbol.value.uppercase(),
                name = if (dialogName.value.isNotBlank()) dialogName.value else dialogSymbol.value,
                quantity = dialogQuantity.value.toDoubleOrNull() ?: 0.0,
                averageBuyPrice = dialogPrice.value.toDoubleOrNull() ?: 0.0,
                currency = dialogCurrency.value,
                maturityDate = try { if (dialogMaturityDate.value.isNotBlank()) dateFormatter.parse(dialogMaturityDate.value)?.time else null } catch (e: Exception) { null },
                couponRate = dialogCouponRate.value.toDoubleOrNull(),
                isSynced = false,
                lookupPrice = dialogLookupPrice.value,
                lastUpdatedPrice = if (!dialogLookupPrice.value) dialogCurrentPrice.value.toDoubleOrNull() else null,
                lastUpdatedDate = if (!dialogLookupPrice.value) System.currentTimeMillis() else null
            )
            portfolioRepository.updateInstrument(updatedInstrument)
            onEditInstrumentDialogDismiss()
        }
    }

    fun onUpdateInstrumentPrice(instrument: HeldInstrument, newPrice: Double) {
        viewModelScope.launch {
            val originalEntity = portfolioRepository.getInstrumentById(instrument.instrument.id).firstOrNull()
            if (originalEntity != null) {
                val updatedEntity = originalEntity.copy(
                    lastUpdatedPrice = newPrice,
                    lastUpdatedDate = System.currentTimeMillis(),
                    isSynced = false
                )
                portfolioRepository.updateInstrument(updatedEntity)
            }
        }
    }

    fun onDeleteAccount(portfolioId: String) { viewModelScope.launch { portfolioRepository.deletePortfolio(portfolioId) } }
    fun onDeleteInstrument(instrumentId: String) { viewModelScope.launch { portfolioRepository.deleteInstrument(instrumentId) } }

    companion object {
        private const val ADD_INSTRUMENT_ACCOUNT_ID = "addInstrumentAccountId"
        private const val EDIT_INSTRUMENT_ID = "editInstrumentId"

        private const val DIALOG_SYMBOL = "dialog_symbol"
        private const val DIALOG_NAME = "dialog_name"
        private const val DIALOG_CURRENCY = "dialog_currency"
        private const val DIALOG_QUANTITY = "dialog_quantity"
        private const val DIALOG_PRICE = "dialog_price"
        private const val DIALOG_MATURITY = "dialog_maturity"
        private const val DIALOG_COUPON = "dialog_coupon"
        private const val DIALOG_LOOKUP = "dialog_lookup"
        private const val DIALOG_CURRENT_PRICE = "dialog_current_price"
    }
}
