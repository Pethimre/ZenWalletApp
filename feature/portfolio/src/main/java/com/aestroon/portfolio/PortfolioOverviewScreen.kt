package com.aestroon.portfolio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.TextUnit
import java.text.DecimalFormat
import kotlin.math.roundToInt
import kotlin.random.Random
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.aestroon.portfolio.mockProvider.HeldInstrument
import com.aestroon.portfolio.mockProvider.Instrument
import com.aestroon.portfolio.mockProvider.PortfolioAccount
import com.aestroon.portfolio.mockProvider.PortfolioAssetType
import com.aestroon.portfolio.mockProvider.PortfolioSummary
import com.aestroon.portfolio.mockProvider.SegmentedControlTabs
import com.aestroon.portfolio.mockProvider.mockPortfolioAccounts
import com.aestroon.portfolio.mockProvider.mockPortfolioSummary

// --- Formatting ---
val currencyFormatter = DecimalFormat("#,##0.00")
val percentageFormatter = DecimalFormat("0.00'%'")

fun formatCurrency(value: Double, currencySymbol: String = "HUF "): String {
    return "$currencySymbol${currencyFormatter.format(value)}"
}

fun formatPercentage(value: Double): String {
    return percentageFormatter.format(value)
}

// --- UI Components ---

@Composable
fun AnimatedLineChart(
    modifier: Modifier = Modifier,
    data: List<Double>,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
    strokeWidth: Float = 3f,
    showGridLines: Boolean = true,
    gridLineColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
) {
    if (data.isEmpty()) {
        Box(
            modifier
                .fillMaxSize()
                .then(modifier), contentAlignment = Alignment.Center
        ) {
            Text("No data for chart", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) // Simplified dash effect

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

        fun getX(index: Int) = index * xStep
        fun getY(value: Double) =
            size.height - ((value - yMin).toFloat() / yRange.toFloat() * size.height).toFloat()

        if (showGridLines) {
            val horizontalLines = 4
            for (i in 0..horizontalLines) {
                val y = size.height * i / horizontalLines
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
        }

        path.moveTo(getX(0), getY(data[0]))
        fillPath.moveTo(getX(0), size.height)
        fillPath.lineTo(getX(0), getY(data[0]))

        for (i in 1 until data.size) {
            path.lineTo(getX(i), getY(data[i]))
            fillPath.lineTo(getX(i), getY(data[i]))
        }

        fillPath.lineTo(getX(data.size - 1), size.height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor, fillColor.copy(alpha = 0.0f)),
                startY = 0f,
                endY = size.height
            )
        )
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = strokeWidth, pathEffect = pathEffect)
        )
    }
}

@Composable
fun InstrumentRow(
    heldInstrument: HeldInstrument,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(heldInstrument.instrument.icon?.let { Color.Transparent }
                    ?: MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            heldInstrument.instrument.icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = heldInstrument.instrument.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            } ?: Text(
                text = heldInstrument.instrument.symbol.firstOrNull()?.toString()?.uppercase()
                    ?: "I",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                heldInstrument.instrument.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${heldInstrument.quantity.roundToInt()} x ${formatCurrency(heldInstrument.instrument.currentPrice)} (${heldInstrument.instrument.symbol})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.wrapContentWidth(Alignment.End)
        ) {
            Text(
                formatCurrency(heldInstrument.currentValue),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val pnlColor =
                    if (heldInstrument.profitLoss >= 0) Color(0xFF28A745) else MaterialTheme.colorScheme.error
                Icon(
                    imageVector = if (heldInstrument.profitLoss >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = "Trend",
                    tint = pnlColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    "${if (heldInstrument.profitLoss >= 0) "+" else ""}${
                        formatCurrency(
                            heldInstrument.profitLoss,
                            ""
                        )
                    } (${formatPercentage(heldInstrument.profitLossPercentage)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = pnlColor
                )
            }
        }
        Row(modifier = Modifier.padding(start = 4.dp)) {
            IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit Instrument",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete Instrument",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AccountCard(
    account: PortfolioAccount,
    onAddInstrument: () -> Unit,
    onEditAccount: () -> Unit,
    onDeleteAccount: () -> Unit,
    onEditInstrument: (HeldInstrument) -> Unit,
    onDeleteInstrument: (HeldInstrument) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    account.accountName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onEditAccount, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Filled.EditNote,
                            contentDescription = "Edit Account",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDeleteAccount, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Filled.DeleteSweep,
                            contentDescription = "Delete Account",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Total Value: ${formatCurrency(account.totalValue)} (${formatPercentage(account.overallProfitLossPercentage)} P/L)",
                style = MaterialTheme.typography.bodyMedium,
                color = if (account.overallProfitLoss >= 0) Color(0xFF28A745) else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            if (account.instruments.isEmpty()) {
                Text(
                    "No instruments in this account.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(vertical = 24.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } else {
                Column {
                    account.instruments.forEach { heldInstrument ->
                        InstrumentRow(
                            heldInstrument = heldInstrument,
                            onEdit = { onEditInstrument(heldInstrument) },
                            onDelete = { onDeleteInstrument(heldInstrument) })
                        if (heldInstrument != account.instruments.last()) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(
                                    alpha = 0.3f
                                )
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAddInstrument,
                modifier = Modifier
                    .align(Alignment.Start)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Filled.Addchart,
                    contentDescription = "Add Instrument",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Add Instrument")
            }
        }
    }
}

@Composable
fun AssetTypeSummaryCard(
    assetType: PortfolioAssetType,
    totalValue: Double,
    overallPerformancePercentage: Double,
    historicalData: List<Double>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${assetType.displayName} Overview",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow(
                "Total Value:",
                formatCurrency(totalValue),
                valueColor = MaterialTheme.colorScheme.onSurfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            SummaryRow(
                "Overall Performance:",
                formatPercentage(overallPerformancePercentage),
                valueColor = if (overallPerformancePercentage >= 0) Color(0xFF28A745) else MaterialTheme.colorScheme.error,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedLineChart(
                data = historicalData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    }
}

@Composable
fun OverallPortfolioSummaryCard(
    summary: PortfolioSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onBackground.copy(
                alpha = .1f,
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Total Portfolio Summary",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow(
                "Total Value:",
                formatCurrency(summary.totalPortfolioValue),
                valueColor = MaterialTheme.colorScheme.onPrimaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            SummaryRow(
                "Overall P/L:",
                "${formatCurrency(summary.totalProfitLoss)} (${formatPercentage(summary.totalProfitLossPercentage)})",
                valueColor = if (summary.totalProfitLoss >= 0) Color(0xFF006400) else Color(
                    0xFFB22222
                ),
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Asset Breakdown:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            summary.performanceByAssetType.forEach { (type, value) ->
                if (type != PortfolioAssetType.ALL) {
                    SummaryRow(
                        "${type.displayName}:",
                        formatCurrency(value),
                        labelFontSize = 14.sp,
                        valueFontSize = 14.sp,
                        valueColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedLineChart(
                data = summary.historicalData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                lineColor = MaterialTheme.colorScheme.onPrimaryContainer,
                fillColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    labelFontSize: TextUnit = 16.sp,
    valueFontSize: TextUnit = 18.sp,
    valueColor: Color,
    labelColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = labelFontSize),
            color = labelColor
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = valueFontSize),
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun AddAccountDialog(
    assetType: PortfolioAssetType,
    onDismiss: () -> Unit,
    onConfirm: (accountName: String) -> Unit
) {
    var accountName by remember { mutableStateOf("") }
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
                    "Add ${assetType.displayName} Account",
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(accountName); onDismiss() },
                        enabled = accountName.isNotBlank()
                    ) { Text("Add Account") }
                }
            }
        }
    }
}

@Composable
fun AddInstrumentDialog(
    accountName: String,
    assetType: PortfolioAssetType,
    onDismiss: () -> Unit,
    onConfirm: (symbol: String, quantity: String, price: String) -> Unit
) {
    var symbol by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
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
                    "Add ${assetType.displayName} to $accountName",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it },
                    label = { Text("${assetType.displayName} Symbol (e.g. AAPL, BTC)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Average Buy Price") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(symbol, quantity, price); onDismiss() },
                        enabled = symbol.isNotBlank() && quantity.isNotBlank() && price.isNotBlank()
                    ) { Text("Add") }
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    itemName: String,
    itemType: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.error,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text("Confirm Deletion", color = MaterialTheme.colorScheme.error) },
        text = { Text("Are you sure you want to delete the $itemType \"$itemName\"? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = { onConfirm(); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete", color = MaterialTheme.colorScheme.onError) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioOverviewScreen(
    initialAccounts: List<PortfolioAccount> = mockPortfolioAccounts(),
    initialSummary: PortfolioSummary = mockPortfolioSummary(initialAccounts)
) {
    var selectedAssetType by remember { mutableStateOf(PortfolioAssetType.ALL) }
    val accounts = remember { initialAccounts.toMutableStateList() }
    var portfolioSummary by remember { mutableStateOf(initialSummary) }
    var showAddAccountDialogForType by remember { mutableStateOf<PortfolioAssetType?>(null) }
    var showAddInstrumentDialogForAccount by remember { mutableStateOf<PortfolioAccount?>(null) }
    var showDeleteConfirmationForAccount by remember { mutableStateOf<PortfolioAccount?>(null) }
    var showDeleteConfirmationForInstrument by remember {
        mutableStateOf<Pair<PortfolioAccount, HeldInstrument>?>(
            null
        )
    }

    fun updatePortfolioSummary() {
        portfolioSummary = mockPortfolioSummary(accounts)
    }

    Scaffold(
        floatingActionButton = {
            if (selectedAssetType != PortfolioAssetType.ALL) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            showAddAccountDialogForType = selectedAssetType
                        },
                        icon = { Icon(Icons.Filled.Add, "Add Account Icon") },
                        text = { Text(" Account") },
                        shape = RoundedCornerShape(16.dp),
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            SegmentedControlTabs(
                tabs = PortfolioAssetType.entries.toList(),
                selectedTab = selectedAssetType,
                onTabSelected = { selectedAssetType = it })

            val filteredAccounts = remember(
                accounts,
                selectedAssetType
            ) { accounts.filter { it.accountType == selectedAssetType } }
            val (assetTypeTotalValue, assetTypePerformance, assetTypeHistoricalData) = remember(
                filteredAccounts,
                selectedAssetType
            ) {
                val totalValue = filteredAccounts.sumOf { it.totalValue }
                val totalCost = filteredAccounts.sumOf { it.totalCostBasis }
                val performance =
                    if (totalCost == 0.0) 0.0 else ((totalValue - totalCost) / totalCost) * 100
                val historical = List(30) {
                    Random.nextDouble(
                        800.0,
                        1200.0
                    ) * (selectedAssetType.ordinal + Random.nextDouble(0.5, 1.5))
                }
                Triple(totalValue, performance, historical)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
            ) {
                when (selectedAssetType) {
                    PortfolioAssetType.ALL -> {
                        item(key = "overall_summary") {
                            OverallPortfolioSummaryCard(
                                summary = portfolioSummary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }

                    else -> {
                        item(key = "${selectedAssetType}_summary") {
                            AssetTypeSummaryCard(
                                assetType = selectedAssetType,
                                totalValue = assetTypeTotalValue,
                                overallPerformancePercentage = assetTypePerformance,
                                historicalData = assetTypeHistoricalData,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                        if (filteredAccounts.isEmpty()) {
                            item(key = "${selectedAssetType}_empty_state") {
                                Box(
                                    Modifier
                                        .fillParentMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No ${selectedAssetType.displayName} accounts yet.\nTap '+' to add one!",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(
                                filteredAccounts,
                                key = { "account_${it.id}" }) { account ->
                                AccountCard(
                                    account = account,
                                    onAddInstrument = {
                                        showAddInstrumentDialogForAccount = account
                                    },
                                    onEditAccount = { /* Edit */ },
                                    onDeleteAccount = {
                                        showDeleteConfirmationForAccount = account
                                    },
                                    onEditInstrument = { /* Edit */ },
                                    onDeleteInstrument = { instrument ->
                                        showDeleteConfirmationForInstrument =
                                            Pair(account, instrument)
                                    },
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
                item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        showAddAccountDialogForType?.let { assetType ->
            AddAccountDialog(
                assetType = assetType,
                onDismiss = { showAddAccountDialogForType = null }) { accountName ->
                val newAccount = PortfolioAccount(
                    "acc_${System.currentTimeMillis()}",
                    accountName,
                    assetType
                ); accounts.add(newAccount); updatePortfolioSummary()
            }
        }
        showAddInstrumentDialogForAccount?.let { account ->
            AddInstrumentDialog(
                account.accountName,
                account.accountType,
                onDismiss = {
                    showAddInstrumentDialogForAccount = null
                }) { symbol, quantityStr, priceStr ->
                val quantity = quantityStr.toDoubleOrNull() ?: 0.0;
                val price = priceStr.toDoubleOrNull() ?: 0.0; if (quantity > 0 && price > 0) {
                val newInstrument = Instrument(
                    "inst_${System.currentTimeMillis()}",
                    "$symbol Name",
                    symbol.uppercase(),
                    price,
                    Random.nextDouble(-5.0, 5.0),
                    type = account.accountType,
                    icon = when (account.accountType) {
                        PortfolioAssetType.STOCKS -> Icons.Filled.ShowChart; PortfolioAssetType.BONDS -> Icons.Filled.AccountBalance; PortfolioAssetType.CRYPTO -> Icons.Filled.CurrencyBitcoin; else -> null
                    }
                ); account.instruments.add(
                    HeldInstrument(
                        newInstrument,
                        quantity,
                        price
                    )
                ); updatePortfolioSummary()
            }
            }
        }
        showDeleteConfirmationForAccount?.let { account ->
            ConfirmDeleteDialog(
                account.accountName,
                "account",
                onDismiss = {
                    showDeleteConfirmationForAccount = null
                }) { accounts.remove(account); updatePortfolioSummary() }
        }
        showDeleteConfirmationForInstrument?.let { (account, instrument) ->
            ConfirmDeleteDialog(
                instrument.instrument.name,
                "instrument holding",
                onDismiss = {
                    showDeleteConfirmationForInstrument = null
                }) { account.instruments.remove(instrument); updatePortfolioSummary() }
        }
    }
}

// --- Previews ---
@Preview(showBackground = true, name = "Portfolio Screen - Light")
@Composable
fun PortfolioScreenPreviewLight() {
    MaterialTheme { Surface { PortfolioOverviewScreen() } }
}

@Preview(showBackground = true, name = "Portfolio Screen - Dark")
@Composable
fun PortfolioScreenPreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) { Surface { PortfolioOverviewScreen() } }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 250)
@Composable
fun AddAccountDialogPreview() {
    MaterialTheme { AddAccountDialog(PortfolioAssetType.STOCKS, {}, {}) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 300)
@Composable
fun AddInstrumentDialogPreview() {
    MaterialTheme {
        AddInstrumentDialog(
            "My Stocks Account",
            PortfolioAssetType.STOCKS,
            {},
            { _, _, _ -> })
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
fun ConfirmDeleteDialogPreview() {
    MaterialTheme { ConfirmDeleteDialog("Bitcoin Holding", "instrument", {}, {}) }
}
