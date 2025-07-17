package com.aestroon.common.presentation.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.domain.CalendarViewModel
import com.aestroon.common.domain.TransactionUiModel
import com.aestroon.common.domain.UiTransactionType
import com.aestroon.common.domain.WalletsViewModel
import com.aestroon.common.theme.GreenChipColor
import com.aestroon.common.theme.OrangeChipColor
import com.aestroon.common.theme.RedChipColor
import com.aestroon.common.utilities.TextFormatter
import com.aestroon.common.utilities.TextFormatter.formatBalance
import com.aestroon.common.utilities.formatDayAndMonth
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    calendarViewModel: CalendarViewModel = koinViewModel(),
    walletsViewModel: WalletsViewModel = koinViewModel()
) {
    val wallets by calendarViewModel.wallets.collectAsState()
    val transactions by calendarViewModel.transactions.collectAsState()
    val isLoading by calendarViewModel.isLoading.collectAsState()
    val isLoadingMore by calendarViewModel.isLoadingMore.collectAsState()
    val baseCurrency by calendarViewModel.baseCurrency.collectAsState()
    val exchangeRates by calendarViewModel.exchangeRates.collectAsState()

    val totalBalance by walletsViewModel.summary.collectAsState()

    val pagerState = rememberPagerState(pageCount = { wallets.size })

    LaunchedEffect(pagerState.currentPage, wallets) {
        if (wallets.isNotEmpty() && pagerState.currentPage < wallets.size) {
            val selectedWalletId = wallets[pagerState.currentPage].id
            calendarViewModel.onWalletSelected(selectedWalletId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text("Total Balance", fontWeight = FontWeight.SemiBold)
        Text(
            text = TextFormatter.toPrettyAmountWithCurrency(totalBalance.totalBalance / 100.0, baseCurrency),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (wallets.isNotEmpty()) {
            HorizontalPager(state = pagerState) { page ->
                WalletCard(wallets[page])
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            TransactionList(
                transactions = transactions,
                isLoadingMore = isLoadingMore,
                baseCurrency = baseCurrency,
                exchangeRates = exchangeRates,
                onLoadMore = { calendarViewModel.loadMoreTransactions() }
            )
        }
    }
}

@Composable
fun WalletCard(wallet: WalletEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = wallet.composeColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = wallet.icon,
                contentDescription = wallet.displayName,
                tint = wallet.composeColor,
                modifier = Modifier
                    .size(40.dp)
                    .background(wallet.composeColor.copy(alpha = 0.2f), CircleShape)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(wallet.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = formatBalance(wallet.balance, wallet.currency),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionList(
    transactions: List<TransactionUiModel>,
    isLoadingMore: Boolean,
    baseCurrency: String,
    exchangeRates: Map<String, Double>?,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        if (transactions.isEmpty() && !isLoadingMore) {
            item {
                Text(
                    "No transactions for this wallet.",
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val grouped = transactions.groupBy {
                val cal = Calendar.getInstance().apply { time = it.date }
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }

            grouped.forEach { (dateMillis, transactionsOnDay) ->
                stickyHeader(key = "header_$dateMillis") {
                    Text(
                        text = formatDayAndMonth(Date(dateMillis)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                }
                items(
                    items = transactionsOnDay,
                    key = { transaction -> "item_${transaction.type}_${transaction.id}" }
                ) { transaction ->
                    TransactionListItem(
                        item = transaction,
                        baseCurrency = baseCurrency,
                        exchangeRates = exchangeRates
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
            }
        }

        if (isLoadingMore) {
            item(key = "loading_more_indicator") {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    LaunchedEffect(listState, transactions.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= transactions.size - 5 && transactions.isNotEmpty()) {
                    onLoadMore()
                }
            }
    }
}

@Composable
fun TransactionListItem(
    item: TransactionUiModel,
    baseCurrency: String,
    exchangeRates: Map<String, Double>?
) {
    val amountColor = when {
        item.amount > 0 -> GreenChipColor
        item.amount < 0 -> MaterialTheme.colorScheme.onSurface
        else -> Color.Gray
    }

    val convertedAmount = remember(item, baseCurrency, exchangeRates) {
        if (exchangeRates == null || item.currency == baseCurrency) return@remember null
        val baseRate = exchangeRates[baseCurrency] ?: return@remember null
        val itemRate = exchangeRates[item.currency] ?: return@remember null
        item.amount * (baseRate / itemRate)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item.category?.let {
            Icon(
                imageVector = it.icon,
                contentDescription = it.name,
                tint = it.composeColor,
                modifier = Modifier
                    .size(40.dp)
                    .background(it.composeColor.copy(alpha = 0.1f), CircleShape)
                    .padding(8.dp)
            )
        } ?: Box(modifier = Modifier.size(40.dp)) // Placeholder if no category

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.SemiBold)
            Text(
                text = item.type.name.lowercase().replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.bodySmall,
                color = when(item.type) {
                    UiTransactionType.UPCOMING -> OrangeChipColor
                    UiTransactionType.OVERDUE -> RedChipColor
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = TextFormatter.toPrettyAmountWithCurrency(item.amount, item.currency, withSign = true),
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
            convertedAmount?.let {
                Text(
                    text = "≈ ${TextFormatter.toPrettyAmountWithCurrency(it, baseCurrency, withSign = true)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
