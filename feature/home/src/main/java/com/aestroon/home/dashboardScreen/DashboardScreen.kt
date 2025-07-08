package com.aestroon.home.dashboardScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aestroon.common.navigation.ScreenNavItems

@Composable
fun DashboardScreen(navController: NavController) {
    val shortcuts = listOf(
        Shortcut("Categories", Icons.Default.Category, ScreenNavItems.Categories.route),
        Shortcut("Planned", Icons.Default.Event, ScreenNavItems.PlannedPayments.route),
        Shortcut("Loans", Icons.Default.MonetizationOn, ScreenNavItems.Loans.route),
        Shortcut("Goals", Icons.Default.TrackChanges, ScreenNavItems.SavingGoals.route)
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuickStatsCard()

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
