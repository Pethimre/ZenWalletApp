package com.aestroon.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aestroon.common.components.mockProvider.sampleOverdueTransactions
import com.aestroon.common.components.mockProvider.sampleUpcomingTransactions
import com.aestroon.common.domain.TransactionsViewModel
import com.aestroon.common.domain.WalletsViewModel
import com.aestroon.home.dashboardScreen.addDashboardContent
import com.aestroon.home.homeScreen.addHomeScreenContent
import com.aestroon.home.news.domain.HomeViewModel
import com.aestroon.home.news.domain.NewsViewModel
import com.aestroon.home.news.ui.addNewsScreenContent
import com.aestroon.home.widgets.HomeScreenType
import com.aestroon.home.widgets.SegmentedControlHomeTabs
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeMainScreen(
    selectedHomeScreenType: HomeScreenType,
    onTabSelected: (HomeScreenType) -> Unit,
    onArticleClick: (String) -> Unit,
    navController: NavController,
    homeViewModel: HomeViewModel = getViewModel(),
    newsViewModel: NewsViewModel = getViewModel(),
    transactionsViewModel: TransactionsViewModel = getViewModel(),
    walletsViewModel: WalletsViewModel = getViewModel()
) {
    val transactions by transactionsViewModel.transactions.collectAsState()
    val articles by newsViewModel.news.collectAsState()
    val isLoadingNews by newsViewModel.loading.collectAsState()
    val summary by walletsViewModel.summary.collectAsState()
    val categoriesMap by transactionsViewModel.categoriesMap.collectAsState()
    val isRefreshing by homeViewModel.isRefreshing.collectAsState()

    val pullRefreshState = rememberPullRefreshState(isRefreshing, { homeViewModel.refreshAllData() })

    LaunchedEffect(selectedHomeScreenType) {
        if (selectedHomeScreenType == HomeScreenType.NEWS && newsViewModel.news.value.isEmpty()) {
            newsViewModel.loadNews()
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            item {
                SegmentedControlHomeTabs(
                    tabs = HomeScreenType.entries.toList(),
                    selectedTab = selectedHomeScreenType,
                    onTabSelected = onTabSelected
                )
            }

            when (selectedHomeScreenType) {
                HomeScreenType.OVERVIEW -> addHomeScreenContent(
                    summary = summary,
                    dailyTransactions = transactions,
                    upcomingTransactions = sampleUpcomingTransactions,
                    overdueTransactions = sampleOverdueTransactions,
                    categoriesMap = categoriesMap,
                    onEdit = {},
                    onDelete = { transactionsViewModel.deleteTransaction(it) }
                )
                HomeScreenType.DASHBOARD -> addDashboardContent(navController = navController)
                HomeScreenType.NEWS -> addNewsScreenContent(
                    newsArticles = articles,
                    isLoading = isLoadingNews,
                    onArticleClick = { article -> onArticleClick(article.id) },
                    onRefresh = newsViewModel::refresh,
                )
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
