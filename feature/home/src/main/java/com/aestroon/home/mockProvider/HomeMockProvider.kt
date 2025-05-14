package com.aestroon.home.mockProvider

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CurrencyFranc
import androidx.compose.material.icons.filled.CurrencyPound
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.ui.graphics.vector.ImageVector

// Enum to represent the trend of the exchange rate
enum class RateTrend {
    UP, DOWN, STABLE
}

// Data class to hold information for each currency
data class CurrencyExchangeInfo(
    val currencyCode: String,
    val currencyName: String, // e.g., "Euro", "US Dollar"
    val icon: ImageVector,
    val rateInHUF: Double,
    val trend: RateTrend = RateTrend.STABLE
)

// Sample data - In a real app, this would come from an API
val sampleExchangeRates = listOf(
    CurrencyExchangeInfo("EUR", "Euro", Icons.Filled.EuroSymbol, 385.25, RateTrend.UP),
    CurrencyExchangeInfo("USD", "US Dollar", Icons.Filled.AttachMoney, 350.70, RateTrend.DOWN),
    CurrencyExchangeInfo("GBP", "British Pound", Icons.Filled.CurrencyPound, 440.10, RateTrend.STABLE),
    CurrencyExchangeInfo("CHF", "Swiss Franc", Icons.Filled.CurrencyFranc, 392.50, RateTrend.UP)
)
