package com.aestroon.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    viewModel: NewsViewModel = getViewModel()
) {
    val groupedTransactions = remember { comprehensivePreviewTransactions }
    val articles by viewModel.news.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNews()
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(Modifier.height(20.dp)) }

        item {
            SegmentedControlHomeTabs(
                tabs = HomeScreenType.entries.toList(),
                selectedTab = selectedHomeScreenType,
                onTabSelected = onTabSelected
            )
        }

        when (selectedHomeScreenType) {
            HomeScreenType.OVERVIEW -> addHomeScreenContent(groupedTransactions)
            HomeScreenType.NEWS -> addNewsScreenContent(
                newsArticles = articles,
                isLoading = isLoading,
                onArticleClick = { article -> onArticleClick(article.id) },
                onRefresh = viewModel::refresh,
            )
        }
    }
}
