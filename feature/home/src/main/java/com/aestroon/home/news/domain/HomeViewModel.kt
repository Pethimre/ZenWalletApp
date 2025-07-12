package com.aestroon.home.news.domain

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CurrencyFranc
import androidx.compose.material.icons.filled.CurrencyPound
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.Paid
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.model.CurrencyExchangeInfo
import com.aestroon.common.data.model.RateTrend
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.repository.CurrencyConversionRepository
import com.aestroon.common.domain.CategoriesViewModel
import com.aestroon.common.domain.PlannedPaymentsViewModel
import com.aestroon.common.domain.TransactionsViewModel
import com.aestroon.common.domain.WalletsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val walletsViewModel: WalletsViewModel,
    private val transactionsViewModel: TransactionsViewModel,
    private val categoriesViewModel: CategoriesViewModel,
    private val plannedPaymentsViewModel: PlannedPaymentsViewModel,
    private val newsViewModel: NewsViewModel,
    private val currencyConversionRepository: CurrencyConversionRepository,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val exchangeRateInfos: StateFlow<List<CurrencyExchangeInfo>> = currencyConversionRepository.exchangeRates
        .map { rates ->
            val targetCurrencies = listOf("USD", "EUR", "GBP", "CHF", "PLN", "CZK")
            rates?.filterKeys { it in targetCurrencies }?.map { (code, rate) ->
                val rateInBase = 1 / rate
                CurrencyExchangeInfo(
                    currencyCode = code,
                    currencyName = getNameForCurrency(code),
                    rateInBaseCurrency = rateInBase,
                    trend = RateTrend.STABLE,
                    icon = getIconForCurrency(code)
                )
            }?.sortedBy { it.currencyCode } ?: emptyList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val collapsedExchangeRateInfos: StateFlow<List<CurrencyExchangeInfo>> = combine(
        exchangeRateInfos,
        currencyConversionRepository.baseCurrency
    ) { allRates, baseCurrency ->
        val priorityCodes = listOf("EUR", "USD", "GBP")
        val allOtherCodes = allRates.map { it.currencyCode }.filterNot { it in priorityCodes }

        val finalCurrencyOrder = (priorityCodes + allOtherCodes).filterNot { it == baseCurrency }

        val ratesMap = allRates.associateBy { it.currencyCode }

        finalCurrencyOrder.take(3).mapNotNull { code -> ratesMap[code] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            authRepository.userIdFlow.filterNotNull().collect { userId ->
                Log.d("AppStartup", "HomeViewModel: User logged in with ID $userId. Triggering data refresh.")
                refreshAllData()
            }
        }
    }

    fun refreshAllData() {
        viewModelScope.launch {
            Log.d("AppStartup", "refreshAllData: Refresh triggered.")
            _isRefreshing.value = true

            walletsViewModel.onEnterScreen()
            categoriesViewModel.onEnterScreen()
            transactionsViewModel.syncTransactions()
            plannedPaymentsViewModel.syncPlannedPayments()
            newsViewModel.refresh()
            _isRefreshing.value = false
            Log.d("AppStartup", "refreshAllData: Refresh finished.")
        }
    }

    private fun getNameForCurrency(code: String): String = when (code) {
        "USD" -> "US Dollar"
        "EUR" -> "Euro"
        "GBP" -> "British Pound"
        "CHF" -> "Swiss Franc"
        "PLN" -> "Polish Zloty"
        "CZK" -> "Czech Koruna"
        else -> "Unknown"
    }

    private fun getIconForCurrency(code: String): ImageVector = when (code) {
        "USD" -> Icons.Default.AttachMoney
        "EUR" -> Icons.Default.EuroSymbol
        "CHF" -> Icons.Default.CurrencyFranc
        "GBP" -> Icons.Default.CurrencyPound
        else -> Icons.Default.Paid
    }
}
