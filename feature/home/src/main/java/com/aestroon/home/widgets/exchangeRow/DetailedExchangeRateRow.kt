package com.aestroon.home.widgets.exchangeRow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aestroon.home.mockProvider.CurrencyExchangeInfo
import com.aestroon.home.mockProvider.RateTrend

@Composable
internal fun DetailedExchangeRateRow( // Previously ExchangeRateRow or ExchangeRateRowInternal
    info: CurrencyExchangeInfo,
    baseCurrencySymbol: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Currency Icon and Code
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f) // Give it some weight
        ) {
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

        // Rate in HUF (aligned to end)
        Text(
            text = "${String.format("%,.2f", info.rateInHUF)} $baseCurrencySymbol",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f) // Give it some weight
        )

        // Trend Icon (aligned to end)
        Box(
            modifier = Modifier.width(40.dp).padding(start = 8.dp), // Fixed width for trend icon area
            contentAlignment = Alignment.CenterEnd
        ) {
            val trendIconAndColor = when (info.trend) {
                RateTrend.UP -> Icons.Filled.ArrowDropUp to MaterialTheme.colorScheme.tertiary // Often greenish
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
            } ?: Spacer(Modifier.width(24.dp)) // Maintain space if no icon
        }
    }
}