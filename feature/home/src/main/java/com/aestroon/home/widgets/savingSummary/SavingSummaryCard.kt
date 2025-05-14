package com.aestroon.home.widgets.savingSummary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet // Placeholder for savings/car
import androidx.compose.material.icons.filled.LocalDining // Placeholder for food
import androidx.compose.material.icons.filled.TrendingUp // Placeholder for revenue
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

@Composable
fun SavingsSummaryCard(
    modifier: Modifier = Modifier,
    revenueLastWeek: String,
    foodLastWeek: String,
    savingsGoalPercentage: Float, // Value between 0f and 1f
    savingsIcon: ImageVector = Icons.Filled.AccountBalanceWallet, // Replace with your icon
    revenueIcon: ImageVector = Icons.Filled.TrendingUp,         // Replace with your icon
    foodIcon: ImageVector = Icons.Filled.LocalDining            // Replace with your icon
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer // Or primary for a more vibrant look like the image
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Savings Goal Circular Progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(0.4f) // Give more space to circular progress part
                    .padding(end = 16.dp)
            ) {
                CircularSavingsProgress(
                    percentage = savingsGoalPercentage,
                    icon = savingsIcon,
                    foregroundColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    backgroundColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 80.dp)) { // Adjust padding as needed
                    Text(
                        text = "Savings on Goals",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            }

            // Vertical Divider (optional, as in image)
            Divider(
                modifier = Modifier
                    .height(130.dp) // Adjust height as needed
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
            )

            // Revenue and Food Info
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                InfoItem(
                    icon = revenueIcon,
                    label = "Revenue Last Week",
                    value = revenueLastWeek,
                    iconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                InfoItem(
                    icon = foodIcon,
                    label = "Food Last Week",
                    value = foodLastWeek,
                    iconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun CircularSavingsProgress(
    percentage: Float,
    icon: ImageVector,
    size: Dp = 72.dp,
    strokeWidth: Dp = 6.dp,
    foregroundColor: Color,
    backgroundColor: Color
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

            // Background circle
            drawArc(
                color = backgroundColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = actualStrokeWidth)
            )

            // Foreground arc
            drawArc(
                color = foregroundColor,
                startAngle = -90f, // Start from the top
                sweepAngle = 360 * percentage.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = actualStrokeWidth, cap = StrokeCap.Round)
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = "Savings Icon",
            modifier = Modifier.size(size / 2.5f), // Adjust icon size relative to circle
            tint = foregroundColor
        )
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
            modifier = Modifier.size(28.dp), // Slightly smaller icon next to text
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                color = textColor.copy(alpha = 0.8f), // Slightly muted label
                fontSize = 12.sp
            )
            Text(
                text = value,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}