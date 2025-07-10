package com.aestroon.home.news.domain

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.domain.CategoriesViewModel
import com.aestroon.common.domain.PlannedPaymentsViewModel
import com.aestroon.common.domain.TransactionsViewModel
import com.aestroon.common.domain.WalletsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val walletsViewModel: WalletsViewModel,
    private val transactionsViewModel: TransactionsViewModel,
    private val categoriesViewModel: CategoriesViewModel,
    private val plannedPaymentsViewModel: PlannedPaymentsViewModel,
    private val newsViewModel: NewsViewModel
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.userIdFlow.filterNotNull().collect { userId ->
                Log.d("AppStartup", "HomeViewModel: User logged in with ID $userId. Triggering data refresh.")
                refreshAllData()
            }
        }
    }

    fun refreshAllData() {
        viewModelScope.launch {
            Log.d("AppStartup", "refreshAllData: Refresh triggered.")
            _isRefreshing.value = true

            walletsViewModel.onEnterScreen()
            categoriesViewModel.onEnterScreen()
            transactionsViewModel.syncTransactions()
            plannedPaymentsViewModel.syncPlannedPayments()
            newsViewModel.refresh()
            _isRefreshing.value = false
            Log.d("AppStartup", "refreshAllData: Refresh finished.")
        }
    }
}
