package com.aestroon.home.dailyTransactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aestroon.common.components.TransactionListItem
import com.aestroon.common.components.mockProvider.TransactionItemData
import com.aestroon.common.data.entity.TransactionEntity
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.theme.GreenChipColor
import com.aestroon.common.theme.RedChipColor
import com.aestroon.common.utilities.formatDayAndMonth
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.collections.first
import kotlin.collections.isNotEmpty

fun formatDayAndMonth(date: Date, locale: Locale): String {
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
    val target = Calendar.getInstance().apply { time = date }

    return when {
        today.get(Calendar.YEAR) == target.get(Calendar.YEAR) && today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR) -> "Today"
        yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) && yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
        else -> SimpleDateFormat("MMMM d", locale).format(date)
    }
}

fun LazyListScope.dailyTransactionItems(
    transactions: List<TransactionEntity>,
    locale: Locale = Locale.getDefault(),
    onTransactionClick: ((transactionId: String) -> Unit)? = null
) {
    val groupedTransactions: Map<Long, List<TransactionEntity>> =
        transactions.sortedByDescending { it.date }.groupBy {
            val cal = Calendar.getInstance().apply {
                time = it.date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }

    if (groupedTransactions.isEmpty()) {
        item {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No transactions to display.", style = MaterialTheme.typography.bodyLarge)
            }
        }
        return
    }

    groupedTransactions.forEach { (dateMillis, dailyTransactions) ->
        val date = Date(dateMillis)
        item(key = "header_$dateMillis") {
            Column(Modifier.padding(horizontal = 16.dp)) {
                DayTransactionHeader(date = date, transactionsOnDay = dailyTransactions, locale = locale)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        items(items = dailyTransactions, key = { transaction -> "transaction_${transaction.id}" }) { transaction ->
            Box(Modifier.padding(horizontal = 12.dp)) {
                TransactionListItem(
                    transaction = transaction,
                    onClick = { onTransactionClick?.invoke(transaction.id) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun DayTransactionHeader(
    date: Date,
    transactionsOnDay: List<TransactionEntity>,
    locale: Locale
) {
    val dailyNet = transactionsOnDay.sumOf {
        when (it.transactionType) {
            TransactionType.INCOME -> it.amount
            TransactionType.EXPENSE -> -it.amount
            TransactionType.TRANSFER -> 0
        }
    }
    val currencyFormatter = remember { DecimalFormat("#,##0.00") }
    val netAmountColor = when {
        dailyNet > 0 -> GreenChipColor
        dailyNet < 0 -> RedChipColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                text = "${if (dailyNet >= 0) "+" else ""}${currencyFormatter.format(dailyNet / 100.0)} ${transactionsOnDay.first().currency}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = netAmountColor
            )
        }
    }
}