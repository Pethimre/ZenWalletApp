package com.aestroon.home.news.ui.component

import android.R.drawable.ic_menu_gallery
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aestroon.home.mockProvider.NewsArticle

@Composable
fun ArticleImage(article: NewsArticle) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(article.imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = article.title,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentScale = ContentScale.Crop,
        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
        error = painterResource(id = ic_menu_gallery)
    )
}
