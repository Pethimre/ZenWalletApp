package com.aestroon.wallets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aestroon.common.theme.AppBlack
import com.aestroon.common.theme.GreenChipColor
import com.aestroon.common.theme.RedChipColor
import com.aestroon.common.utilities.TextFormatter
import com.aestroon.wallets.mockProvider.MonthlyFlow
import com.aestroon.wallets.mockProvider.SpendingWallet
import com.aestroon.wallets.mockProvider.WalletType
import com.aestroon.wallets.mockProvider.WalletsScreenSummary
import com.aestroon.wallets.mockProvider.defaultWalletColors
import com.aestroon.wallets.mockProvider.generateRandomColor
import com.aestroon.wallets.mockProvider.mockMiniTransactions
import com.aestroon.wallets.mockProvider.mockSpendingWallets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class WalletsScreenCarouselItems(name: String){
    OVERVIEW("Overview"),
    WALLETS("Wallets"),
}

@Composable
fun PieChart(
    data: List<Pair<String, Float>>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 20.dp,
    chartRadiusRatio: Float = 0.8f
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No data for chart.")
        }
        return
    }

    val total = data.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(0.0001f)

    Box(
        modifier = modifier
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val size = size.minDimension
            val center = Offset(size / 2, size / 2)
            val radius = size / 2 * chartRadiusRatio
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)

            var startAngle = -90f

            data.forEachIndexed { index, item ->
                val sweepAngle = (item.second / total) * 360f
                if (sweepAngle > 0f) {
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = stroke
                    )
                }
                startAngle += sweepAngle
            }
        }
    }
}

@Composable
fun BarChart(
    data: List<MonthlyFlow>,
    modifier: Modifier = Modifier,
    barWidthFraction: Float = 0.6f,
    incomeColor: Color = Color(0xFF28A745),
    expenseColor: Color = MaterialTheme.colorScheme.error
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("No monthly data.") } // Pass modifier
        return
    }

    val maxIncomeOrExpense = remember(data) {
        data.maxOfOrNull { maxOf(it.income, it.expense) }?.toFloat()
            ?: 1f // Default to 1f to avoid division by zero if all values are 0
    }
    // Define a fixed max bar height within the chart area to prevent bars from exceeding Row's height
    val chartAreaMaxHeight = 130.dp


    Row(
        modifier = modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { flow ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.height(150.dp), // Total height for bars + label space
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Calculate height based on the chartAreaMaxHeight
                    val incomeHeight =
                        if (maxIncomeOrExpense > 0) (flow.income.toFloat() / maxIncomeOrExpense) * chartAreaMaxHeight.value else 0
                    val expenseHeight =
                        if (maxIncomeOrExpense > 0) (flow.expense.toFloat() / maxIncomeOrExpense) * chartAreaMaxHeight.value else 0

                    Box(
                        modifier = Modifier
                            .height(incomeHeight.toInt().coerceAtLeast(0).dp)
                            .widthIn(min = 8.dp) // Ensure minimum visible width
                            .weight(barWidthFraction / 2)
                            .background(
                                incomeColor,
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .height(expenseHeight.toInt().coerceAtLeast(0).dp)
                            .widthIn(min = 8.dp)
                            .weight(barWidthFraction / 2)
                            .background(
                                expenseColor,
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = flow.monthYear.split(" ").firstOrNull() ?: flow.monthYear,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun OverallSummaryCard(summary: WalletsScreenSummary, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Wallets Overview",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))

            SummaryRow(
                "Total Balance:",
                TextFormatter.toBasicFormat(summary.totalBalance),
                valueColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            SummaryRow(
                "Total Monthly Income:",
                TextFormatter.toBasicFormat(summary.totalMonthlyIncome),
                valueColor = GreenChipColor,
            )
            SummaryRow(
                "Total Monthly Expense:",
                TextFormatter.toBasicFormat(summary.totalMonthlyExpense),
                valueColor = RedChipColor,
            )

            if (summary.balanceBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Balance Breakdown:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                PieChart(
                    data = summary.balanceBreakdown.map { it.first.name to it.second },
                    colors = summary.balanceBreakdown.map { it.first.color },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    summary.balanceBreakdown.take(3).forEach { (wallet, _) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(wallet.color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                wallet.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    if (summary.balanceBreakdown.size > 3) Text(
                        "...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (summary.recentMonthlyFlows.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Monthly Cash Flow (Last ${summary.recentMonthlyFlows.size} Months):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                BarChart(data = summary.recentMonthlyFlows, modifier = Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    LegendItem(color = Color(0xFF28A745), label = "Income")
                    Spacer(modifier = Modifier.width(16.dp))
                    LegendItem(color = MaterialTheme.colorScheme.error, label = "Expense")
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(10.dp)
            .background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}


@Composable
fun SpendingWalletCard(
    wallet: SpendingWallet,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = wallet.color.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier
            .padding(16.dp)
            .animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(CircleShape).background(AppBlack), contentAlignment = Alignment.Center){
                    Icon(
                        imageVector = wallet.type.icon,
                        contentDescription = wallet.type.displayName,
                        tint = wallet.color,
                        modifier = Modifier.size(32.dp).padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        wallet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        wallet.type.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    TextFormatter.toPrettyAmountWithCurrency(
                        wallet.balance,
                        wallet.currency,
                        false,
                        TextFormatter.CurrencyPosition.AFTER
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide Details" else "Show Details")
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Hide Details" else "Show Details"
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit Wallet",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Delete Wallet",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "Recent Activity (Mock):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (wallet.lastTransactions.isEmpty()) {
                        Text(
                            "No recent transactions.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        wallet.lastTransactions.take(3).forEach { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    tx.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    TextFormatter.toPrettyAmountWithCurrency(
                                        tx.amount,
                                        wallet.currency,
                                        false,
                                        TextFormatter.CurrencyPosition.AFTER
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (tx.amount >= 0) Color(0xFF28A745) else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWalletDialog(
    existingWallet: SpendingWallet? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: WalletType, balance: String, color: Color) -> Unit
) {
    var name by remember { mutableStateOf(existingWallet?.name ?: "") }
    var selectedType by remember { mutableStateOf(existingWallet?.type ?: WalletType.DEBIT_CARD) }
    var balanceStr by remember { mutableStateOf(existingWallet?.balance?.toString() ?: "") }
    var selectedColor by remember { mutableStateOf(existingWallet?.color ?: generateRandomColor()) }
    var showColorPicker by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }


    val availableWalletTypes = WalletType.entries.toList()

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier
                .padding(24.dp)
                .width(IntrinsicSize.Min)) {
                Text(
                    if (existingWallet == null) "Add New Wallet" else "Edit Wallet",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Wallet Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                // Corrected ExposedDropdownMenuBox for WalletType
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField( // This OutlinedTextField is the anchor
                        value = selectedType.displayName,
                        onValueChange = {}, // Value is changed by DropdownMenu selection
                        readOnly = true,
                        label = { Text("Wallet Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(), // Recommended colors
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(), // menuAnchor is important
                        interactionSource = remember { MutableInteractionSource() } // Often helps with focus/click issues
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        availableWalletTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = balanceStr,
                    onValueChange = { balanceStr = it },
                    label = { Text("Current Balance") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showColorPicker = !showColorPicker }) { // Toggle color picker
                    Text("Wallet Color:", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .size(24.dp)
                            .background(selectedColor, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }

                AnimatedVisibility(visible = showColorPicker) {
                    LazyRow(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        items(defaultWalletColors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(color, CircleShape)
                                    .clickable { selectedColor = color; showColorPicker = false; })
                        }
                    }
                }


                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onConfirm(
                            name,
                            selectedType,
                            balanceStr,
                            selectedColor
                        ); onDismiss()
                    }, enabled = name.isNotBlank() && balanceStr.toDoubleOrNull() != null) {
                        Text(if (existingWallet == null) "Add" else "Save")
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteWalletDialog(walletName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Wallet", color = MaterialTheme.colorScheme.error) },
        text = { Text("Are you sure you want to delete the wallet \"$walletName\"? This action cannot be undone and will remove all associated data.") },
        confirmButton = {
            Button(
                onClick = { onConfirm(); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete", color = MaterialTheme.colorScheme.onError) }
        }, // Ensure text color is contrasty
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


// --- Main Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(
    initialWallets: List<SpendingWallet> = mockSpendingWallets(5),
) {
    val wallets = remember { initialWallets.toMutableStateList() }
    var showAddEditDialogForWallet by remember { mutableStateOf<SpendingWallet?>(null) } // Stores wallet for edit, or a dummy for add intent
    var isAddingNewWallet by remember { mutableStateOf(false) } // Differentiates Add vs Edit mode for the dialog
    var showConfirmDeleteDialog by remember { mutableStateOf<SpendingWallet?>(null) }


    val summary = remember(wallets) { // Re-calculate summary when wallets list changes
        val totalBalance = wallets.sumOf { it.balance }
        val totalIncome = wallets.sumOf { it.monthlyIncome }
        val totalExpense = wallets.sumOf { it.monthlyExpense }
        val breakdown = if (totalBalance > 0) {
            wallets.map { it to (it.balance.toFloat() / totalBalance.toFloat()) } // Ensure float division
        } else {
            wallets.map { it to 0f }
        }
        val calendar = Calendar.getInstance()
        val flows = List(4) { i ->
            calendar.time = Date()
            calendar.add(Calendar.MONTH, -i)
            MonthlyFlow(
                monthYear = SimpleDateFormat(
                    "MMM yy",
                    Locale.getDefault()
                ).format(calendar.time), // Shortened month/year
                income = Random.nextDouble(100000.0, 800000.0),
                expense = Random.nextDouble(50000.0, 600000.0)
            )
        }.reversed()
        WalletsScreenSummary(totalBalance, totalIncome, totalExpense, breakdown, flows)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    isAddingNewWallet = true; showAddEditDialogForWallet = SpendingWallet(
                    "",
                    "",
                    WalletType.DEBIT_CARD,
                    0.0,
                    color = generateRandomColor()
                ) /* Dummy for add */
                },
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add New Wallet")
            }
        }
    ) { _ ->
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { WalletsScreenCarouselItems.entries.size })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    // Wallets Overview Page
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        OverallSummaryCard(summary = summary)
                    }
                }

                1 -> {
                    // Wallets List Page
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                "Your Wallets",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (wallets.isEmpty()) {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No wallets yet. Tap '+' to add one!", textAlign = TextAlign.Center)
                                }
                            }
                        } else {
                            items(wallets, key = { it.id }) { wallet ->
                                SpendingWalletCard(
                                    wallet = wallet,
                                    onEdit = {
                                        isAddingNewWallet = false
                                        showAddEditDialogForWallet = wallet
                                    },
                                    onDelete = { showConfirmDeleteDialog = wallet },
                                    onClick = { /* Navigate to detail */ }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialogs
        if (isAddingNewWallet || showAddEditDialogForWallet != null) {
            AddEditWalletDialog(
                existingWallet = if (isAddingNewWallet) null else showAddEditDialogForWallet, // Pass null for add, actual wallet for edit
                onDismiss = { showAddEditDialogForWallet = null; isAddingNewWallet = false },
                onConfirm = { name, type, balanceStr, color ->
                    val balance = balanceStr.toDoubleOrNull() ?: 0.0
                    if (isAddingNewWallet) {
                        wallets.add(
                            SpendingWallet(
                                id = "wallet_${System.currentTimeMillis()}",
                                name = name, type = type, balance = balance, color = color,
                                lastTransactions = mockMiniTransactions(Random.nextInt(0, 4))
                            )
                        )
                    } else {
                        showAddEditDialogForWallet?.let { existing ->
                            val index = wallets.indexOfFirst { it.id == existing.id }
                            if (index != -1) {
                                wallets[index] = existing.copy(
                                    name = name,
                                    type = type,
                                    balance = balance,
                                    color = color
                                )
                            }
                        }
                    }
                    showAddEditDialogForWallet = null
                    isAddingNewWallet = false
                }
            )
        }


        showConfirmDeleteDialog?.let { walletToDelete ->
            ConfirmDeleteWalletDialog(
                walletName = walletToDelete.name,
                onDismiss = { showConfirmDeleteDialog = null },
                onConfirm = {
                    wallets.remove(walletToDelete)
                    showConfirmDeleteDialog = null
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "Spending Wallets Screen - Light")
@Composable
fun SpendingWalletsScreenPreviewLight() {
    MaterialTheme { Surface { WalletsScreen() } }
}

@Preview(showBackground = true, name = "Spending Wallets Screen - Dark")
@Composable
fun SpendingWalletsScreenPreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) { Surface { WalletsScreen() } }
}

@Preview(showBackground = true, name = "Add Wallet Dialog - Add New")
@Composable
fun AddWalletDialogPreviewNew() {
    MaterialTheme {
        AddEditWalletDialog(
            existingWallet = null,
            onDismiss = {},
            onConfirm = { _, _, _, _ -> })
    }
}

@Preview(showBackground = true, name = "Add Wallet Dialog - Edit")
@Composable
fun AddWalletDialogPreviewEdit() {
    MaterialTheme {
        AddEditWalletDialog(
            existingWallet = mockSpendingWallets(1).first(),
            onDismiss = {},
            onConfirm = { _, _, _, _ -> })
    }
}
