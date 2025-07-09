package com.aestroon.zenwallet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aestroon.calendar.CalendarScreen
import com.aestroon.common.domain.PlannedPaymentsViewModel
import com.aestroon.common.domain.TransactionsViewModel
import com.aestroon.common.domain.WalletsViewModel
import com.aestroon.common.navigation.AnimatedNavigationBar
import com.aestroon.common.navigation.ButtonData
import com.aestroon.common.navigation.ScreenNavItems
import com.aestroon.common.presentation.AddEditTransactionSheet
import com.aestroon.common.presentation.screen.PlannedPaymentsScreen
import com.aestroon.home.HomeMainScreen
import com.aestroon.home.news.domain.HomeViewModel
import com.aestroon.home.news.domain.NewsViewModel
import com.aestroon.home.news.ui.NewsDetailErrorScreen
import com.aestroon.home.news.ui.NewsDetailScreen
import com.aestroon.home.widgets.HomeScreenType
import com.aestroon.portfolio.PortfolioOverviewScreen
import com.aestroon.common.domain.ProfileViewModel
import com.aestroon.profile.presentation.CurrencySelectionScreen
import com.aestroon.profile.presentation.ProfileScreen
import com.aestroon.wallets.presentation.CategoriesScreen
import com.aestroon.wallets.presentation.WalletsScreen
import org.koin.androidx.compose.getViewModel

@Composable
fun AuthenticatedNavGraph(onLogoutClicked: () -> Unit) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val newsViewModel: NewsViewModel = getViewModel()
    val profileViewModel: ProfileViewModel = getViewModel()
    val transactionsViewModel: TransactionsViewModel = getViewModel()
    val walletsViewModel: WalletsViewModel = getViewModel()
    val homeViewModel: HomeViewModel = getViewModel()
    val plannedPaymentsViewModel: PlannedPaymentsViewModel = getViewModel()

    var selectedIndex by remember { mutableStateOf(2) }
    var showAddTransactionSheet by remember { mutableStateOf(false) }

    val wallets by transactionsViewModel.wallets.collectAsState()
    val categories by transactionsViewModel.categories.collectAsState()

    val buttons = remember(selectedIndex) {
        listOf(
            ButtonData("Wallets", Icons.Default.Wallet),
            ButtonData("Portfolio", Icons.Default.StackedLineChart),
            ButtonData("Home", if (selectedIndex == 2) Icons.Default.Add else Icons.Default.Home),
            ButtonData("Calendar", Icons.Default.DateRange),
            ButtonData("Settings", Icons.Default.Settings),
        )
    }

    if (showAddTransactionSheet) {
        AddEditTransactionSheet(
            wallets = wallets,
            categories = categories,
            onDismiss = { showAddTransactionSheet = false },
            onConfirm = { amount, name, description, date, fromWallet, category, type, toWallet ->
                transactionsViewModel.addTransaction(amount, name, description, date, fromWallet, category, type, toWallet)
                showAddTransactionSheet = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedNavigationBar(
                selectedIndex = selectedIndex,
                onSelectedIndexChange = { index ->
                    selectedIndex = index
                    val route = when (index) {
                        0 -> ScreenNavItems.Wallets.route
                        1 -> ScreenNavItems.Portfolio.route
                        2 -> ScreenNavItems.Home.route
                        3 -> ScreenNavItems.Calendar.route
                        4 -> ScreenNavItems.Settings.route
                        else -> ScreenNavItems.Home.route
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onCircleClick = {
                    if (selectedIndex == 2) {
                        showAddTransactionSheet = true
                    }
                },
                buttons = buttons,
                barColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .9f),
                circleColor = MaterialTheme.colorScheme.primary,
                selectedColor = MaterialTheme.colorScheme.onPrimary,
                unselectedColor = Color.Gray,
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ScreenNavItems.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(ScreenNavItems.Portfolio.route) { PortfolioOverviewScreen() }
            composable(ScreenNavItems.Calendar.route) { CalendarScreen() }
            composable(ScreenNavItems.Settings.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogoutClicked = onLogoutClicked,
                    onNavigateToCurrencySelection = {
                        navController.navigate(ScreenNavItems.CurrencySelection.route)
                    },
                )
            }
            composable(ScreenNavItems.CurrencySelection.route) {
                CurrencySelectionScreen(viewModel = profileViewModel, onNavigateUp = { navController.navigateUp() })
            }
            composable(ScreenNavItems.Home.route) {
                var selectedTab by remember { mutableStateOf(HomeScreenType.OVERVIEW) }
                HomeMainScreen(
                    selectedHomeScreenType = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onArticleClick = { articleId -> navController.navigate("news_detail/$articleId") },
                    navController = navController,
                    homeViewModel = homeViewModel,
                    newsViewModel = newsViewModel,
                    transactionsViewModel = transactionsViewModel,
                    walletsViewModel = walletsViewModel,
                    plannedPaymentsViewModel = plannedPaymentsViewModel,
                    profileViewModel = profileViewModel,
                )
            }
            composable("news_detail/{articleId}") { backStackEntry ->
                val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
                val article = newsViewModel.findArticleById(articleId)
                if (article != null) {
                    NewsDetailScreen(article = article, onBackClick = { navController.popBackStack() })
                } else {
                    NewsDetailErrorScreen(onBackClick = { navController.popBackStack() })
                }
            }

            composable(ScreenNavItems.Wallets.route) {
                WalletsScreen()
            }
            composable(ScreenNavItems.Categories.route) {
                CategoriesScreen(onNavigateUp = { navController.popBackStack() })
            }
            composable(ScreenNavItems.PlannedPayments.route) {
                PlannedPaymentsScreen(navController = navController)
            }
            composable(ScreenNavItems.Loans.route) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loans Screen")
                }
            }
            composable(ScreenNavItems.SavingGoals.route) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Saving Goals Screen")
                }
            }
            composable(ScreenNavItems.PlannedPayments.route) {
                PlannedPaymentsScreen(navController = navController)
            }
        }
    }
}
