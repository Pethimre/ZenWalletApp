package com.aestroon.home.news.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.domain.CategoriesViewModel
import com.aestroon.common.domain.TransactionsViewModel
import com.aestroon.common.domain.WalletsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val walletsViewModel: WalletsViewModel,
    private val transactionsViewModel: TransactionsViewModel,
    private val categoriesViewModel: CategoriesViewModel,
    private val newsViewModel: NewsViewModel
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun refreshAllData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // These functions already use the reactive userIdFlow,
            // so they will correctly fetch data for the logged-in user.
            walletsViewModel.onEnterScreen()
            categoriesViewModel.onEnterScreen()
            // We need a public sync function in TransactionsViewModel
            transactionsViewModel.syncTransactions()
            newsViewModel.refresh()
            _isRefreshing.value = false
        }
    }
}
