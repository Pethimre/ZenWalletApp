package com.aestroon.common.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.repository.UserRepository
import com.aestroon.common.utilities.DEFAULT_BASE_CURRENCY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

data class NetWorthState(
    val currentNetWorth: Double = 0.0,
    val netWorthGoal: Long = 0L,
    val progress: Float = 0f,
    val currency: String = DEFAULT_BASE_CURRENCY
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val walletsViewModel: WalletsViewModel,
    private val portfolioViewModel: PortfolioViewModel
) : ViewModel() {

    val netWorthState: StateFlow<NetWorthState> = authRepository.userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            val userProfileFlow = flow {
                val profileResult = userRepository.getUserProfile(userId)
                emit(profileResult.getOrNull())
            }

            combine(
                userProfileFlow,
                walletsViewModel.summary,
                portfolioViewModel.overallSummary
            ) { userProfile, walletSummary, portfolioSummary ->

                if (userProfile == null) {
                    return@combine NetWorthState()
                }

                val totalWalletValue = walletSummary.totalBalance / 100.0

                val totalPortfolioValue = portfolioSummary.totalPortfolioValue

                val currentNetWorth = totalWalletValue + totalPortfolioValue
                val goal = userProfile.worth_goal
                val baseCurrency = userProfile.base_currency
                val progress = if (goal > 0) (currentNetWorth / goal.toDouble()).toFloat().coerceAtMost(1f) else 0f

                NetWorthState(
                    currentNetWorth = currentNetWorth,
                    netWorthGoal = goal,
                    progress = progress,
                    currency = baseCurrency
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NetWorthState())
}
