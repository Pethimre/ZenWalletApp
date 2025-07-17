package com.aestroon.common.data.repository

import com.aestroon.common.presentation.screen.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RssNewsRepository {

    private val rssUrl = "https://feeds.npr.org/1001/rss.xml"

    suspend fun fetchNews(): List<NewsArticle> = withContext(Dispatchers.IO) {
        val articles = mutableListOf<NewsArticle>()

        try {
            val url = URL(rssUrl)
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(url.openConnection().getInputStream(), null)

            var eventType = parser.eventType
            var currentTag = ""
            var currentTitle = ""
            var currentDescription: String? = null
            var currentLink = ""
            var currentPubDateRaw = ""
            var currentImageUrl: String? = null
            var currentContentEncoded: String? = null
            val currentCategories = mutableListOf<String>()

            val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                    }

                    XmlPullParser.TEXT -> {
                        when (currentTag) {
                            "title" -> currentTitle = parser.text
                            "description" -> currentDescription = parser.text
                            "link" -> currentLink = parser.text
                            "pubDate" -> currentPubDateRaw = parser.text
                            "category" -> currentCategories.add(parser.text)
                            "content:encoded" -> currentContentEncoded = parser.text
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item") {
                            val parsedDate: Date? = try {
                                dateFormat.parse(currentPubDateRaw)
                            } catch (e: Exception) {
                                null
                            }

                            // Try to extract image from content:encoded with Jsoup
                            if (currentImageUrl == null && currentContentEncoded != null) {
                                val doc = Jsoup.parse(currentContentEncoded)
                                val img = doc.selectFirst("img")
                                currentImageUrl = img?.attr("src")
                            }

                            articles.add(
                                NewsArticle(
                                    id = currentLink.hashCode().toString(),
                                    title = currentTitle,
                                    description = currentDescription,
                                    link = currentLink,
                                    imageUrl = currentImageUrl,
                                    pubDate = parsedDate,
                                    sourceName = "NPR",
                                    categories = currentCategories.toList()
                                )
                            )

                            // Reset fields
                            currentTitle = ""
                            currentDescription = null
                            currentLink = ""
                            currentPubDateRaw = ""
                            currentImageUrl = null
                            currentContentEncoded = null
                            currentCategories.clear()
                        }

                        currentTag = ""
                    }
                }

                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        articles
    }
}