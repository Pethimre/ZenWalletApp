package com.aestroon.common.data.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class RateTrend {
    UP, DOWN, STABLE
}

data class CurrencyExchangeInfo(
    val currencyCode: String,
    val currencyName: String,
    val rateInBaseCurrency: Double,
    val trend: RateTrend,
    val icon: ImageVector
)