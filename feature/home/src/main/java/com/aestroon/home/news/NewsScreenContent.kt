package com.aestroon.home.news

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aestroon.home.mockProvider.NewsArticle
import com.aestroon.home.mockProvider.createMockNewsArticles

fun LazyListScope.addNewsScreenContent(
    // In a real app, this would be a StateFlow from a ViewModel
    newsArticles: List<NewsArticle>,
    isLoading: Boolean,
    onArticleClick: (NewsArticle) -> Unit,
    onRefresh: () -> Unit,
) {
    if (isLoading && newsArticles.isEmpty()) {
        item(key = "news_screen_loading") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    } else if (!isLoading && newsArticles.isEmpty()) {
        item(key = "news_screen_empty") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.ErrorOutline,
                        contentDescription = "No news",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No news articles found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRefresh) { Text("Try Again") }
                }
            }
        }
    } else {
        item(key = "featured_${newsArticles.first().id}") {
            Box(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                FeaturedNewsCard(
                    article = newsArticles.first(),
                    onClick = onArticleClick
                )
            }
        }
        items(newsArticles.drop(1), key = { "article_${it.id}" }) { article ->
            Box(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                NewsArticleCard(article = article, onClick = onArticleClick)
            }
        }
    }
}

@Preview(showBackground = true, name = "News Screen - Light")
@Composable
fun NewsScreenPreviewLight() {
    MaterialTheme {
        LazyColumn {
            addNewsScreenContent(
                newsArticles = createMockNewsArticles(10),
                isLoading = false,
                onArticleClick = {},
                onRefresh = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "News Screen - Dark")
@Composable
fun NewsScreenPreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        LazyColumn {
            addNewsScreenContent(
                newsArticles = createMockNewsArticles(10),
                isLoading = false,
                onArticleClick = {},
                onRefresh = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "News Screen - Loading")
@Composable
fun NewsScreenLoadingPreview() {
    MaterialTheme {
        LazyColumn {
            addNewsScreenContent(
                newsArticles = emptyList(),
                isLoading = true,
                onArticleClick = {},
                onRefresh = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "News Screen - Empty")
@Composable
fun NewsScreenEmptyPreview() {
    MaterialTheme {
        LazyColumn {
            addNewsScreenContent(
                newsArticles = emptyList(),
                isLoading = false,
                onArticleClick = {},
                onRefresh = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Featured News Card")
@Composable
fun FeaturedNewsCardPreview() {
    MaterialTheme {
        FeaturedNewsCard(
            article = createMockNewsArticles(1).first(),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "News Article Card")
@Composable
fun NewsArticleCardPreview() {
    MaterialTheme {
        NewsArticleCard(
            article = createMockNewsArticles(1).first(),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
