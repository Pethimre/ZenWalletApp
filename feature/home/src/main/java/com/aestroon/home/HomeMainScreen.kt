package com.aestroon.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aestroon.common.domain.TransactionsViewModel
import com.aestroon.common.domain.WalletsViewModel
import com.aestroon.common.presentation.AddEditTransactionSheet
import com.aestroon.home.homeScreen.addHomeScreenContent
import com.aestroon.home.mockProvider.comprehensivePreviewTransactions
import com.aestroon.home.news.domain.NewsViewModel
import com.aestroon.home.news.ui.addNewsScreenContent
import com.aestroon.home.widgets.HomeScreenType
import com.aestroon.home.widgets.SegmentedControlHomeTabs
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeMainScreen(
    selectedHomeScreenType: HomeScreenType,
    onTabSelected: (HomeScreenType) -> Unit,
    onArticleClick: (String) -> Unit,
    newsViewModel: NewsViewModel = getViewModel(),
    transactionsViewModel: TransactionsViewModel = getViewModel(),
    walletsViewModel: WalletsViewModel = getViewModel()
) {
    val transactions by transactionsViewModel.transactions.collectAsState()
    val articles by newsViewModel.news.collectAsState()
    val isLoadingNews by newsViewModel.loading.collectAsState()
    val summary by walletsViewModel.summary.collectAsState()

    LaunchedEffect(Unit) {
        newsViewModel.loadNews()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
                groupedTransactions = transactions,
                summary = summary,
                onTransactionClick = { /* TODO */ }
            )
            HomeScreenType.NEWS -> addNewsScreenContent(
                newsArticles = articles,
                isLoading = isLoadingNews,
                onArticleClick = { article -> onArticleClick(article.id) },
                onRefresh = newsViewModel::refresh,
            )
        }
    }
}