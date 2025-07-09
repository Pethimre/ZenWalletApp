package com.aestroon.common.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.DashboardStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val walletsViewModel: WalletsViewModel,
    private val transactionsViewModel: TransactionsViewModel
) : ViewModel() {

    val stats: StateFlow<DashboardStats> = combine(
        walletsViewModel.summary,
        transactionsViewModel.currentMonthIncome,
        transactionsViewModel.currentMonthExpense,
        walletsViewModel.baseCurrency
    ) { summary, income, expense, currency ->

        val netWorth = summary.totalBalance / 100.0
        val cashFlow = income - expense
        val savingsRate = if (income > 0) (cashFlow / income).toFloat() else 0f

        DashboardStats(
            netWorth = netWorth,
            savingsRate = savingsRate,
            thisMonthCashFlow = cashFlow,
            baseCurrency = currency
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())
}