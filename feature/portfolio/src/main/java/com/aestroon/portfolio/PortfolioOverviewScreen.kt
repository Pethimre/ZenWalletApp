package com.aestroon.portfolio

import HeldInstrument
import PortfolioAccount
import PortfolioAssetType
import PortfolioSummary
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Addchart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aestroon.common.data.repository.TimeRange
import com.aestroon.common.domain.PortfolioViewModel
import com.aestroon.common.presentation.components.BondPieChart
import com.aestroon.common.presentation.components.ConfirmDeleteDialog
import com.aestroon.common.utilities.DEFAULT_BASE_CURRENCY
import org.koin.androidx.compose.koinViewModel
import java.text.DecimalFormat

// --- Formatting (Unchanged) ---
private val currencyFormatter = DecimalFormat("#,##0.00")
private val percentageFormatter = DecimalFormat("0.00'%'")
private fun formatCurrency(value: Double, currencySymbol: String = "HUF "): String { return "$currencySymbol${currencyFormatter.format(value)}" }
private fun formatPercentage(value: Double): String { return percentageFormatter.format(value) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioOverviewScreen(
    viewModel: PortfolioViewModel = koinViewModel()
) {
    var selectedAssetType by remember { mutableStateOf(PortfolioAssetType.ALL) }
    val accounts by viewModel.accounts.collectAsState()
    val overallSummary by viewModel.overallSummary.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val isChartLoading by viewModel.isChartLoading.collectAsState()
    val marketDataFailed by viewModel.marketDataFailed.collectAsState()
    val showAddAccountDialogFor by viewModel.showAddAccountDialog.collectAsState()
    val showAddInstrumentDialogFor by viewModel.showAddInstrumentDialog.collectAsState()
    val showEditAccountDialogFor by viewModel.showEditAccountDialog.collectAsState()
    val showEditInstrumentDialogFor by viewModel.showEditInstrumentDialog.collectAsState()
    var accountToDelete by remember { mutableStateOf<PortfolioAccount?>(null) }
    var instrumentToDelete by remember { mutableStateOf<HeldInstrument?>(null) }

    Scaffold(
        floatingActionButton = {
            if (selectedAssetType != PortfolioAssetType.ALL) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.onAddAccountClicked(selectedAssetType.name) },
                    icon = { Icon(Icons.Filled.Add, "Add Account Icon") },
                    text = { Text("Account") },
                    shape = RoundedCornerShape(16.dp),
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SegmentedControlTabs(
                tabs = PortfolioAssetType.entries.toList(),
                selectedTab = selectedAssetType,
                onTabSelected = { newTab ->
                    selectedAssetType = newTab
                    viewModel.clearChartData() // Clear chart when switching main tabs
                }
            )
            val filteredAccounts = remember(accounts, selectedAssetType) {
                if (selectedAssetType == PortfolioAssetType.ALL) accounts else accounts.filter { it.accountType == selectedAssetType }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
            ) {
                item(key = "summary_card") {
                    if (selectedAssetType == PortfolioAssetType.ALL) {
                        OverallPortfolioSummaryCard(summary = overallSummary, modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        val totalValue = filteredAccounts.sumOf { it.totalValue }
                        val cost = filteredAccounts.sumOf { it.totalCostBasis }
                        val performance = if (cost == 0.0) 0.0 else ((totalValue - cost) / cost) * 100
                        AssetTypeSummaryCard(
                            assetType = selectedAssetType,
                            totalValue = totalValue,
                            overallPerformancePercentage = performance,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
                if (filteredAccounts.isEmpty() && selectedAssetType != PortfolioAssetType.ALL) {
                    item {
                        Box(Modifier.fillParentMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                            Text("No ${selectedAssetType.displayName} accounts yet.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(filteredAccounts, key = { it.id }) { account ->
                        AccountCard(
                            account = account,
                            chartData = chartData,
                            isChartLoading = isChartLoading,
                            isMarketDataFailed = marketDataFailed,
                            onFetchData = viewModel::fetchHistoricalData,
                            onClearChartData = viewModel::clearChartData,
                            onAddInstrument = { viewModel.onAddInstrumentClicked(account) },
                            onEditAccount = { viewModel.onEditAccountClicked(account) },
                            onDeleteAccount = { accountToDelete = account },
                            onEditInstrument = { instrument -> viewModel.onEditInstrumentClicked(account, instrument) },
                            onDeleteInstrument = { instrument -> instrumentToDelete = instrument },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
    showAddAccountDialogFor?.let { assetType -> AddEditAccountDialog(assetType = PortfolioAssetType.valueOf(assetType), onDismiss = viewModel::onAddAccountDialogDismiss, onConfirm = viewModel::onAddAccountConfirm) }
    showEditAccountDialogFor?.let { account -> EditAccountDialog(existingAccount = account, onDismiss = viewModel::onEditAccountDialogDismiss, onConfirm = { newName -> viewModel.onEditAccountConfirm(account.id, newName, account.accountType.name) }) }
    showAddInstrumentDialogFor?.let { account -> AddEditInstrumentDialog(accountName = account.accountName, assetType = account.accountType, onDismiss = viewModel::onAddInstrumentDialogDismiss, onConfirm = { symbol, name, currency, quantity, price, maturityDateStr, couponRate -> viewModel.onAddInstrumentConfirm(account, symbol, name, currency, quantity, price, maturityDateStr, couponRate) }) }
    showEditInstrumentDialogFor?.let { (account, instrument) -> AddEditInstrumentDialog(accountName = account.accountName, assetType = account.accountType, existingInstrument = instrument, onDismiss = viewModel::onEditInstrumentDialogDismiss, onConfirm = { symbol, name, currency, quantity, price, maturityDateStr, couponRate -> viewModel.onEditInstrumentConfirm(account, instrument, symbol, name, currency, quantity, price, maturityDateStr, couponRate) }) }
    accountToDelete?.let { account -> ConfirmDeleteDialog(itemName = account.accountName, itemType = "account (and all its instruments)", onDismiss = { accountToDelete = null }, onConfirm = { viewModel.onDeleteAccount(account.id) }) }
    instrumentToDelete?.let { instrument -> ConfirmDeleteDialog(itemName = instrument.instrument.name, itemType = "instrument", onDismiss = { instrumentToDelete = null }, onConfirm = { viewModel.onDeleteInstrument(instrument.instrument.id) }) }
}

// --- Supporting UI Components ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeSelector(selectedRange: TimeRange, onRangeSelected: (TimeRange) -> Unit, modifier: Modifier = Modifier) {
    val options = TimeRange.entries.toList()
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, range ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { onRangeSelected(range) },
                selected = range == selectedRange,
                icon = { }
            ) {
                Text(range.displayName)
            }
        }
    }
}

@Composable
fun AccountCard(
    account: PortfolioAccount,
    chartData: List<Double>,
    isChartLoading: Boolean,
    isMarketDataFailed: Boolean,
    onFetchData: (HeldInstrument, TimeRange) -> Unit,
    onClearChartData: () -> Unit,
    onAddInstrument: () -> Unit,
    onEditAccount: () -> Unit,
    onDeleteAccount: () -> Unit,
    onEditInstrument: (HeldInstrument) -> Unit,
    onDeleteInstrument: (HeldInstrument) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedInstrument by remember { mutableStateOf<HeldInstrument?>(null) }
    var selectedRange by remember { mutableStateOf(TimeRange.MONTH) }

    DisposableEffect(Unit) {
        onDispose {
            onClearChartData()
        }
    }

    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(account.accountName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = onEditAccount, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.EditNote, contentDescription = "Edit Account", tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = onDeleteAccount, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.DeleteSweep, contentDescription = "Delete Account", tint = MaterialTheme.colorScheme.error) }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (account.overallProfitLossPercentage != 0.0) {
                Text("Total Value: ${formatCurrency(account.totalValue, account.instruments.firstOrNull()?.instrument?.currency ?: DEFAULT_BASE_CURRENCY)} (${formatPercentage(account.overallProfitLossPercentage)} P/L)", style = MaterialTheme.typography.bodyMedium, color = if (account.overallProfitLoss >= 0) Color(0xFF28A745) else MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            } else {
                Text("Total Value: ${formatCurrency(account.totalValue, account.instruments.firstOrNull()?.instrument?.currency ?: DEFAULT_BASE_CURRENCY)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            if (account.accountType != PortfolioAssetType.BONDS) {
                if (selectedInstrument != null) {
                    if (isChartLoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else if (chartData.isNotEmpty()) {
                        AnimatedLineChart(data = chartData, modifier = Modifier.fillMaxWidth().height(150.dp))
                    } else if (isMarketDataFailed) {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) { Text("Chart data is not available for this asset.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeRangeSelector(
                        selectedRange = selectedRange,
                        onRangeSelected = { newRange ->
                            selectedRange = newRange
                            selectedInstrument?.let { onFetchData(it, newRange) }
                        }
                    )
                }
            } else if (account.instruments.size >= 2) {
                BondPieChart(instruments = account.instruments)
            }
            Column {
                account.instruments.forEach { heldInstrument ->
                    InstrumentRow(
                        heldInstrument = heldInstrument,
                        isMarketDataFailed = isMarketDataFailed,
                        onEdit = { onEditInstrument(heldInstrument) },
                        onDelete = { onDeleteInstrument(heldInstrument) },
                        onSelect = {
                            if (account.accountType != PortfolioAssetType.BONDS) {
                                val newSelection = if (selectedInstrument == heldInstrument) null else heldInstrument
                                selectedInstrument = newSelection
                                if (newSelection != null) {
                                    onFetchData(newSelection, selectedRange)
                                } else {
                                    onClearChartData()
                                }
                            }
                        },
                        isSelected = selectedInstrument?.instrument?.id == heldInstrument.instrument.id
                    )
                    if (heldInstrument != account.instruments.last()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onAddInstrument, modifier = Modifier.align(Alignment.Start).height(48.dp), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Filled.Addchart, contentDescription = "Add Instrument", modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Add Instrument")
            }
        }
    }
}

@Composable
fun InstrumentRow(
    heldInstrument: HeldInstrument,
    isMarketDataFailed: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
    Column(
        modifier = modifier.clip(RoundedCornerShape(8.dp)).background(backgroundColor).clickable { onSelect() }.padding(horizontal = 4.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(heldInstrument.instrument.icon?.let { Color.Transparent } ?: MaterialTheme.colorScheme.secondaryContainer, CircleShape).clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                heldInstrument.instrument.icon?.let { Icon(imageVector = it, contentDescription = heldInstrument.instrument.name, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)) } ?: Text(text = heldInstrument.instrument.symbol.firstOrNull()?.toString()?.uppercase() ?: "I", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = heldInstrument.instrument.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "${heldInstrument.quantity} x ${formatCurrency(heldInstrument.averageBuyPrice, heldInstrument.instrument.currency + " ")}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!isMarketDataFailed && heldInstrument.profitLoss.toInt() != 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val pnlColor = if (heldInstrument.profitLoss >= 0) Color(0xFF28A745) else MaterialTheme.colorScheme.error
                        Icon(imageVector = if (heldInstrument.profitLoss >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown, contentDescription = "Trend", tint = pnlColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${if (heldInstrument.profitLoss >= 0) "+" else ""}${formatCurrency(heldInstrument.profitLoss, "")} (${formatPercentage(heldInstrument.profitLossPercentage)})", style = MaterialTheme.typography.bodyMedium, color = pnlColor, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(text = "Value: ${formatCurrency(heldInstrument.currentValue, heldInstrument.instrument.currency + " ")}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.Edit, contentDescription = "Edit Instrument", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.Delete, contentDescription = "Delete Instrument", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun AnimatedLineChart(modifier: Modifier = Modifier, data: List<Double>, lineColor: Color = MaterialTheme.colorScheme.primary, fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), strokeWidth: Float = 3f) {
    if (data.isEmpty()) { Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data for chart", style = MaterialTheme.typography.bodySmall) }; return }
    Canvas(modifier = modifier.fillMaxSize()) {
        val path = Path()
        val fillPath = Path()
        val xStep = size.width / (data.size - 1).coerceAtLeast(1)
        val yDataMin = data.minOrNull() ?: 0.0
        val yDataMax = data.maxOrNull() ?: 1.0
        val yPadding = (yDataMax - yDataMin) * 0.05
        val yMin = (yDataMin - yPadding).coerceAtLeast(0.0)
        val yMax = yDataMax + yPadding
        val yRange = (yMax - yMin).coerceAtLeast(1.0)
        fun getX(index: Int): Float = index * xStep
        fun getY(value: Double): Float = size.height - ((value - yMin).toFloat() / yRange.toFloat() * size.height)
        path.moveTo(getX(0), getY(data[0]))
        fillPath.moveTo(getX(0), size.height)
        fillPath.lineTo(getX(0), getY(data[0]))
        for (i in 1 until data.size) { path.lineTo(getX(i), getY(data[i])); fillPath.lineTo(getX(i), getY(data[i])) }
        fillPath.lineTo(getX(data.size - 1), size.height)
        fillPath.close()
        drawPath(path = fillPath, brush = Brush.verticalGradient(colors = listOf(fillColor, fillColor.copy(alpha = 0.0f))))
        drawPath(path = path, color = lineColor, style = Stroke(width = strokeWidth))
    }
}

@Composable
fun AssetTypeSummaryCard(assetType: PortfolioAssetType, totalValue: Double, overallPerformancePercentage: Double, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${assetType.displayName} Overview", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow("Total Value:", formatCurrency(totalValue), valueColor = MaterialTheme.colorScheme.onSurfaceVariant, labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            SummaryRow("Overall Performance:", formatPercentage(overallPerformancePercentage), valueColor = if (overallPerformancePercentage >= 0) Color(0xFF28A745) else MaterialTheme.colorScheme.error, labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
        }
    }
}
@Composable
fun OverallPortfolioSummaryCard(summary: PortfolioSummary, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total Portfolio Summary", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow("Total Value:", formatCurrency(summary.totalPortfolioValue), valueColor = MaterialTheme.colorScheme.onPrimaryContainer, labelColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            SummaryRow("Overall P/L:", "${formatCurrency(summary.totalProfitLoss)} (${formatPercentage(summary.totalProfitLossPercentage)})", valueColor = if (summary.totalProfitLoss >= 0) Color(0xFF006400) else Color(0xFFB22222), labelColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Asset Breakdown:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            summary.performanceByAssetType.forEach { (type, value) -> if (type != PortfolioAssetType.ALL) { SummaryRow("${type.displayName}:", formatCurrency(value), labelFontSize = 14.sp, valueFontSize = 14.sp, valueColor = MaterialTheme.colorScheme.onPrimaryContainer, labelColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)) } }
        }
    }
}
@Composable
private fun SummaryRow(label: String, value: String, labelFontSize: TextUnit = 16.sp, valueFontSize: TextUnit = 18.sp, valueColor: Color, labelColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge.copy(fontSize = labelFontSize), color = labelColor)
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontSize = valueFontSize), fontWeight = FontWeight.Bold, color = valueColor, textAlign = TextAlign.End)
    }
}

@Composable
fun EditAccountDialog(
    existingAccount: PortfolioAccount,
    onDismiss: () -> Unit,
    onConfirm: (newName: String) -> Unit
) {
    var accountName by remember { mutableStateOf(existingAccount.accountName) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Edit Account",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(accountName) },
                        enabled = accountName.isNotBlank()
                    ) { Text("Save") }
                }
            }
        }
    }
}

// --- Dialogs ---
@Composable
fun AddEditAccountDialog(assetType: PortfolioAssetType? = null, existingAccount: PortfolioAccount? = null, onDismiss: () -> Unit, onConfirm: (accountName: String, type: String) -> Unit) {
    var accountName by remember { mutableStateOf(existingAccount?.accountName ?: "") }
    val dialogTitle = if (existingAccount == null) "Add ${assetType?.displayName} Account" else "Edit Account"
    val confirmText = if (existingAccount == null) "Add" else "Save"
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(dialogTitle, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(value = accountName, onValueChange = { accountName = it }, label = { Text("Account Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onConfirm(accountName, existingAccount?.accountType?.name ?: assetType!!.name) }, enabled = accountName.isNotBlank()) { Text(confirmText) }
                }
            }
        }
    }
}
@Composable
fun AddEditInstrumentDialog(
    accountName: String,
    assetType: PortfolioAssetType,
    existingInstrument: HeldInstrument? = null,
    onDismiss: () -> Unit,
    onConfirm: (symbol: String, name: String, currency: String, quantity: Double, price: Double, maturityDateStr: String, couponRate: Double?) -> Unit
) {
    var symbol by remember { mutableStateOf(existingInstrument?.instrument?.symbol ?: "") }
    var name by remember { mutableStateOf(existingInstrument?.instrument?.name ?: "") }
    var currency by remember { mutableStateOf(existingInstrument?.instrument?.currency ?: "HUF") }
    var quantity by remember { mutableStateOf(existingInstrument?.quantity?.toString() ?: "") }
    var price by remember { mutableStateOf(existingInstrument?.averageBuyPrice?.toString() ?: "") }
    var couponRate by remember { mutableStateOf(existingInstrument?.instrument?.couponRate?.toString() ?: "") }
    val dateFormatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }
    var maturityDateStr by remember { mutableStateOf(existingInstrument?.instrument?.maturityDate?.let { dateFormatter.format(it) } ?: "") }

    val dialogTitle = if (existingInstrument == null) "Add ${assetType.displayName}" else "Edit Instrument"
    val confirmText = if (existingInstrument == null) "Add" else "Save"

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(text = dialogTitle, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(value = symbol, onValueChange = { symbol = it }, label = { Text("Symbol (e.g., AAPL, BTC)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Instrument Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = currency, onValueChange = { currency = it.uppercase() }, label = { Text("Currency (e.g., USD, EUR)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity / Face Value") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Average Buy Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())

                if (assetType == PortfolioAssetType.BONDS) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = maturityDateStr, onValueChange = { maturityDateStr = it }, label = { Text("Maturity Date (YYYY-MM-DD)") }, placeholder = { Text("e.g., 2034-06-15") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = couponRate, onValueChange = { couponRate = it }, label = { Text("Coupon Rate (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                symbol,
                                if (name.isNotBlank()) name else symbol,
                                currency,
                                quantity.toDoubleOrNull() ?: 0.0,
                                price.toDoubleOrNull() ?: 0.0,
                                maturityDateStr,
                                couponRate.toDoubleOrNull()
                            )
                        },
                        enabled = symbol.isNotBlank() && quantity.isNotBlank() && price.isNotBlank() && currency.isNotBlank()
                    ) { Text(confirmText) }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
fun ConfirmDeleteDialogPreview() {
    MaterialTheme { ConfirmDeleteDialog("Bitcoin Holding", "instrument", {}, {}) }
}
