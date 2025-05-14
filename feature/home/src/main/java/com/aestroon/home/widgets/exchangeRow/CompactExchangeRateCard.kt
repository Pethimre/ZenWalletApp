package com.aestroon.home.widgets.exchangeRow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // For trend icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aestroon.home.mockProvider.CurrencyExchangeInfo
import com.aestroon.home.mockProvider.RateTrend
import com.aestroon.home.mockProvider.sampleExchangeRates

@Composable
fun CompactExchangeRateCard(
    modifier: Modifier = Modifier,
    rates: List<CurrencyExchangeInfo>,
    baseCurrencySymbol: String = "Ft" // For HUF
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), // Slightly smaller radius for a compact feel
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer // A neutral, slightly off-background color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)) {
            Text(
                text = "Exchange Rates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            rates.forEachIndexed { index, rateInfo ->
                ExchangeRateRow(
                    info = rateInfo,
                    baseCurrencySymbol = baseCurrencySymbol
                )
                if (index < rates.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp, // Thin divider for compactness
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
        // Currency Icon and Code
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.5f)) {
            Icon(
                imageVector = info.icon,
                contentDescription = info.currencyName,
                modifier = Modifier.size(20.dp), // Compact icon size
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

        // Rate in HUF
        Text(
            text = "${String.format("%,.2f", info.rateInHUF)} $baseCurrencySymbol",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(2f, fill = false), // Allow shrinking but prefer its content size
            textAlign = TextAlign.End
        )

        // Trend Icon
        Box(modifier = Modifier.weight(0.5f, fill = false).padding(start = 8.dp), contentAlignment = Alignment.CenterEnd) {
            val trendIconAndColor = when (info.trend) {
                RateTrend.UP -> Icons.Filled.ArrowDropUp to MaterialTheme.colorScheme.tertiary // Often a greenish hue
                RateTrend.DOWN -> Icons.Filled.ArrowDropDown to MaterialTheme.colorScheme.error
                RateTrend.STABLE -> null // No icon for stable
            }

            trendIconAndColor?.let { (icon, color) ->
                Icon(
                    imageVector = icon,
                    contentDescription = "Trend: ${info.trend.name}",
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
            } ?: Spacer(modifier = Modifier.width(24.dp)) // Keep space if no icon
        }
    }
}

// Preview for the component
@Preview(showBackground = true, widthDp = 340)
@Composable
fun CompactExchangeRateCardPreview() {
    MaterialTheme {
        CompactExchangeRateCard(
            rates = sampleExchangeRates
        )
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
fun CompactExchangeRateCardDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        CompactExchangeRateCard(
            rates = sampleExchangeRates
        )
    }
}