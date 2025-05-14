package com.aestroon.zenwallet

import com.aestroon.home.HomeScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aestroon.calendar.CalendarScreen
import com.aestroon.common.navigation.ScreenNavItems
import com.aestroon.common.navigation.AnimatedNavigationBar
import com.aestroon.common.navigation.ButtonData
import com.aestroon.common.theme.PrimaryColor
import com.aestroon.calendar.mockProvider.WalletsScreen
import com.aestroon.portfolio.PortfolioOverviewScreen
import com.aestroon.profile.ProfileScreen

@Composable
fun AuthenticatedNavGraph() {
    val navController = rememberNavController()

    val buttons = listOf(
        ButtonData("Wallets", Icons.Default.Wallet),
        ButtonData("Portfolio", Icons.Default.StackedLineChart),
        ButtonData("Home", Icons.Default.Home),
        ButtonData("Calendar", Icons.Default.DateRange),
        ButtonData("Settings", Icons.Default.Settings),
    )

    Scaffold(
        bottomBar = {
            AnimatedNavigationBar(
                buttons = buttons,
                barColor = MaterialTheme.colorScheme.onPrimary,
                circleColor = MaterialTheme.colorScheme.onPrimary,
                selectedColor = PrimaryColor,
                unselectedColor = Color.Gray,
                onItemClick = { index ->
                    val route = when (index) {
                        0 -> ScreenNavItems.Wallets.route
                        1 -> ScreenNavItems.Portfolio.route
                        2 -> ScreenNavItems.Home.route
                        3 -> ScreenNavItems.Calendar.route
                        4 -> ScreenNavItems.Settings.route
                        else -> ScreenNavItems.Home.route
                    }
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ScreenNavItems.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(ScreenNavItems.Home.route) { HomeScreen() }
            composable(ScreenNavItems.Wallets.route) { WalletsScreen() }
            composable(ScreenNavItems.Portfolio.route) { PortfolioOverviewScreen() }
            composable(ScreenNavItems.Calendar.route) { CalendarScreen() }
            composable(ScreenNavItems.Settings.route) { ProfileScreen() }
        }
    }
}
