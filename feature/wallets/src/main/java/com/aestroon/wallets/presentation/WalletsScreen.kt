package com.aestroon.wallets.presentation

import AddEditWalletDialog
import ConfirmDeleteWalletDialog
import OfflineWarningBanner
import OverallSummaryCard
import SpendingWalletCard
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.aestroon.common.utilities.network.ConnectivityObserver
import com.aestroon.wallets.domain.WalletsUiState
import com.aestroon.wallets.domain.WalletsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WalletsScreen(
    viewModel: WalletsViewModel = koinViewModel()
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

    LaunchedEffect(Unit) {
        viewModel.onEnterScreen()
    }

    LaunchedEffect(uiState) {
        if (uiState is WalletsUiState.Error) {
            snackbarHostState.showSnackbar((uiState as WalletsUiState.Error).message)
        }
    }

    val showOverview = wallets.size >= 2
    val pageCount = if (showOverview) 2 else 1
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    walletToEdit = null
                    showAddEditDialog = true
                },
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add New Wallet")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OfflineWarningBanner(
                isVisible = hasPendingSyncs && networkStatus == ConnectivityObserver.Status.Unavailable
            )

            if (uiState is WalletsUiState.Loading && wallets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
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
                                Text("Your Wallets", style = MaterialTheme.typography.headlineSmall)
                            }
                            if (wallets.isEmpty()) {
                                item {
                                    Text(
                                        "No wallets yet. Tap '+' to add one!",
                                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                items(wallets, key = { it.id }) { wallet ->
                                    SpendingWalletCard(
                                        wallet = wallet,
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
        ConfirmDeleteWalletDialog(
            walletName = walletToDelete.displayName,
            onDismiss = { showConfirmDeleteDialog = null },
            onConfirm = { viewModel.deleteWallet(walletToDelete) }
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
        iconName = "CreditCard"
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
        WalletEntity("1", "Cash", 5000000L, "#4CAF50", "HUF", "user1", "Payments"),
        WalletEntity("2", "Debit Card", 15000000L, "#2196F3", "HUF", "user1", "CreditCard"),
        WalletEntity("3", "Savings", 30000000L, "#F44336", "HUF", "user1", "Savings")
    )
    val summary = WalletsSummary(
        totalBalance = 50000000L,
        balanceBreakdown = wallets.map { it to (it.balance.toFloat() / 50000000.0f) }
    )
    OverallSummaryCard(summary = summary, baseCurrency = "HUF")
}
