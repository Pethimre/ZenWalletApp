package com.aestroon.home.news.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.home.mockProvider.NewsArticle
import com.aestroon.home.news.data.RssNewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewsViewModel(
    private val repository: RssNewsRepository
) : ViewModel() {

    private val _news = MutableStateFlow<List<NewsArticle>>(emptyList())
    val news: StateFlow<List<NewsArticle>> = _news

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadNews() {
        viewModelScope.launch {
            _loading.value = true
            _news.value = repository.fetchNews()
            _loading.value = false
        }
    }

    fun refresh() = loadNews()

    fun findArticleById(id: String): NewsArticle? {
        return _news.value.find { it.id == id }
    }
}
