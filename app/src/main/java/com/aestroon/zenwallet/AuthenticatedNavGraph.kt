package com.aestroon.zenwallet

import com.aestroon.home.HomeMainScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.aestroon.home.news.domain.NewsViewModel
import com.aestroon.home.news.ui.NewsDetailErrorScreen
import com.aestroon.home.news.ui.NewsDetailScreen
import com.aestroon.home.widgets.HomeScreenType
import com.aestroon.portfolio.PortfolioOverviewScreen
import com.aestroon.profile.domain.ProfileViewModel
import com.aestroon.profile.presentation.ProfileScreen
import com.aestroon.wallets.WalletsScreen
import org.koin.androidx.compose.getViewModel

@Composable
fun AuthenticatedNavGraph(onLogoutClicked: () -> Unit) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedTab = rememberSaveable { mutableStateOf(HomeScreenType.OVERVIEW) }
    val newsViewModel: NewsViewModel = getViewModel()
    val profileViewModel: ProfileViewModel = getViewModel()

    val buttons = listOf(
        ButtonData("Wallets", Icons.Default.Wallet),
        ButtonData("Portfolio", Icons.Default.StackedLineChart),
        ButtonData("Home", Icons.Default.Home),
        ButtonData("Calendar", Icons.Default.DateRange),
        ButtonData("Settings", Icons.Default.Settings),
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedNavigationBar(
                buttons = buttons,
                barColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .6f),
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
            composable(ScreenNavItems.Wallets.route) { WalletsScreen() }
            composable(ScreenNavItems.Portfolio.route) { PortfolioOverviewScreen() }
            composable(ScreenNavItems.Calendar.route) { CalendarScreen() }
            composable(ScreenNavItems.Settings.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogoutClicked = onLogoutClicked
                )
            }

            composable(ScreenNavItems.Home.route) {
                HomeMainScreen(
                    viewModel = newsViewModel,
                    selectedHomeScreenType = selectedTab.value,
                    onTabSelected = { selectedTab.value = it },
                    onArticleClick = { articleId ->
                        navController.navigate("news_detail/$articleId")
                    }
                )
            }

            composable("news_detail/{articleId}") { backStackEntry ->
                val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
                val article = newsViewModel.findArticleById(articleId)

                if (article != null) {
                    NewsDetailScreen(article = article, onBackClick = {
                        navController.popBackStack()
                    })
                } else {
                    NewsDetailErrorScreen(onBackClick = {
                        navController.popBackStack()
                    })
                }
            }
        }
    }
}
