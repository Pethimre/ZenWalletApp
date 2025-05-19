package com.aestroon.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aestroon.home.homeScreen.addHomeScreenContent
import com.aestroon.home.mockProvider.comprehensivePreviewTransactions
import com.aestroon.home.mockProvider.createMockNewsArticles
import com.aestroon.home.news.addNewsScreenContent
import com.aestroon.home.widgets.HomeScreenType
import com.aestroon.home.widgets.SegmentedControlHomeTabs

@Composable
fun HomeMainScreen() {
    val groupedTransactions = remember { comprehensivePreviewTransactions }
    var selectedHomeScreenType by remember { mutableStateOf(HomeScreenType.OVERVIEW) }

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
                newsArticles = createMockNewsArticles(10),
                isLoading = false,
                onArticleClick = {},
                onRefresh = {}
            )
        }
    }
}
