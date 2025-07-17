package com.aestroon.common.presentation.screen

import AddEditWalletDialog
import OverallSummaryCard
import SpendingWalletCard
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.data.model.MonthlyCashFlow
import com.aestroon.common.data.model.WalletsSummary
import com.aestroon.common.data.serializable.Currency
import com.aestroon.common.domain.WalletMonthlySummary
import com.aestroon.common.domain.WalletsViewModel
import com.aestroon.common.presentation.screen.components.ConfirmDeleteDialog
import com.aestroon.common.theme.GreenChipColor
import com.aestroon.common.theme.RedChipColor
import org.koin.androidx.compose.koinViewModel
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WalletsScreen(
    viewModel: WalletsViewModel = koinViewModel(),
) {
    val wallets by viewModel.wallets.collectAsState()
    val allCurrencies by viewModel.allCurrencies.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val monthlyCashFlow by viewModel.monthlyCashFlow.collectAsState()
    val baseCurrency by viewModel.baseCurrency.collectAsState()
    val exchangeRates by viewModel.exchangeRates.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState()
    var expandedWalletId by remember { mutableStateOf<String?>(null) }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var walletToEdit by remember { mutableStateOf<WalletEntity?>(null) }

    var showConfirmDeleteDialog by remember { mutableStateOf<WalletEntity?>(null) }

    LaunchedEffect(expandedWalletId) {
        viewModel.loadMonthlySummaryFor(expandedWalletId)
    }

    val tabs = listOf("Overview", "Wallets")
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Your Wallets", style = MaterialTheme.typography.headlineSmall)
                                TextButton(onClick = {
                                    walletToEdit = null
                                    showAddEditDialog = true
                                }) {
                                    Text("Add Wallet")
                                }
                            }
                        }
                        if (wallets.isEmpty()) {
                            item {
                                Text(
                                    "No wallets found. Tap 'Add Wallet' to create one.",
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(wallets, key = { it.id }) { wallet ->
                                SpendingWalletCard(
                                    wallet = wallet,
                                    isExpanded = wallet.id == expandedWalletId,
                                    monthlySummary = if (wallet.id == expandedWalletId) monthlySummary else null,
                                    baseCurrency = baseCurrency,
                                    exchangeRates = exchangeRates,
                                    onClick = {
                                        expandedWalletId = if (expandedWalletId == wallet.id) null else wallet.id
                                    },
                                    onEdit = {
                                        walletToEdit = wallet
                                        showAddEditDialog = true
                                    },
                                    onDelete = { showConfirmDeleteDialog = wallet }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            OverallSummaryCard(
                                summary = summary,
                                monthlyIncome = monthlyCashFlow.lastOrNull()?.income ?: 0f,
                                monthlyExpense = monthlyCashFlow.lastOrNull()?.expense ?: 0f,
                                baseCurrency = baseCurrency
                            )
                        }
                        item {
                            MonthlyCashFlowChart(cashFlowData = monthlyCashFlow)
                        }
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditWalletDialog(
            existingWallet = walletToEdit,
            allCurrencies = allCurrencies,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { name, balanceStr, goalAmountStr, color, currency, iconName, included ->
                viewModel.addOrUpdateWallet(walletToEdit, name, balanceStr, goalAmountStr, color, currency, iconName, included)
                showAddEditDialog = false
            }
        )
    }

    showConfirmDeleteDialog?.let { walletToDelete ->
        ConfirmDeleteDialog(
            itemName = walletToDelete.displayName,
            onDismiss = { showConfirmDeleteDialog = null },
            onConfirm = { viewModel.deleteWallet(walletToDelete) },
            itemType = "wallet",
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun MonthlyCashFlowChart(
    cashFlowData: List<MonthlyCashFlow>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Monthly Cash Flow (Last 4 Months):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))

            if (cashFlowData.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxFlow = remember(cashFlowData) {
                        cashFlowData.maxOfOrNull { max(it.income, it.expense) }?.coerceAtLeast(1f) ?: 1f
                    }

                    cashFlowData.forEach { data ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Row(
                                modifier = Modifier.height(120.dp),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .fillMaxHeight(data.income / maxFlow)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(GreenChipColor)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .fillMaxHeight(data.expense / maxFlow)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(RedChipColor)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = data.month, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    LegendItem(color = GreenChipColor, label = "Income")
                    Spacer(modifier = Modifier.width(16.dp))
                    LegendItem(color = RedChipColor, label = "Expense")
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Not enough data for chart.")
                }
            }
        }
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
fun SpendingWalletCardPreview() {
    val wallet = WalletEntity(
        id = "1",
        displayName = "My Main Card",
        balance = 12345678L, // 123,456.78
        color = "#FF9800",
        currency = "HUF",
        ownerId = "user1",
        isSynced = false,
        iconName = "CreditCard",
        included = true,
        goalAmount = 1200,
    )
    SpendingWalletCard(
        wallet = wallet,
        onEdit = {},
        onDelete = {},
        isExpanded = true,
        monthlySummary = WalletMonthlySummary(1500.0, 800.0),
        onClick = {},
        baseCurrency = "HUF",
        exchangeRates = mapOf("EUR" to 400.0)
    )
}

@Preview(showBackground = true)
@Composable
fun AddWalletDialogPreview() {
    AddEditWalletDialog(
        existingWallet = null,
        allCurrencies = listOf(Currency("HUF", "Hungarian Forint"), Currency("EUR", "Euro")),
        onDismiss = {},
        onConfirm = { _, _, _, _, _, _, _ -> }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
fun WalletsOverviewPreview() {
    val wallets = listOf(
        WalletEntity("1", "Cash", 5000000L, "#4CAF50", "HUF", "user1", "Payments", true, 3242),
        WalletEntity(
            "2",
            "Debit Card",
            15000000L,
            "#2196F3",
            "HUF",
            "user1",
            "CreditCard",
            true,
            2122
        ),
        WalletEntity("3", "Savings", 30000000L, "#F44336", "HUF", "user1", "Savings", true, 82421)
    )
    val summary = WalletsSummary(
        totalBalance = 50000000L,
        balanceBreakdown = wallets.map { it to (it.balance.toFloat() / 50000000.0f) }
    )
    OverallSummaryCard(summary = summary, baseCurrency = "HUF", monthlyIncome = 1200f, monthlyExpense = 800f)
}
