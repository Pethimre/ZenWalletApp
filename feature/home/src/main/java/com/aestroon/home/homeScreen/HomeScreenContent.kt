package com.aestroon.home.homeScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aestroon.common.data.entity.CategoryEntity
import com.aestroon.common.data.entity.PlannedPaymentEntity
import com.aestroon.common.data.entity.TransactionEntity
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.model.WalletsSummary
import com.aestroon.common.theme.GreenChipColor
import com.aestroon.common.theme.OrangeChipColor
import com.aestroon.common.theme.RedChipColor
import com.aestroon.common.utilities.TextFormatter
import com.aestroon.common.utilities.formatDayAndMonth
import com.aestroon.home.widgets.balanceOverview.BalanceOverviewCard
import com.aestroon.home.widgets.savingSummary.SavingsSummaryCard
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun LazyListScope.addHomeScreenContent(
    summary: WalletsSummary,
    worthGoal: Long,
    worthGoalCurrency: String,
    monthlyProgress: Double,
    dailyTransactions: List<TransactionEntity>,
    upcomingTransactions: List<TransactionEntity>,
    overdueTransactions: List<TransactionEntity>,
    categoriesMap: Map<String, CategoryEntity>,
    baseCurrency: String,
    exchangeRates: Map<String, Double>?,
    onEdit: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    onPayPlanned: (PlannedPaymentEntity) -> Unit,
    onSkipPlanned: (PlannedPaymentEntity) -> Unit,
    allUpcoming: List<PlannedPaymentEntity>,
    allOverdue: List<PlannedPaymentEntity>,
    currentMonthIncome: Double,
    currentMonthExpense: Double,
) {
    item(key = "balance_overview_card") {
        BalanceOverviewCard(
            totalBalance = summary.totalBalance / 100.0,
            worthGoal = worthGoal,
            worthGoalCurrency = worthGoalCurrency,
            monthlyProgress = monthlyProgress,
            baseCurrency = baseCurrency,
            exchangeRates = exchangeRates,
        )
    }

    item(key = "savings_summary_card") {
        SavingsSummaryCard(
            income = TextFormatter.toPrettyAmountWithCurrency(currentMonthIncome, baseCurrency, currencyPosition = TextFormatter.CurrencyPosition.AFTER),
            expense = TextFormatter.toPrettyAmountWithCurrency(currentMonthExpense, baseCurrency, currencyPosition = TextFormatter.CurrencyPosition.AFTER),
            savingsGoalPercentage = if (currentMonthIncome > 0) ((currentMonthIncome - currentMonthExpense) / currentMonthIncome).toFloat() else 0f
        )
    }

    if (upcomingTransactions.isNotEmpty()) {
        item(key = "upcoming_transactions_section") {
            CollapsibleSectionCard(
                title = "Upcoming",
                transactions = upcomingTransactions,
                headerBackgroundColor = OrangeChipColor.copy(alpha = 0.3f),
                isInitiallyExpanded = false,
                categoriesMap = categoriesMap,
                isPlanned = true,
                baseCurrency = baseCurrency,
                exchangeRates = exchangeRates,
                onEdit = onEdit,
                onDelete = onDelete,
                onPay = { transaction -> allUpcoming.find { it.id == transaction.id }?.let(onPayPlanned) },
                onSkip = { transaction -> allUpcoming.find { it.id == transaction.id }?.let(onSkipPlanned) }
            )
        }
    }

    if (overdueTransactions.isNotEmpty()) {
        item(key = "overdue_transactions_section") {
            CollapsibleSectionCard(
                title = "Overdue",
                transactions = overdueTransactions,
                headerBackgroundColor = RedChipColor.copy(alpha = 0.3f),
                isInitiallyExpanded = false,
                categoriesMap = categoriesMap,
                isPlanned = true,
                baseCurrency = baseCurrency,
                exchangeRates = exchangeRates,
                onEdit = onEdit,
                onDelete = onDelete,
                onPay = { transaction -> allOverdue.find { it.id == transaction.id }?.let(onPayPlanned) },
                onSkip = { transaction -> allOverdue.find { it.id == transaction.id }?.let(onSkipPlanned) }
            )
        }
    }

    if (dailyTransactions.isEmpty()) {
        item(key = "no_transactions_fallback") {
            Text(
                text = "No transactions found.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val groupedDailyTransactions: Map<Long, List<TransactionEntity>> =
        dailyTransactions.sortedByDescending { it.date }.groupBy {
            val cal = Calendar.getInstance().apply { time = it.date }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }

    groupedDailyTransactions.forEach { (dateMillis, transactionsOnDay) ->
        val date = Date(dateMillis)
        item(key = "daily_header_$dateMillis") {
            DayTransactionHeader(
                date = date,
                transactionsOnDay = transactionsOnDay,
                baseCurrency = baseCurrency,
                exchangeRates = exchangeRates
            )
        }

        items(transactionsOnDay, key = { "daily_tx_${it.id}" }) { transaction ->
            TransactionCard(
                transaction = transaction,
                category = transaction.categoryId?.let { categoriesMap[it] },
                isPlanned = false,
                baseCurrency = baseCurrency,
                exchangeRates = exchangeRates,
                onEditClick = { onEdit(transaction) },
                onDeleteClick = { onDelete(transaction) },
                onPayClick = {},
                onSkipClick = {},
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
fun CollapsibleSectionCard(
    title: String,
    transactions: List<TransactionEntity>,
    headerBackgroundColor: Color,
    isInitiallyExpanded: Boolean,
    categoriesMap: Map<String, CategoryEntity>,
    isPlanned: Boolean,
    baseCurrency: String,
    exchangeRates: Map<String, Double>?,
    onEdit: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    onPay: (TransactionEntity) -> Unit,
    onSkip: (TransactionEntity) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(isInitiallyExpanded) }
    val rates = exchangeRates

    val convertToBaseCurrency: (TransactionEntity) -> Double = { txn ->
        val amount = txn.amount / 100.0
        if (txn.currency == baseCurrency || rates == null) {
            amount
        } else {
            val rate = rates[txn.currency]
            if (rate != null && rate != 0.0) amount / rate else 0.0
        }
    }

    // Calculate totals for each type
    val incomeInBase = transactions
        .filter { it.transactionType == TransactionType.INCOME }
        .sumOf(convertToBaseCurrency)

    val expenseInBase = transactions
        .filter { it.transactionType == TransactionType.EXPENSE }
        .sumOf(convertToBaseCurrency)

    val transferInBase = transactions
        .filter { it.transactionType == TransactionType.TRANSFER }
        .sumOf(convertToBaseCurrency)


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(headerBackgroundColor)
            .clickable { isExpanded = !isExpanded }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (incomeInBase > 0) {
                    Icon(
                        Icons.Default.KeyboardDoubleArrowUp,
                        contentDescription = "Income",
                        tint = GreenChipColor,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp)
                    )
                    Text(
                        "${TextFormatter.toPrettyAmount(incomeInBase)} $baseCurrency",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.width(8.dp))
                }
                if (expenseInBase > 0) {
                    Icon(
                        Icons.Default.KeyboardDoubleArrowDown,
                        contentDescription = "Expense",
                        tint = RedChipColor,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp)
                    )
                    Text(
                        "${TextFormatter.toPrettyAmount(expenseInBase)} $baseCurrency",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.width(8.dp))
                }
                if (transferInBase > 0) {
                    Icon(
                        Icons.Default.CompareArrows,
                        contentDescription = "Transfer",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp)
                    )
                    Text(
                        "${TextFormatter.toPrettyAmount(transferInBase)} $baseCurrency",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title)
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand section"
                )
            }
        }
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                if (transactions.isEmpty()) {
                    Text(
                        "No $title items.",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    transactions.forEach { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            category = transaction.categoryId?.let { categoriesMap[it] },
                            isPlanned = isPlanned,
                            baseCurrency = baseCurrency,
                            exchangeRates = exchangeRates,
                            onEditClick = { onEdit(transaction) },
                            onDeleteClick = { onDelete(transaction) },
                            onPayClick = { onPay(transaction) },
                            onSkipClick = { onSkip(transaction) },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    isPlanned: Boolean,
    baseCurrency: String,
    exchangeRates: Map<String, Double>?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPayClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val amountColor = when (transaction.transactionType) {
        TransactionType.INCOME -> GreenChipColor
        TransactionType.EXPENSE -> MaterialTheme.colorScheme.onSurface
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.primary
    }
    val simpleDateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val convertedAmount = remember(transaction, baseCurrency, exchangeRates) {
        if (exchangeRates == null || transaction.currency == baseCurrency) return@remember null
        val baseRate = exchangeRates[baseCurrency] ?: return@remember null
        val walletRate = exchangeRates[transaction.currency] ?: return@remember null
        (transaction.amount / 100.0) * (baseRate / walletRate)
    }

    Card(
        modifier = modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(transaction.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            transaction.description?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (isPlanned) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Due: ${simpleDateFormat.format(transaction.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                category?.let { CategoryChip(it) }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (transaction.transactionType) {
                    TransactionType.INCOME -> Icon(Icons.Default.ArrowOutward, contentDescription = "income", Modifier.padding(end = 4.dp).rotate(180f), tint = GreenChipColor)
                    TransactionType.EXPENSE -> Icon(Icons.Default.ArrowOutward, contentDescription = "expense", Modifier.padding(end = 4.dp))
                    TransactionType.TRANSFER -> Icon(Icons.Default.CompareArrows, contentDescription = "transfer", Modifier.padding(end = 4.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${TextFormatter.toBasicFormat(transaction.amount / 100.0)} ${transaction.currency}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                    convertedAmount?.let {
                        Text(
                            text = "≈ ${TextFormatter.toPrettyAmount(it)} $baseCurrency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isPlanned) Arrangement.SpaceEvenly else Arrangement.End
                    ) {
                        if (isPlanned) {
                            Button(onClick = onPayClick, colors = ButtonDefaults.buttonColors(containerColor = GreenChipColor)) {
                                Text("Pay")
                            }
                            TextButton(onClick = onSkipClick) {
                                Text("Skip")
                            }
                        } else {
                            TextButton(onClick = onEditClick) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Edit")
                            }
                            TextButton(onClick = onDeleteClick, colors = ButtonDefaults.textButtonColors(contentColor = RedChipColor)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(category: CategoryEntity) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(category.composeColor.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = category.composeColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = category.name,
            color = category.composeColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DayTransactionHeader(
    date: Date,
    transactionsOnDay: List<TransactionEntity>,
    baseCurrency: String,
    exchangeRates: Map<String, Double>?,
    locale: Locale = Locale.getDefault()
) {
    val dailyNetInBaseCurrency = remember(transactionsOnDay, baseCurrency, exchangeRates) {
        if (exchangeRates == null) return@remember 0.0

        transactionsOnDay.sumOf { transaction ->
            val amount = when (transaction.transactionType) {
                TransactionType.INCOME -> transaction.amount / 100.0
                TransactionType.EXPENSE -> -transaction.amount / 100.0
                TransactionType.TRANSFER -> 0.0
            }

            if (transaction.currency == baseCurrency) {
                amount
            } else {
                val rate = exchangeRates[transaction.currency] ?: 0.0
                if (rate != 0.0) amount / rate else 0.0
            }
        }
    }

    val currencyFormatter = remember { DecimalFormat("#,##0.00") }
    val netAmountColor = when {
        dailyNetInBaseCurrency > 0 -> GreenChipColor
        dailyNetInBaseCurrency < 0 -> RedChipColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = formatDayAndMonth(date, locale),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (transactionsOnDay.isNotEmpty()) {
                Text(
                    text = "${if (dailyNetInBaseCurrency >= 0) "+" else ""}${currencyFormatter.format(dailyNetInBaseCurrency)} $baseCurrency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = netAmountColor
                )
            }
        }
        HorizontalDivider(Modifier.fillMaxWidth())
    }
}
