package com.aestroon.common.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.model.CalculationResult
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.round

enum class CompoundingFrequency(val displayName: String, val periodsPerYear: Int) {
    YEARLY("Yearly", 1),
    SEMI_ANNUALLY("Semi-Annually", 2),
    QUARTERLY("Quarterly", 4),
    MONTHLY("Monthly", 12),
    DAILY("Daily", 365);
}

class CalculatorViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _result = MutableStateFlow(CalculationResult())
    val result = _result.asStateFlow()

    private val _baseCurrency = MutableStateFlow("USD")
    val baseCurrency = _baseCurrency.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.userIdFlow.firstOrNull()?.let { userId ->
                userRepository.getUserProfile(userId).getOrNull()?.let { profile ->
                    _baseCurrency.value = profile.base_currency
                }
            }
        }
    }

    fun calculate(
        principalStr: String,
        monthlyContributionStr: String,
        yearsStr: String,
        rateStr: String,
        frequency: CompoundingFrequency
    ) {
        val principal = ((principalStr.toDoubleOrNull() ?: 0.0) * 100).toLong()
        val monthlyContribution = ((monthlyContributionStr.toDoubleOrNull() ?: 0.0) * 100).toLong()
        val years = yearsStr.toIntOrNull() ?: 0
        val annualRate = rateStr.toDoubleOrNull() ?: 0.0

        if (years == 0) {
            _result.value = CalculationResult()
            return
        }

        val periodsPerYear = frequency.periodsPerYear.toDouble() // m
        val ratePerPeriod = (annualRate / 100.0) / periodsPerYear // r / m

        // monthlyFactor = (1 + r/m)^(m/12)  -> exact multiplier for each month
        val monthlyFactor = (1.0 + ratePerPeriod).pow(periodsPerYear / 12.0)

        val compoundChartData = mutableListOf<Double>()
        val linearChartData = mutableListOf<Double>()

        var compoundBalance = principal.toDouble()
        var linearBalance = principal.toDouble()

        val totalMonths = years * 12
        for (month in 1..totalMonths) {
            // Apply interest for the month (fractional compounding handled by pow)
            compoundBalance *= monthlyFactor

            // Add monthly contribution at the END of the month (ordinary annuity)
            compoundBalance += monthlyContribution.toDouble()
            linearBalance += monthlyContribution.toDouble()

            // Record yearly snapshot
            if (month % 12 == 0) {
                compoundChartData.add(compoundBalance / 100.0)
                linearChartData.add(linearBalance / 100.0)
            }
        }

        val futureValue = round(compoundBalance).toLong()
        val totalContributions = principal + (monthlyContribution * 12 * years)
        val totalInterest = futureValue - totalContributions

        _result.value = CalculationResult(
            futureValue = futureValue,
            totalContributions = totalContributions,
            totalInterest = totalInterest,
            compoundGrowthData = compoundChartData,
            linearGrowthData = linearChartData
        )
    }
}
