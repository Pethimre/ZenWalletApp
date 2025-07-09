package com.aestroon.home.dashboardScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aestroon.common.data.DashboardStats
import com.aestroon.common.navigation.ScreenNavItems
import com.aestroon.common.theme.GreenChipColor
import com.aestroon.common.utilities.TextFormatter

data class Shortcut(
    val title: String,
    val icon: ImageVector,
    val route: String
)

fun LazyListScope.addDashboardContent(
    navController: NavController,
    stats: DashboardStats,
) {
    item(key = "dashboard_quick_stats") {
        QuickStatsCard(
            netWorth = stats.netWorth,
            savingsRate = stats.savingsRate,
            thisMonthCashFlow = stats.thisMonthCashFlow,
            baseCurrency = stats.baseCurrency
        )
    }

    item(key = "dashboard_shortcuts_title") {
        Text(
            "Shortcuts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
    }

    val shortcuts = listOf(
        Shortcut("Categories", Icons.Default.Category, ScreenNavItems.Categories.route),
        Shortcut("Planned", Icons.Default.Event, ScreenNavItems.PlannedPayments.route),
        Shortcut("Loans", Icons.Default.MonetizationOn, ScreenNavItems.Loans.route),
        Shortcut("Goals", Icons.Default.TrackChanges, ScreenNavItems.SavingGoals.route)
    )

    val chunkedShortcuts = shortcuts.chunked(3)

    items(items = chunkedShortcuts, key = { row -> "shortcut_row_${row.first().title}" }) { row ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            row.forEach { shortcut ->
                Box(modifier = Modifier.weight(1f)) {
                    ShortcutButton(
                        title = shortcut.title,
                        icon = shortcut.icon,
                        onClick = { navController.navigate(shortcut.route) }
                    )
                }
            }
            repeat(3 - row.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ShortcutButton(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QuickStatsCard() {
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
            StatItem("Net Worth", "${TextFormatter.toPrettyAmount(12500.0)}K")
            StatItem("Savings Rate", "15%", GreenChipColor)
            StatItem("This Month", "-${TextFormatter.toPrettyAmount(550.0)}K")
        }
    }
}

@Composable
fun StatItem(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
