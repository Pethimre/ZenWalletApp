package com.aestroon.home.widgets.exchangeRow.mockProvider

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CurrencyFranc
import androidx.compose.material.icons.filled.CurrencyPound
import androidx.compose.material.icons.filled.EuroSymbol
import com.aestroon.common.utilities.NUMBER_OF_CURRENCIES_ON_COMPACT
import com.aestroon.home.mockProvider.CurrencyExchangeInfo
import com.aestroon.home.mockProvider.RateTrend

val sampleExchangeRatesForWidget = listOf(
    CurrencyExchangeInfo("EUR", "Euro", Icons.Filled.EuroSymbol, 403.85, RateTrend.UP),
    CurrencyExchangeInfo("USD", "US Dollar", Icons.Filled.AttachMoney, 360.94, RateTrend.DOWN),
    CurrencyExchangeInfo("GBP", "British Pound", Icons.Filled.CurrencyPound, 480.39, RateTrend.STABLE),
    CurrencyExchangeInfo("CHF", "Swiss Franc", Icons.Filled.CurrencyFranc, 392.50, RateTrend.UP)
)

val sampleCollapsedRates = sampleExchangeRatesForWidget.take(NUMBER_OF_CURRENCIES_ON_COMPACT)
