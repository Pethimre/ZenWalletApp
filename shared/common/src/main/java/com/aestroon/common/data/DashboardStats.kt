package com.aestroon.common.data

import com.aestroon.common.utilities.DEFAULT_BASE_CURRENCY

data class DashboardStats(
    val netWorth: Double = 0.0,
    val savingsRate: Float = 0f,
    val thisMonthCashFlow: Double = 0.0,
    val baseCurrency: String = DEFAULT_BASE_CURRENCY
)