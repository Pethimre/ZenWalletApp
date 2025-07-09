package com.aestroon.home.dashboardScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aestroon.common.domain.DashboardViewModel
import com.aestroon.common.theme.GreenChipColor
import com.aestroon.common.theme.RedChipColor
import com.aestroon.common.utilities.TextFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val stats by viewModel.stats.collectAsState()

    val shortcuts = listOf(
        Shortcut("Categories", Icons.Default.Category, "categories"),
        Shortcut("Planned", Icons.Default.Event, "planned_payments"),
        Shortcut("Loans", Icons.Default.MonetizationOn, "loans"),
        Shortcut("Goals", Icons.Default.TrackChanges, "saving_goals")
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuickStatsCard(
            netWorth = stats.netWorth,
            savingsRate = stats.savingsRate,
            thisMonthCashFlow = stats.thisMonthCashFlow,
            baseCurrency = stats.baseCurrency
        )

        Text("Shortcuts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(shortcuts) { shortcut ->
                ShortcutButton(
                    title = shortcut.title,
                    icon = shortcut.icon,
                    onClick = { navController.navigate(shortcut.route) }
                )
            }
        }
    }
}

@Composable
fun QuickStatsCard(
    netWorth: Double,
    savingsRate: Float,
    thisMonthCashFlow: Double,
    baseCurrency: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem("Net Worth", TextFormatter.toPrettyAmountWithCurrency(netWorth, baseCurrency))
            StatItem("Savings Rate", String.format("%.0f%%", savingsRate * 100), if (savingsRate >= 0) GreenChipColor else RedChipColor)
            StatItem("This Month", TextFormatter.toPrettyAmountWithCurrency(thisMonthCashFlow, baseCurrency, withSign = true))
        }
    }
}
