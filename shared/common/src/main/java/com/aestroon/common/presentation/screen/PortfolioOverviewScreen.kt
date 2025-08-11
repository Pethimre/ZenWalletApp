package com.aestroon.common.presentation.screen

import HeldInstrument
import PortfolioAccount
import PortfolioAssetType
import PortfolioSummary
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
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
import com.aestroon.common.presentation.screen.components.AssetPieChart
import com.aestroon.common.presentation.screen.components.ConfirmDeleteDialog
import com.aestroon.common.presentation.screen.components.SegmentedControlTabs
import org.koin.androidx.compose.koinViewModel
import java.text.DecimalFormat

private val currencyFormatter = DecimalFormat("#,##0.00")
private val percentageFormatter = DecimalFormat("0.00'%'")
private fun formatCurrency(value: Double, currencySymbol: String = "HUF "): String { return "$currencySymbol${currencyFormatter.format(value)}" }
private fun formatPercentage(value: Double): String { return percentageFormatter.format(value) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioOverviewScreen(
    viewModel: PortfolioViewModel = koinViewModel()
) {
    var selectedAssetType by rememberSaveable { mutableStateOf(PortfolioAssetType.ALL) }
    val accounts by viewModel.accounts.collectAsState()
    val overallSummary by viewModel.overallSummary.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val isChartLoading by viewModel.isChartLoading.collectAsState()
    val showAddAccountDialogFor by viewModel.showAddAccountDialog.collectAsState()
    val showAddInstrumentDialogFor by viewModel.showAddInstrumentDialogFor.collectAsState()
    val showEditAccountDialogFor by viewModel.showEditAccountDialog.collectAsState()
    val showEditInstrumentDialogFor by viewModel.showEditInstrumentDialogFor.collectAsState()
    var accountToDelete by remember { mutableStateOf<PortfolioAccount?>(null) }
    var instrumentToDelete by remember { mutableStateOf<HeldInstrument?>(null) }
    val marketDataError by viewModel.marketDataError.collectAsState()

    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(marketDataError) {
        marketDataError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.onDataErrorShown()
        }
    }

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
                    viewModel.clearChartData()
                }
            )
            val filteredAccounts = remember(accounts, selectedAssetType) {
                if (selectedAssetType == PortfolioAssetType.ALL) accounts else accounts.filter { it.accountType == selectedAssetType }
            }
            LazyColumn(
                state = listState,
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
                            onFetchData = viewModel::fetchHistoricalData,
                            onClearChartData = viewModel::clearChartData,
                            onAddInstrument = { viewModel.onAddInstrumentClicked(account) },
                            onEditAccount = { viewModel.onEditAccountClicked(account) },
                            onDeleteAccount = { accountToDelete = account },
                            onEditInstrument = { instrument -> viewModel.onEditInstrumentClicked(account, instrument) },
                            onDeleteInstrument = { instrument -> instrumentToDelete = instrument },
                            onUpdateInstrumentPrice = viewModel::onUpdateInstrumentPrice,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
    showAddAccountDialogFor?.let { assetType -> AddEditAccountDialog(assetType = PortfolioAssetType.valueOf(assetType), onDismiss = viewModel::onAddAccountDialogDismiss, onConfirm = viewModel::onAddAccountConfirm) }
    showEditAccountDialogFor?.let { account -> EditAccountDialog(existingAccount = account, onDismiss = viewModel::onEditAccountDialogDismiss, onConfirm = { newName -> viewModel.onEditAccountConfirm(account.id, newName, account.accountType.name) }) }

    showAddInstrumentDialogFor?.let { account ->
        val symbol by viewModel.dialogSymbol.collectAsState()
        val name by viewModel.dialogName.collectAsState()
        val currency by viewModel.dialogCurrency.collectAsState()
        val quantity by viewModel.dialogQuantity.collectAsState()
        val price by viewModel.dialogPrice.collectAsState()
        val maturityDate by viewModel.dialogMaturityDate.collectAsState()
        val couponRate by viewModel.dialogCouponRate.collectAsState()
        val lookupPrice by viewModel.dialogLookupPrice.collectAsState()
        val currentPrice by viewModel.dialogCurrentPrice.collectAsState()

        AddEditInstrumentDialog(
            accountName = account.accountName,
            assetType = account.accountType,
            existingInstrument = null,
            onDismiss = viewModel::onAddInstrumentDialogDismiss,
            onConfirm = viewModel::onAddInstrumentConfirm,
            symbol = symbol,
            onSymbolChange = viewModel::onDialogSymbolChange,
            name = name,
            onNameChange = viewModel::onDialogNameChange,
            currency = currency,
            onCurrencyChange = viewModel::onDialogCurrencyChange,
            quantity = quantity,
            onQuantityChange = viewModel::onDialogQuantityChange,
            averageBuyPrice = price,
            onAverageBuyPriceChange = viewModel::onDialogPriceChange,
            maturityDateStr = maturityDate,
            onMaturityDateChange = viewModel::onDialogMaturityDateChange,
            couponRate = couponRate,
            onCouponRateChange = viewModel::onDialogCouponRateChange,
            lookupPrice = lookupPrice,
            onLookupPriceChange = viewModel::onDialogLookupPriceChange,
            currentPrice = currentPrice,
            onCurrentPriceChange = viewModel::onDialogCurrentPriceChange
        )
    }

    showEditInstrumentDialogFor?.let { (account, instrument) ->
        val symbol by viewModel.dialogSymbol.collectAsState()
        val name by viewModel.dialogName.collectAsState()
        val currency by viewModel.dialogCurrency.collectAsState()
        val quantity by viewModel.dialogQuantity.collectAsState()
        val price by viewModel.dialogPrice.collectAsState()
        val maturityDate by viewModel.dialogMaturityDate.collectAsState()
        val couponRate by viewModel.dialogCouponRate.collectAsState()
        val lookupPrice by viewModel.dialogLookupPrice.collectAsState()
        val currentPrice by viewModel.dialogCurrentPrice.collectAsState()

        AddEditInstrumentDialog(
            accountName = account.accountName,
            assetType = account.accountType,
            existingInstrument = instrument,
            onDismiss = viewModel::onEditInstrumentDialogDismiss,
            onConfirm = viewModel::onEditInstrumentConfirm,
            symbol = symbol,
            onSymbolChange = viewModel::onDialogSymbolChange,
            name = name,
            onNameChange = viewModel::onDialogNameChange,
            currency = currency,
            onCurrencyChange = viewModel::onDialogCurrencyChange,
            quantity = quantity,
            onQuantityChange = viewModel::onDialogQuantityChange,
            averageBuyPrice = price,
            onAverageBuyPriceChange = viewModel::onDialogPriceChange,
            maturityDateStr = maturityDate,
            onMaturityDateChange = viewModel::onDialogMaturityDateChange,
            couponRate = couponRate,
            onCouponRateChange = viewModel::onDialogCouponRateChange,
            lookupPrice = lookupPrice,
            onLookupPriceChange = viewModel::onDialogLookupPriceChange,
            currentPrice = currentPrice,
            onCurrentPriceChange = viewModel::onDialogCurrentPriceChange
        )
    }

    accountToDelete?.let { account -> ConfirmDeleteDialog(itemName = account.accountName, itemType = "account (and all its instruments)", onDismiss = { accountToDelete = null }, onConfirm = { viewModel.onDeleteAccount(account.id) }) }
    instrumentToDelete?.let { instrument -> ConfirmDeleteDialog(itemName = instrument.instrument.name, itemType = "instrument", onDismiss = { instrumentToDelete = null }, onConfirm = { viewModel.onDeleteInstrument(instrument.instrument.id) }) }
}

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
    onFetchData: (HeldInstrument, TimeRange) -> Unit,
    onClearChartData: () -> Unit,
    onAddInstrument: () -> Unit,
    onEditAccount: () -> Unit,
    onDeleteAccount: () -> Unit,
    onEditInstrument: (HeldInstrument) -> Unit,
    onDeleteInstrument: (HeldInstrument) -> Unit,
    onUpdateInstrumentPrice: (HeldInstrument, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var instrumentIdToUpdatePrice by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedInstrumentId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedRange by rememberSaveable { mutableStateOf(TimeRange.MONTH) }

    val selectedInstrument = remember(selectedInstrumentId, account.instruments) {
        account.instruments.find { it.instrument.id == selectedInstrumentId }
    }

    LaunchedEffect(selectedInstrument, selectedRange) {
        selectedInstrument?.let {
            onFetchData(it, selectedRange)
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
            if (account.overallProfitLoss.toInt() != 0) {
                Text("Total Value: ${formatCurrency(account.totalValue, account.instruments.firstOrNull()?.instrument?.currency ?: "HUF")} (${formatPercentage(account.overallProfitLossPercentage)} P/L)", style = MaterialTheme.typography.bodyMedium, color = if (account.overallProfitLoss >= 0) Color(0xFF28A745) else MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            } else {
                Text("Total Value: ${formatCurrency(account.totalValue, account.instruments.firstOrNull()?.instrument?.currency ?: "HUF")}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            if (selectedInstrument != null && account.accountType != PortfolioAssetType.BONDS) {
                if (isChartLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else if (chartData.isNotEmpty()) {
                    AnimatedLineChart(data = chartData, modifier = Modifier.fillMaxWidth().height(150.dp))
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) { Text("No chart data available.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TimeRangeSelector(
                    selectedRange = selectedRange,
                    onRangeSelected = { newRange -> selectedRange = newRange }
                )
            } else if (account.instruments.size >= 2) {
                AssetPieChart(instruments = account.instruments)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Column {
                account.instruments.forEach { heldInstrument ->
                    InstrumentRow(
                        heldInstrument = heldInstrument,
                        onEdit = { onEditInstrument(heldInstrument) },
                        onDelete = { onDeleteInstrument(heldInstrument) },
                        onUpdatePrice = {
                            instrumentIdToUpdatePrice = heldInstrument.instrument.id
                        },
                        onSelect = {
                            if (account.accountType != PortfolioAssetType.BONDS) {
                                selectedInstrumentId = if (selectedInstrumentId == heldInstrument.instrument.id) null else heldInstrument.instrument.id
                            }
                        },
                        isSelected = selectedInstrumentId == heldInstrument.instrument.id
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
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    onUpdatePrice: () -> Unit,
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
                if (heldInstrument.profitLoss.toInt() != 0) {
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
            if (!heldInstrument.lookupPrice) {
                IconButton(onClick = onUpdatePrice, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Update, contentDescription = "Update Price", tint = MaterialTheme.colorScheme.primary)
                }
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.Edit, contentDescription = "Edit Instrument", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.Delete, contentDescription = "Delete Instrument", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun AnimatedLineChart(
    modifier: Modifier = Modifier,
    data: List<List<Double>>,
    lineColors: List<Color> = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
    fillColors: List<Color> = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)),
    strokeWidth: Float = 3f
) {
    if (data.isEmpty() || data.any { it.isEmpty() }) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data for chart", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val allDataPoints = data.flatten()
        val xStep = size.width / (data.first().size - 1).coerceAtLeast(1)

        val yDataMin = allDataPoints.minOrNull() ?: 0.0
        val yDataMax = allDataPoints.maxOrNull() ?: 1.0
        val yPadding = (yDataMax - yDataMin) * 0.05
        val yMin = (yDataMin - yPadding).coerceAtLeast(0.0)
        val yMax = yDataMax + yPadding
        val yRange = (yMax - yMin).coerceAtLeast(1.0)

        fun getX(index: Int): Float = index * xStep
        fun getY(value: Double): Float = size.height - ((value - yMin).toFloat() / yRange.toFloat() * size.height)

        data.forEachIndexed { seriesIndex, dataSeries ->
            val path = Path()
            val fillPath = Path()
            val lineColor = lineColors.getOrElse(seriesIndex) { Color.Gray }
            val fillColor = fillColors.getOrElse(seriesIndex) { Color.Transparent }

            path.moveTo(getX(0), getY(dataSeries[0]))
            fillPath.moveTo(getX(0), size.height)
            fillPath.lineTo(getX(0), getY(dataSeries[0]))

            for (i in 1 until dataSeries.size) {
                path.lineTo(getX(i), getY(dataSeries[i]))
                fillPath.lineTo(getX(i), getY(dataSeries[i]))
            }

            fillPath.lineTo(getX(dataSeries.size - 1), size.height)
            fillPath.close()

            drawPath(path = fillPath, brush = Brush.verticalGradient(colors = listOf(fillColor, fillColor.copy(alpha = 0.0f))))
            drawPath(path = path, color = lineColor, style = Stroke(width = strokeWidth))
        }
    }
}

@Composable
fun AnimatedLineChart(
    modifier: Modifier = Modifier,
    data: List<Double>,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
    strokeWidth: Float = 3f
) {
    AnimatedLineChart(
        modifier = modifier,
        data = listOf(data),
        lineColors = listOf(lineColor),
        fillColors = listOf(fillColor),
        strokeWidth = strokeWidth
    )
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
    var accountName by rememberSaveable { mutableStateOf(existingAccount.accountName) }

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

@Composable
fun AddEditAccountDialog(assetType: PortfolioAssetType? = null, existingAccount: PortfolioAccount? = null, onDismiss: () -> Unit, onConfirm: (accountName: String, type: String) -> Unit) {
    var accountName by rememberSaveable { mutableStateOf(existingAccount?.accountName ?: "") }
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
    symbol: String,
    onSymbolChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    currency: String,
    onCurrencyChange: (String) -> Unit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    averageBuyPrice: String,
    onAverageBuyPriceChange: (String) -> Unit,
    maturityDateStr: String,
    onMaturityDateChange: (String) -> Unit,
    couponRate: String,
    onCouponRateChange: (String) -> Unit,
    lookupPrice: Boolean,
    onLookupPriceChange: (Boolean) -> Unit,
    currentPrice: String,
    onCurrentPriceChange: (String) -> Unit,
    accountName: String,
    assetType: PortfolioAssetType,
    existingInstrument: HeldInstrument?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val dialogTitle = if (existingInstrument == null) "Add ${assetType.displayName}" else "Edit Instrument"
    val confirmText = if (existingInstrument == null) "Add" else "Save"

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(text = dialogTitle, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(value = symbol, onValueChange = onSymbolChange, label = { Text("Symbol (e.g., AAPL, BTC)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Instrument Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = currency, onValueChange = onCurrencyChange, label = { Text("Currency (e.g., USD, EUR)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = quantity, onValueChange = onQuantityChange, label = { Text("Quantity / Face Value") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = averageBuyPrice, onValueChange = onAverageBuyPriceChange, label = { Text("Average Buy Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onLookupPriceChange(!lookupPrice) }.padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Checkbox(checked = lookupPrice, onCheckedChange = onLookupPriceChange)
                    Spacer(Modifier.width(8.dp))
                    Text("Look up live price information")
                }

                AnimatedVisibility(visible = !lookupPrice) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = currentPrice,
                            onValueChange = onCurrentPriceChange,
                            label = { Text("Current Price (Manual)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (assetType == PortfolioAssetType.BONDS) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = maturityDateStr, onValueChange = onMaturityDateChange, label = { Text("Maturity Date (YYYY-MM-DD)") }, placeholder = { Text("e.g., 2034-06-15") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = couponRate, onValueChange = onCouponRateChange, label = { Text("Coupon Rate (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = symbol.isNotBlank() && quantity.isNotBlank() && averageBuyPrice.isNotBlank() && currency.isNotBlank()
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
