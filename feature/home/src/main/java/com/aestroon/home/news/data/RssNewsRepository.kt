package com.aestroon.home.news.data

import com.aestroon.home.mockProvider.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RssNewsRepository {

    private val rssUrl = "https://ir.thomsonreuters.com/rss/news-releases.xml?items=15"

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
            val currentCategories = mutableListOf<String>()

            val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name

                        // Handle media content or enclosure for image
                        if (currentTag == "media:content" || currentTag == "enclosure") {
                            val urlAttr = parser.getAttributeValue(null, "url")
                            if (urlAttr != null && (urlAttr.endsWith(".jpg") || urlAttr.contains("image"))) {
                                currentImageUrl = urlAttr
                            }
                        }
                    }

                    XmlPullParser.TEXT -> {
                        when (currentTag) {
                            "title" -> currentTitle = parser.text
                            "description" -> currentDescription = parser.text
                            "link" -> currentLink = parser.text
                            "pubDate" -> currentPubDateRaw = parser.text
                            "category" -> currentCategories.add(parser.text)
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item") {
                            val parsedDate: Date? = try {
                                dateFormat.parse(currentPubDateRaw)
                            } catch (e: Exception) {
                                null
                            }

                            articles.add(
                                NewsArticle(
                                    id = currentLink.hashCode().toString(),
                                    title = currentTitle,
                                    description = currentDescription,
                                    link = currentLink,
                                    imageUrl = currentImageUrl,
                                    pubDate = parsedDate,
                                    sourceName = "Reuters",
                                    categories = currentCategories.toList()
                                )
                            )

                            // Reset for next item
                            currentTitle = ""
                            currentDescription = null
                            currentLink = ""
                            currentPubDateRaw = ""
                            currentImageUrl = null
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
