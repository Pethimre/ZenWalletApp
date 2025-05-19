package com.aestroon.home.mockProvider

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
    val imageBaseUrl = "https://placehold.co/600x400/" // Using a placeholder image service
    val categories = listOf("Business", "Technology", "Finance", "Economy", "Markets", "World News")
    val sources = listOf("Reuters", "Bloomberg", "Associated Press", "Financial Times")

    return List(count) { i ->
        val pubDate = Date(System.currentTimeMillis() - Random.nextLong(1000L * 60 * 60 * 24 * 7)) // Within last 7 days
        NewsArticle(
            id = "news_${i + 1}",
            title = titles[i % titles.size],
            description = descriptions[i % descriptions.size],
            link = "https://www.reuters.com/business/", // Placeholder link
            imageUrl = "${imageBaseUrl}EBF0F1/grey?text=News+${i+1}\\n${titles[i % titles.size].substring(0,10)}...",
            pubDate = pubDate,
            sourceName = sources[i % sources.size],
            categories = listOf(categories[i % categories.size], categories[(i+1) % categories.size]).distinct()
        )
    }
}
