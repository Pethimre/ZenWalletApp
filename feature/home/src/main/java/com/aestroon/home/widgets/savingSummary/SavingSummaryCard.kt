package com.aestroon.home.widgets.savingSummary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aestroon.common.theme.PrimaryColor
import com.aestroon.common.utilities.TextFormatter

@Composable
fun SavingsSummaryCard(
    modifier: Modifier = Modifier,
    income: String,
    expense: String,
    savingsGoalPercentage: Float,
    goalIcon: ImageVector = Icons.Filled.AccountBalanceWallet,
    revenueIcon: ImageVector = Icons.Filled.TrendingUp,
    expenseIcon: ImageVector = Icons.Filled.TrendingDown,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(end = 16.dp)
            ) {
                CircularProgress(
                    percentage = savingsGoalPercentage,
                    icon = goalIcon,
                    percentageIndicator = true,
                    foregroundColor = PrimaryColor,
                    backgroundColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 80.dp)
                ) {
                    Text(
                        text = "Cashflow rate",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            }

            Divider(
                modifier = Modifier
                    .height(130.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
            )

            Column(
                modifier = Modifier
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                InfoItem(
                    icon = revenueIcon,
                    label = "Income",
                    value = income,
                    iconColor = Color.Green,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(16.dp))
                InfoItem(
                    icon = expenseIcon,
                    label = "Expense",
                    value = expense,
                    iconColor = Color.Red,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun CircularProgress(
    percentage: Float,
    icon: ImageVector?,
    size: Dp = 72.dp,
    strokeWidth: Dp = 6.dp,
    foregroundColor: Color,
    backgroundColor: Color,
    percentageIndicator: Boolean = false,
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val actualStrokeWidth = strokeWidth.toPx()
            val radius = (this.size.minDimension - actualStrokeWidth) / 2
            val arcSize = Size(radius * 2, radius * 2)
            val topLeft = Offset(
                (this.size.width - arcSize.width) / 2,
                (this.size.height - arcSize.height) / 2
            )

            drawArc(
                color = backgroundColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = actualStrokeWidth),
            )

            drawArc(
                color = foregroundColor,
                startAngle = -90f,
                sweepAngle = 360 * percentage.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = actualStrokeWidth, cap = StrokeCap.Round),
            )
        }
        if (icon != null && !percentageIndicator) {
            Icon(
                imageVector = icon,
                contentDescription = "Savings Icon",
                modifier = Modifier.size(size / 2.5f),
                tint = foregroundColor,
            )
        }
        if (percentageIndicator) {
            Text(
                TextFormatter.formatPercentage((percentage * 100).toDouble()),
                color = if (percentage > 0) Color.Green else Color.Red
            )
        }
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    textColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(28.dp),
            tint = iconColor,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                color = textColor.copy(alpha = 0.8f),
                fontSize = 12.sp,
            )
            Text(
                text = value,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
    }
}