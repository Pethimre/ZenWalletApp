package com.aestroon.wallets.presentation

import AddEditWalletDialog
import OfflineWarningBanner
import OverallSummaryCard
import SpendingWalletCard
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.data.model.WalletsSummary
import com.aestroon.common.data.serializable.Currency
import com.aestroon.common.domain.WalletsUiState
import com.aestroon.common.domain.WalletsViewModel
import com.aestroon.common.presentation.components.ConfirmDeleteDialog
import com.aestroon.common.utilities.network.ConnectivityObserver
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(
    viewModel: WalletsViewModel = koinViewModel(),
) {
    val wallets: List<WalletEntity> by viewModel.wallets.collectAsState()
    val uiState: WalletsUiState by viewModel.uiState.collectAsState()
    val allCurrencies: List<Currency> by viewModel.allCurrencies.collectAsState()
    val hasPendingSyncs: Boolean by viewModel.hasPendingSyncs.collectAsState()
    val networkStatus: ConnectivityObserver.Status by viewModel.networkStatus.collectAsState()
    val summary: WalletsSummary by viewModel.summary.collectAsState()
    val baseCurrency: String by viewModel.baseCurrency.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var walletToEdit by remember { mutableStateOf<WalletEntity?>(null) }
    var showConfirmDeleteDialog by remember { mutableStateOf<WalletEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.onEnterScreen() }
    LaunchedEffect(uiState) {
        if (uiState is WalletsUiState.Error) {
            snackbarHostState.showSnackbar((uiState as WalletsUiState.Error).message)
        }
    }

    val showOverview = wallets.size >= 2
    val pageCount = if (showOverview) 2 else 1
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OfflineWarningBanner(isVisible = hasPendingSyncs && networkStatus == ConnectivityObserver.Status.Unavailable)

            if (uiState is WalletsUiState.Loading && wallets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pageIndex ->
                    val page = if (showOverview) pageIndex else 0
                    if (page == 0 && showOverview) {
                        LazyColumn(contentPadding = PaddingValues(16.dp)) {
                            item { OverallSummaryCard(summary = summary, baseCurrency = baseCurrency) }
                        }
                    } else {
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
                                    Text("No wallets yet. Tap 'Add Wallet' to create one!",
                                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                                        textAlign = TextAlign.Center)
                                }
                            } else {
                                items(wallets, key = { it.id }) { wallet ->
                                    SpendingWalletCard(
                                        wallet = wallet,
                                        onEdit = { walletToEdit = wallet; showAddEditDialog = true },
                                        onDelete = { showConfirmDeleteDialog = wallet }
                                    )
                                }
                            }
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
    SpendingWalletCard(wallet = wallet, onEdit = {}, onDelete = {})
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
        WalletEntity("2", "Debit Card", 15000000L, "#2196F3", "HUF", "user1", "CreditCard", true, 2122),
        WalletEntity("3", "Savings", 30000000L, "#F44336", "HUF", "user1", "Savings", true, 82421)
    )
    val summary = WalletsSummary(
        totalBalance = 50000000L,
        balanceBreakdown = wallets.map { it to (it.balance.toFloat() / 50000000.0f) }
    )
    OverallSummaryCard(summary = summary, baseCurrency = "HUF")
}
