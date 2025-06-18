package com.aestroon.home.homeScreen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aestroon.common.components.CollapsibleSectionCard
import com.aestroon.common.components.ExpandableTransactionHeader
import com.aestroon.common.components.TransactionListItem
import com.aestroon.common.components.mockProvider.MOCK_BASE_CURRENCY
import com.aestroon.common.components.mockProvider.TransactionItemData
import com.aestroon.common.components.mockProvider.sampleOverdueTransactions
import com.aestroon.common.components.mockProvider.sampleUpcomingTransactions
import com.aestroon.common.theme.OrangeChipColor
import com.aestroon.common.theme.RedChipColor
import com.aestroon.common.utilities.TextFormatter
import com.aestroon.home.dailyTransactions.dailyTransactionItems
import com.aestroon.home.mockProvider.GOAL
import com.aestroon.home.mockProvider.TOTAL_BALANCE
import com.aestroon.home.mockProvider.sampleExchangeRates
import com.aestroon.home.widgets.balanceOverview.BalanceOverviewCard
import com.aestroon.home.widgets.exchangeRow.ExpandableExchangeRateWidgetCard
import com.aestroon.home.widgets.savingSummary.SavingsSummaryCard

fun LazyListScope.addHomeScreenContent(
    groupedTransactions:  List<TransactionItemData>,
){
    item(key = "home_screen_total_balance_overview") {
        BalanceOverviewCard(
            totalBalance = TextFormatter.toPrettyAmountWithCurrency(
                amount = TOTAL_BALANCE,
                currencyPosition = TextFormatter.CurrencyPosition.AFTER,
                currency = MOCK_BASE_CURRENCY,
            ),
            amountUntilGoal = TextFormatter.toPrettyAmountWithCurrency(
                amount = GOAL - TOTAL_BALANCE,
                currencyPosition = TextFormatter.CurrencyPosition.AFTER,
                currency = MOCK_BASE_CURRENCY,
            ),
            goalAmountValue = GOAL.toFloat(),
            goalProgress = (1 - (GOAL - TOTAL_BALANCE) / GOAL).toFloat(),
            statusMessage = "This is a status message",
            statusIcon = Icons.Default.Star,
        )
    }

    item(key = "home_screen_savings_summary") {
        SavingsSummaryCard(
            income = TextFormatter.toPrettyAmountWithCurrency(12300.42, MOCK_BASE_CURRENCY, false, TextFormatter.CurrencyPosition.AFTER),
            expense = TextFormatter.toPrettyAmountWithCurrency(845.72, MOCK_BASE_CURRENCY, false, TextFormatter.CurrencyPosition.AFTER),
            savingsGoalPercentage = 0.75f,
        )
    }

    item(key = "home_screen_exchange_rate_overview") {
        ExpandableExchangeRateWidgetCard(
            allExchangeRates = sampleExchangeRates,
            ratesForCollapsedView = sampleExchangeRates.take(3),
            initiallyExpanded = false
        )
    }

    item(key = "home_screen_upcoming_transactions") {
        CollapsibleSectionCard(
            title = "Upcoming",
            summary = {
                ExpandableTransactionHeader(
                    income = TextFormatter.toPrettyAmountWithCurrency(812.72, MOCK_BASE_CURRENCY, false, TextFormatter.CurrencyPosition.AFTER),
                    expense = TextFormatter.toPrettyAmountWithCurrency(362.1, MOCK_BASE_CURRENCY, false, TextFormatter.CurrencyPosition.AFTER),
                )
            },
            headerBackgroundColor = OrangeChipColor.copy(alpha = 0.3f),
            headerContentColor = MaterialTheme.colorScheme.onBackground,
            initiallyExpanded = false,
        ) {
            sampleUpcomingTransactions.forEach {
                TransactionListItem(Modifier.padding(bottom = 8.dp), it)
            }
        }
    }

    item(key = "home_screen_overdue_transactions") {
        CollapsibleSectionCard(
            title = "Overdue",
            summary = {
                ExpandableTransactionHeader(
                    income = TextFormatter.toPrettyAmountWithCurrency(8120.72, MOCK_BASE_CURRENCY, false, TextFormatter.CurrencyPosition.AFTER),
                    expense = TextFormatter.toPrettyAmountWithCurrency(2382.12, MOCK_BASE_CURRENCY, false, TextFormatter.CurrencyPosition.AFTER),
                )
            },
            headerBackgroundColor = RedChipColor.copy(alpha = .3f),
            headerContentColor = MaterialTheme.colorScheme.onBackground,
            initiallyExpanded = false,
        ) {
            sampleOverdueTransactions.forEach {
                TransactionListItem(Modifier.padding(bottom = 8.dp), it)
            }

            if (sampleOverdueTransactions.isEmpty()) {
                Text(
                    "No overdue items.",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }

    dailyTransactionItems(
        transactions = groupedTransactions,
        onTransactionClick = { id -> /* handle click */ }
    )
}