package com.aestroon.home.widgets.balanceOverview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance // Placeholder for Total Balance
import androidx.compose.material.icons.filled.CheckCircle // Placeholder for Checkmark
import androidx.compose.material.icons.filled.CreditCard // Placeholder for Total Expense
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BalanceOverviewCard(
    modifier: Modifier = Modifier,
    totalBalance: String,
    totalExpense: String,
    expenseTrackingValue: Float, // e.g. 20000.00f
    expenseProgress: Float, // Value between 0f and 1f (e.g., 0.3f for 30%)
    statusMessage: String,
    balanceIcon: ImageVector = Icons.Filled.AccountBalance, // Replace with your icon
    expenseIcon: ImageVector = Icons.Filled.CreditCard,   // Replace with your icon
    statusIcon: ImageVector = Icons.Filled.CheckCircle    // Replace with your icon
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // A light background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Balance and Expense Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top // Align items to the top of their cells
            ) {
                BalanceOrExpenseItem(
                    icon = balanceIcon,
                    label = "Total Balance",
                    value = totalBalance,
                    valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Vertical Divider
                Divider(
                    modifier = Modifier
                        .height(50.dp) // Adjust height
                        .width(1.dp)
                        .padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                BalanceOrExpenseItem(
                    icon = expenseIcon,
                    label = "Total Expense",
                    value = totalExpense,
                    valueColor = MaterialTheme.colorScheme.error // Use error color for expenses
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${(expenseProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(40.dp) // Fixed width for percentage
                )
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { expenseProgress.coerceIn(0f,1f) },
                    modifier = Modifier.weight(1f).height(12.dp), // Make progress bar taller
                    color = MaterialTheme.colorScheme.primary, // Progress color
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest, // Track color (lighter than surfaceVariant)
                    strokeCap = StrokeCap.Round // Rounded ends
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$${String.format("%,.2f", expenseTrackingValue)}", // Format the tracking value
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Message Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = "Status",
                    modifier = Modifier.size(18.dp),
                    tint = if (statusMessage.contains("Good", ignoreCase = true) || statusMessage.contains("Great", ignoreCase = true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant // Dynamic color
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BalanceOrExpenseItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp), // Smaller icon for this context
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = valueColor,
                fontSize = 18.sp // Match image size
            )
        }
    }
}
