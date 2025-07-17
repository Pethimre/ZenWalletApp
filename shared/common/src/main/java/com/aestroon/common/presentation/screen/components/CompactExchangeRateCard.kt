package com.aestroon.common.presentation.screen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aestroon.common.data.model.CurrencyExchangeInfo
import com.aestroon.common.data.model.RateTrend

@Composable
fun CompactExchangeRateCard(
    modifier: Modifier = Modifier,
    rates: List<CurrencyExchangeInfo>,
    baseCurrencySymbol: String = "Ft"
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)) {
            rates.forEachIndexed { index, rateInfo ->
                ExchangeRateRow(
                    info = rateInfo,
                    baseCurrencySymbol = baseCurrencySymbol
                )
                if (index < rates.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExchangeRateRow(
    info: CurrencyExchangeInfo,
    baseCurrencySymbol: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.5f)) {
            Icon(
                imageVector = info.icon,
                contentDescription = info.currencyName,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = info.currencyCode,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = "${String.format("%,.2f", info.rateInBaseCurrency)} $baseCurrencySymbol",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(2f, fill = false),
            textAlign = TextAlign.End
        )

        // Trend Icon
        Box(modifier = Modifier.weight(0.5f, fill = false).padding(start = 8.dp), contentAlignment = Alignment.CenterEnd) {
            val trendIconAndColor = when (info.trend) {
                RateTrend.UP -> Icons.Filled.ArrowDropUp to MaterialTheme.colorScheme.tertiary
                RateTrend.DOWN -> Icons.Filled.ArrowDropDown to MaterialTheme.colorScheme.error
                RateTrend.STABLE -> null
            }

            trendIconAndColor?.let { (icon, color) ->
                Icon(
                    imageVector = icon,
                    contentDescription = "Trend: ${info.trend.name}",
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
            } ?: Spacer(modifier = Modifier.width(24.dp))
        }
    }
}
