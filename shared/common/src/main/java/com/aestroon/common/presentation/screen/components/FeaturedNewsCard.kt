package com.aestroon.common.presentation.screen.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aestroon.common.presentation.screen.NewsArticle
import com.aestroon.common.presentation.screen.createMockNewsArticles
import com.aestroon.common.utilities.TextFormatter.formatNewsTimestamp

@Composable
fun FeaturedNewsCard(
    article: NewsArticle,
    onClick: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(article) },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column {
            ArticleImage(article)
            Column(modifier = Modifier.padding(16.dp)) {
                if (article.categories.isNotEmpty()) {
                    NewsCategoryChip(category = article.categories.first())
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                article.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatNewsTimestamp(article.pubDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row {
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(article.link))
                            Toast.makeText(context, "Link copied to clipboard" , Toast.LENGTH_LONG).show()
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Article Content - Dark")
@Composable
fun FeaturedNewsCardPreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        FeaturedNewsCard(article = createMockNewsArticles(1).first(), onClick = {})
    }
}

@Preview(showBackground = true, name = "Article Content - Light")
@Composable
fun FeaturedNewsCardPreviewLight() {
    MaterialTheme {
        FeaturedNewsCard(article = createMockNewsArticles(1).first(), onClick = {})
    }
}
