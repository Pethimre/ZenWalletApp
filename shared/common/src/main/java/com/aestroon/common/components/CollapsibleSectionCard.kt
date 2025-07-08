package com.aestroon.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aestroon.common.components.mockProvider.TransactionTag
import com.aestroon.common.components.mockProvider.sampleDailyTransactions
import com.aestroon.common.components.mockProvider.sampleOverdueTransactions
import com.aestroon.common.components.mockProvider.sampleUpcomingTransactions
import com.aestroon.common.data.entity.TransactionEntity
import com.aestroon.common.data.entity.TransactionType.EXPENSE
import com.aestroon.common.data.entity.TransactionType.INCOME
import com.aestroon.common.data.entity.TransactionType.TRANSFER
import com.aestroon.common.theme.DarkGreenChipColor
import com.aestroon.common.theme.GreenChipColor
import com.aestroon.common.theme.RedChipColor
import com.aestroon.common.utilities.TextFormatter

@Composable
fun CollapsibleSectionCard(
    modifier: Modifier = Modifier,
    title: String,
    summary: @Composable RowScope.() -> Unit,
    headerBackgroundColor: Color,
    headerContentColor: Color,
    initiallyExpanded: Boolean = false,
    onHeaderClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "expand_icon_rotation"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isExpanded) 0.dp else 12.dp,
                        bottomEnd = if (isExpanded) 0.dp else 12.dp
                    )
                )
                .background(headerBackgroundColor)
                .clickable {
                    isExpanded = !isExpanded
                    onHeaderClick?.invoke()
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            summary()
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = headerContentColor,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Filled.UnfoldLess else Icons.Default.UnfoldMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = headerContentColor,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotationAngle)
            )
        }

        // Animated Content Area
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .background(headerBackgroundColor.copy(alpha = 0.1f))
                    .padding(16.dp),
                content = content
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (transaction.transactionType) {
        INCOME -> Icons.Default.ArrowUpward to GreenChipColor
        EXPENSE -> Icons.Default.ArrowDownward to RedChipColor
        TRANSFER -> Icons.Default.CompareArrows to Color.Gray
    }

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = transaction.transactionType.name,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp).background(color, CircleShape).padding(8.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(transaction.name, fontWeight = FontWeight.SemiBold)
                    transaction.description?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Text(
                text = TextFormatter.toPrettyAmountWithCurrency(
                    amount = transaction.amount / 100.0,
                    currency = transaction.currency,
                    currencyPosition = TextFormatter.CurrencyPosition.AFTER,
                ),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                color = color
            )
        }
    }
}

@Composable
fun ExpandableTransactionHeader(income: String, expense: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            tint = DarkGreenChipColor,
        )
        Text(
            income,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.width(4.dp))

        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = Color.Red
        )
        Text(
            expense,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true, name = "Collapsible Card - Upcoming")
@Composable
fun CollapsibleUpcomingPreview() {
    MaterialTheme {
        CollapsibleSectionCard(
            title = "Upcoming",
            summary = {
                ExpandableTransactionHeader(income = "812.00 HUF", expense = "362.00 HUF")
            },
            headerBackgroundColor = Color(0xFFFFA94D).copy(alpha = 0.3f),
            headerContentColor = Color.Black,
            initiallyExpanded = true,
            modifier = Modifier.padding(16.dp)
        ) {
            sampleUpcomingTransactions.forEach { transaction ->
                TransactionListItem(
                    transaction = transaction,
                    modifier = Modifier.padding(bottom = 8.dp),
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Collapsible Card - Overdue")
@Composable
fun CollapsibleOverduePreview() {
    MaterialTheme {
        CollapsibleSectionCard(
            title = "Overdue",
            summary = {
                ExpandableTransactionHeader(income = "4812.00 HUF", expense = "1362.00 HUF")
            },
            headerBackgroundColor = RedChipColor.copy(alpha = .3f),
            headerContentColor = Color.Black,
            initiallyExpanded = false,
            modifier = Modifier.padding(16.dp)
        ) {
            sampleOverdueTransactions.forEach { transaction ->
                TransactionListItem(
                    transaction = transaction,
                    modifier = Modifier.padding(bottom = 8.dp),
                    onClick = {},
                )
            }
            if (sampleOverdueTransactions.isEmpty()) {
                Text(
                    "No overdue items.",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Daily Transactions List")
@Composable
fun DailyTransactionsPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "May 16.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "186.36 HUF",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF28A745)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            items(sampleDailyTransactions) { transaction ->
                TransactionListItem(transaction = transaction, onClick = {})
            }
        }
    }
}

@Preview(showBackground = true, name = "Single Transaction Item")
@Composable
fun SingleTransactionItemPreview() {
    MaterialTheme {
        TransactionListItem(
            transaction = sampleUpcomingTransactions.first(),
            modifier = Modifier.padding(16.dp),
            onClick = {},
        )
    }
}
