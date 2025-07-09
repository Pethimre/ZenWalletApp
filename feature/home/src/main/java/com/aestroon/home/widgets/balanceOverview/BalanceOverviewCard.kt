package com.aestroon.home.widgets.balanceOverview

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AssistantPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aestroon.common.theme.GreenChipColor
import com.aestroon.common.theme.RedChipColor
import com.aestroon.common.utilities.TextFormatter
import kotlin.math.abs

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BalanceOverviewCard(
    modifier: Modifier = Modifier,
    totalBalance: Double,
    worthGoal: Long,
    worthGoalCurrency: String,
    monthlyProgress: Double,
    baseCurrency: String,
    exchangeRates: Map<String, Double>?
) {
    val goalInCurrentBaseCurrency = remember(worthGoal, worthGoalCurrency, baseCurrency, exchangeRates) {
        val goalAsDouble = worthGoal.toDouble()
        if (exchangeRates == null || worthGoalCurrency == baseCurrency) {
            goalAsDouble
        } else {
            val baseRate = exchangeRates[baseCurrency]
            val goalRate = exchangeRates[worthGoalCurrency]
            if (baseRate != null && goalRate != null && goalRate != 0.0) {
                goalAsDouble * (baseRate / goalRate)
            } else {
                goalAsDouble
            }
        }
    }

    val amountUntilGoal = (goalInCurrentBaseCurrency - totalBalance).coerceAtLeast(0.0)
    val hasGoal = goalInCurrentBaseCurrency > 0
    val isGoalReached = hasGoal && totalBalance >= goalInCurrentBaseCurrency

    val totalProgress = if (hasGoal) (totalBalance / goalInCurrentBaseCurrency).toFloat() else 0f
    val monthlyProgressPercent = if (hasGoal) (monthlyProgress / goalInCurrentBaseCurrency).toFloat() else 0f
    val pastProgress = totalProgress - monthlyProgressPercent

    val statusMessage = "This month: ${if(monthlyProgress >= 0) "+" else ""}${TextFormatter.toPrettyAmountWithCurrency(monthlyProgress, baseCurrency)}"
    val monthlyProgressColor = if (monthlyProgress >= 0) GreenChipColor else RedChipColor

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                BalanceOrExpenseItem(
                    icon = Icons.Filled.AccountBalance,
                    label = "Total Balance",
                    value = TextFormatter.toPrettyAmountWithCurrency(totalBalance, baseCurrency),
                    valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Divider(
                    modifier = Modifier.height(50.dp).width(1.dp).padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                BalanceOrExpenseItem(
                    icon = Icons.Filled.AssistantPhoto,
                    label = if(isGoalReached) "Goal Reached!" else "Amount Until Goal:",
                    value = TextFormatter.toPrettyAmountWithCurrency(amountUntilGoal, baseCurrency),
                    valueColor = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (hasGoal && !isGoalReached) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${(totalProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .clip(CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pastProgress.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        if (monthlyProgress >= 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(totalProgress.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(monthlyProgressColor)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .fillMaxHeight()
                                    .padding(start = maxWidth * totalProgress.coerceIn(0f, 1f))
                                    .width(maxWidth * abs(monthlyProgressPercent))
                                    .background(monthlyProgressColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = TextFormatter.toPrettyAmountWithCurrency(goalInCurrentBaseCurrency, baseCurrency),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusIcon = if (isGoalReached) Icons.Filled.EmojiEvents else (if (monthlyProgress >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown)
                Icon(
                    imageVector = statusIcon,
                    contentDescription = "Status",
                    modifier = Modifier.size(18.dp),
                    tint = if(isGoalReached) Color(0xFFFFD700) else monthlyProgressColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if(isGoalReached) "Congratulations! Set a new goal to keep growing." else statusMessage,
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
            modifier = Modifier.size(20.dp),
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
                fontSize = 18.sp,
            )
        }
    }
}
