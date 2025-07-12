package com.aestroon.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aestroon.common.data.DashboardStats
import com.aestroon.common.data.entity.PlannedPaymentEntity
import com.aestroon.common.data.entity.TransactionEntity
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.model.CurrencyExchangeInfo
import com.aestroon.common.data.model.RateTrend
import com.aestroon.common.domain.DashboardViewModel
import com.aestroon.common.domain.PlannedPaymentsViewModel
import com.aestroon.common.domain.PortfolioViewModel
import com.aestroon.common.domain.ProfileViewModel
import com.aestroon.common.domain.TransactionsViewModel
import com.aestroon.common.domain.WalletsViewModel
import com.aestroon.common.presentation.AddEditTransactionSheet
import com.aestroon.home.dashboardScreen.addDashboardContent
import com.aestroon.home.homeScreen.addHomeScreenContent
import com.aestroon.home.news.domain.HomeViewModel
import com.aestroon.home.news.domain.NewsViewModel
import com.aestroon.home.news.ui.addNewsScreenContent
import com.aestroon.home.widgets.HomeScreenType
import com.aestroon.home.widgets.SegmentedControlHomeTabs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeMainScreen(
    selectedHomeScreenType: HomeScreenType,
    onTabSelected: (HomeScreenType) -> Unit,
    onArticleClick: (String) -> Unit,
    navController: NavController,
    homeViewModel: HomeViewModel,
    newsViewModel: NewsViewModel,
    transactionsViewModel: TransactionsViewModel,
    walletsViewModel: WalletsViewModel,
    plannedPaymentsViewModel: PlannedPaymentsViewModel,
    profileViewModel: ProfileViewModel,
    portfolioViewModel: PortfolioViewModel,
    dashboardViewModel: DashboardViewModel,
) {
    val summary by walletsViewModel.summary.collectAsState()
    val portfolioSummary by portfolioViewModel.overallSummary.collectAsState()
    val transactions by transactionsViewModel.transactions.collectAsState()
    val articles by newsViewModel.news.collectAsState()
    val isLoadingNews by newsViewModel.loading.collectAsState()
    val categoriesMap by transactionsViewModel.categoriesMap.collectAsState()
    val isRefreshing by homeViewModel.isRefreshing.collectAsState()
    val plannedPayments by plannedPaymentsViewModel.plannedPayments.collectAsState()
    val baseCurrency by transactionsViewModel.baseCurrency.collectAsState()
    val exchangeRates by transactionsViewModel.exchangeRates.collectAsState()
    val monthlyProgress by transactionsViewModel.monthlyProgress.collectAsState()
    val worthGoal by profileViewModel.savedWorthGoal.collectAsState()
    val worthGoalCurrency by profileViewModel.savedWorthGoalCurrency.collectAsState()
    val currentMonthIncome by transactionsViewModel.currentMonthIncome.collectAsState()
    val currentMonthExpense by transactionsViewModel.currentMonthExpense.collectAsState()
    val netWorthState by dashboardViewModel.netWorthState.collectAsState()
    val allExchangeRateInfos by homeViewModel.exchangeRateInfos.collectAsState()
    val collapsedExchangeRateInfos by homeViewModel.collapsedExchangeRateInfos.collectAsState()

    val transactionToEdit by transactionsViewModel.transactionToEdit.collectAsState()
    val wallets by walletsViewModel.wallets.collectAsState()
    val categories by transactionsViewModel.categories.collectAsState()

    val pullRefreshState = rememberPullRefreshState(isRefreshing, { homeViewModel.refreshAllData() })

    LaunchedEffect(Unit) {
        homeViewModel.refreshAllData()
    }

    LaunchedEffect(selectedHomeScreenType) {
        if (selectedHomeScreenType == HomeScreenType.NEWS && articles.isEmpty()) {
            newsViewModel.loadNews()
        }
    }

    val upcomingPayments = remember(plannedPayments) {
        val now = Calendar.getInstance()
        val twoWeeksFromNow = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, 2) }
        plannedPayments.filter {
            val dueDate = Calendar.getInstance().apply { time = it.dueDate }
            !dueDate.before(now) && (dueDate.before(twoWeeksFromNow) || dueDate.get(Calendar.MONTH) == now.get(
                Calendar.MONTH))
        }
    }

    val overduePayments = remember(plannedPayments) {
        val now = Calendar.getInstance()
        plannedPayments.filter {
            val dueDate = Calendar.getInstance().apply { time = it.dueDate }
            dueDate.before(now)
        }
    }

    transactionToEdit?.let { transaction ->
        AddEditTransactionSheet(
            existingTransaction = transaction,
            wallets = wallets,
            categories = categories,
            onDismiss = { transactionsViewModel.onEditTransactionDialogDismiss() },
            onConfirm = { amount, name, description, date, fromWallet, category, type, toWallet ->
                transactionsViewModel.onEditTransactionConfirm(
                    amountStr = (amount / 100.0).toString(),
                    name = name,
                    description = description ?: "",
                    category = category!!
                )
            }
        )
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            item {
                SegmentedControlHomeTabs(
                    tabs = HomeScreenType.entries.toList(),
                    selectedTab = selectedHomeScreenType,
                    onTabSelected = onTabSelected
                )
            }

            when (selectedHomeScreenType) {
                HomeScreenType.OVERVIEW -> {
                    addHomeScreenContent(
                        netWorth = netWorthState.currentNetWorth,
                        summary = summary,
                        worthGoal = worthGoal,
                        worthGoalCurrency = worthGoalCurrency,
                        monthlyProgress = monthlyProgress,
                        dailyTransactions = transactions,
                        upcomingTransactions = upcomingPayments.map { it.toTransactionEntity() },
                        overdueTransactions = overduePayments.map { it.toTransactionEntity() },
                        categoriesMap = categoriesMap,
                        baseCurrency = baseCurrency,
                        exchangeRates = exchangeRates,
                        onEdit = { transaction ->
                            transactionsViewModel.onEditTransactionClicked(transaction)
                        },
                        onDelete = { transactionsViewModel.deleteTransaction(it) },
                        onPayPlanned = { plannedPaymentsViewModel.pay(it) },
                        onSkipPlanned = { plannedPaymentsViewModel.skip(it) },
                        allUpcoming = upcomingPayments,
                        allOverdue = overduePayments,
                        currentMonthIncome = currentMonthIncome,
                        currentMonthExpense = currentMonthExpense,
                        allExchangeRates = allExchangeRateInfos,
                        collapsedExchangeRates = collapsedExchangeRateInfos,
                    )
                }
                HomeScreenType.DASHBOARD -> {
                    val walletNetWorth = summary.totalBalance / 100.0
                    val portfolioNetWorth = portfolioSummary.totalPortfolioValue
                    val totalNetWorth = walletNetWorth + portfolioNetWorth

                    val cashFlow = currentMonthIncome - currentMonthExpense
                    val savingsRate = if (currentMonthIncome > 0) (cashFlow / currentMonthIncome).toFloat() else 0f

                    val stats = DashboardStats(
                        netWorth = netWorthState.currentNetWorth,
                        savingsRate = savingsRate,
                        thisMonthCashFlow = cashFlow,
                        baseCurrency = netWorthState.currency
                    )
                    addDashboardContent(
                        navController = navController,
                        stats = stats
                    )
                }
                HomeScreenType.NEWS -> addNewsScreenContent(
                    newsArticles = articles,
                    isLoading = isLoadingNews,
                    onArticleClick = { article -> onArticleClick(article.id) },
                    onRefresh = newsViewModel::refresh,
                )
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

fun PlannedPaymentEntity.toTransactionEntity(): TransactionEntity {
    return TransactionEntity(
        id = this.id,
        amount = this.amount,
        currency = this.currency,
        name = this.name,
        description = this.description,
        date = this.dueDate,
        userId = this.userId,
        walletId = this.walletId,
        categoryId = this.categoryId,
        transactionType = this.transactionType,
        toWalletId = this.toWalletId,
    )
}