package com.aestroon.common.presentation.screen.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.aestroon.common.presentation.screen.NewsArticle
import com.aestroon.common.presentation.screen.createMockNewsArticles
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BoxScope.ArticleContentCard(
    article: NewsArticle,
) {
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = 180.dp)
            .align(alignment = Alignment.TopCenter)
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = article.sourceName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = .2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, article.link.toUri())
                            context.startActivity(intent)
                        },
                )
                IconButton(onClick = {
                    clipboardManager.setText(AnnotatedString(article.link))
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Link copied to clipboard")
                    }
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, article.link.toUri())
                        context.startActivity(intent)
                    }) {
                    Icon(
                        imageVector = Icons.Default.Newspaper,
                        contentDescription = "Open in browser",
                    )
                }
            }
            Text(
                text = SimpleDateFormat("h:mm a - EEEE", Locale.ENGLISH)
                    .format(article.pubDate ?: Date()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(12.dp))

        article.description?.let {
            Text(
                text = it,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp),
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true, name = "Article Content - Dark")
@Composable
fun NewsScreenPreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Box {
            ArticleContentCard(
                article = createMockNewsArticles(1).first(),
            )
        }
    }
}

@Preview(showBackground = true, name = "Article Content - Light")
@Composable
fun NewsScreenPreviewLight() {
    MaterialTheme {
        Box {
            ArticleContentCard(
                article = createMockNewsArticles(1).first(),
            )
        }
    }
}