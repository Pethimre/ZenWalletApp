package com.aestroon.common.data.model

data class CalculationResult(
    val futureValue: Long = 0L,
    val totalContributions: Long = 0L,
    val totalInterest: Long = 0L,
    val compoundGrowthData: List<Double> = emptyList(),
    val linearGrowthData: List<Double> = emptyList()
)