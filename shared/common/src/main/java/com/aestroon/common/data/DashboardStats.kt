package com.aestroon.common.data

data class DashboardStats(
    val netWorth: Double = 0.0,
    val savingsRate: Float = 0f,
    val thisMonthCashFlow: Double = 0.0,
    val baseCurrency: String = "HUF"
)