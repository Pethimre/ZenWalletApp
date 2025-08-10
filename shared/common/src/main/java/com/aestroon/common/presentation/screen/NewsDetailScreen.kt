package com.aestroon.common.presentation.screen

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
import com.aestroon.common.R
import com.aestroon.common.presentation.screen.components.ArticleContentCard
import java.util.Date
import kotlin.random.Random

data class NewsArticle(
    val id: String,
    val title: String,
    val description: String?,
    val link: String,
    val imageUrl: String?,
    val pubDate: Date?,
    val sourceName: String,
    val categories: List<String> = emptyList()
)

fun createMockNewsArticles(count: Int): List<NewsArticle> {
    val titles = listOf(
        "Global Markets Rally on Positive Economic Outlook",
        "Tech Giants Unveil Next-Gen AI Innovations",
        "Central Bank Hints at Interest Rate Adjustments",
        "Renewable Energy Sector Sees Record Investment",
        "Startup Ecosystem Thrives Amidst New Funding Rounds",
        "Retail Sales Surge as Consumer Confidence Grows",
        "Supply Chain Disruptions Ease, Boosting Manufacturing",
        "Agricultural Commodities Prices Stabilize After Volatility",
        "Real Estate Market Shows Signs of Cooling Down",
        "Travel Industry Rebounds with Increased Bookings"
    )
    val descriptions = listOf(
        "Stock markets worldwide experienced a significant upswing today, driven by optimistic forecasts for global economic recovery and strong corporate earnings reports.",
        "Leading technology companies showcased groundbreaking advancements in artificial intelligence, promising to revolutionize various industries from healthcare to autonomous driving.",
        "In a recent address, the central bank governor suggested potential shifts in monetary policy, closely monitoring inflation data and employment figures.",
        "Investments in solar, wind, and other renewable energy sources have reached an all-time high this quarter, signaling a major shift towards sustainable power.",
        "Venture capital funding for startups has surged, particularly in fintech and biotech sectors, fostering innovation and job creation.",
        "Latest figures indicate a robust increase in retail sales, reflecting heightened consumer confidence and spending power.",
        "Improvements in global logistics and a reduction in shipping backlogs are providing much-needed relief to manufacturers, leading to increased output.",
        "After a period of fluctuation, prices for key agricultural commodities like wheat and corn have found a new equilibrium.",
        "Recent data suggests a moderation in housing price growth and sales activity, indicating a potential cooldown in the previously heated real estate sector.",
        "Airlines and hotels are reporting a significant uptick in reservations as international travel restrictions are eased and pent-up demand is unleashed."
    )
    val imageBaseUrl = "https://placehold.co/600x400/"
    val categories = listOf("Business", "Technology", "Finance", "Economy", "Markets", "World News")
    val sources = listOf("Reuters", "Bloomberg", "Associated Press", "Financial Times")

    return List(count) { i ->
        val pubDate = Date(System.currentTimeMillis() - Random.nextLong(1000L * 60 * 60 * 24 * 7)) // Within last 7 days
        NewsArticle(
            id = "news_${i + 1}",
            title = titles[i % titles.size],
            description = descriptions[i % descriptions.size],
            link = "https://www.reuters.com/business/",
            imageUrl = "${imageBaseUrl}EBF0F1/grey?text=News+${i+1}\\n${titles[i % titles.size].substring(0,10)}...",
            pubDate = pubDate,
            sourceName = sources[i % sources.size],
            categories = listOf(categories[i % categories.size], categories[(i+1) % categories.size]).distinct()
        )
    }
}

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