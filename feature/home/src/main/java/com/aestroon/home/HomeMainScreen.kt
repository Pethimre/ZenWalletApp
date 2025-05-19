package com.aestroon.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aestroon.home.homeScreen.addHomeScreenContent
import com.aestroon.home.mockProvider.comprehensivePreviewTransactions
import com.aestroon.home.news.domain.NewsViewModel
import com.aestroon.home.news.ui.addNewsScreenContent
import com.aestroon.home.widgets.HomeScreenType
import com.aestroon.home.widgets.SegmentedControlHomeTabs

@Composable
fun HomeMainScreen() {
    val groupedTransactions = remember { comprehensivePreviewTransactions }
    var selectedHomeScreenType by remember { mutableStateOf(HomeScreenType.OVERVIEW) }

    val viewModel: NewsViewModel? = if (selectedHomeScreenType == HomeScreenType.NEWS) {
        org.koin.androidx.compose.getViewModel()
    } else {
        null
    }

    val articles by viewModel?.news?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val isLoading by viewModel?.loading?.collectAsState() ?: remember { mutableStateOf(false) }

    LazyColumn {
        item(key = "home_screen_spacer") { Spacer(Modifier.height(20.dp)) }

        item(key = "home_screen_segmented_control_tab_selector") {
            SegmentedControlHomeTabs(
                tabs = HomeScreenType.entries.toList(),
                selectedTab = selectedHomeScreenType,
                onTabSelected = { selectedHomeScreenType = it },
            )
        }

        when (selectedHomeScreenType) {
            HomeScreenType.OVERVIEW -> addHomeScreenContent(groupedTransactions)
            HomeScreenType.NEWS -> addNewsScreenContent(
                newsArticles = articles,
                isLoading = isLoading,
                onArticleClick = { /* handle click */ },
                onRefresh = { viewModel?.refresh() ?: Unit }
            )
        }
    }
}
