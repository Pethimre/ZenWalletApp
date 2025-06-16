package com.aestroon.home.news.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aestroon.home.R
import com.aestroon.home.mockProvider.NewsArticle
import com.aestroon.home.mockProvider.createMockNewsArticles
import com.aestroon.home.news.ui.component.ArticleContentCard

@Composable
fun NewsDetailScreen(
    article: NewsArticle,
    onBackClick: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val placeholderPainter: Painter = painterResource(id = R.drawable.newspaper)
        AsyncImage(
            model = article.imageUrl,
            contentDescription = article.title,
            contentScale = ContentScale.Inside,
            alignment = Alignment.TopCenter,
            placeholder = placeholderPainter,
            modifier = Modifier
                .fillMaxSize()
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.onBackground),
                        startY = 100f
                    )
                )
        )

        ArticleContentCard(article)

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.onBackground, CircleShape),
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.background.copy(alpha = .5f),
            )
        }
    }
}

@Preview(showBackground = true, name = "News Article Details Content - Dark")
@Composable
fun NewsDetailScreenPreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        NewsDetailScreen(article = createMockNewsArticles(1).first())
    }
}

@Preview(showBackground = true, name = "News Article Details Content - Light")
@Composable
fun NewsDetailScreenPreviewLight() {
    MaterialTheme {
        NewsDetailScreen(article = createMockNewsArticles(1).first())
    }
}