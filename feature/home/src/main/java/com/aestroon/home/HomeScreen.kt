package com.aestroon.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aestroon.home.mockProvider.sampleExchangeRates
import com.aestroon.home.widgets.balanceOverview.BalanceOverviewCard
import com.aestroon.home.widgets.exchangeRow.ExpandableExchangeRateWidgetCard
import com.aestroon.home.widgets.savingSummary.SavingsSummaryCard

@Composable
fun HomeScreen() {
    Column {
        SavingsSummaryCard(
            modifier = Modifier,
            revenueLastWeek = "12,300.42 HUF",
            foodLastWeek = "845.72 HUF",
            savingsGoalPercentage = .25f,
        )
        Spacer(Modifier.height(20.dp))
        BalanceOverviewCard(
            modifier = Modifier,
            totalBalance = "54210.22 HUF",
            totalExpense = "41221.1 HUF",
            expenseTrackingValue = 42f,
            expenseProgress = .11f,
            statusMessage = "This is a status message",
            statusIcon = Icons.Default.Star,
        )
        Spacer(Modifier.height(20.dp))
        ExpandableExchangeRateWidgetCard(
            allExchangeRates = sampleExchangeRates,
            ratesForCollapsedView = sampleExchangeRates.take(3),
            initiallyExpanded = false
        )
    }
}

