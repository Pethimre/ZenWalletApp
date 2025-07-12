package com.aestroon.home.widgets.exchangeRow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aestroon.common.data.model.CurrencyExchangeInfo
import com.aestroon.common.data.model.RateTrend
import com.aestroon.common.theme.GreenChipColor
import com.aestroon.common.theme.RedChipColor
import com.aestroon.common.utilities.TextFormatter

@Composable
internal fun CollapsedRatesDisplay(
    rates: List<CurrencyExchangeInfo>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        rates.forEach { rateInfo ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = rateInfo.icon,
                    contentDescription = rateInfo.currencyCode,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = TextFormatter.toBasicFormat(rateInfo.rateInBaseCurrency),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.UnfoldMore,
            contentDescription = "Expand",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
internal fun DetailedExchangeRateRow(
    info: CurrencyExchangeInfo,
    baseCurrencySymbol: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = info.icon,
                contentDescription = info.currencyName,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = info.currencyCode, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = info.currencyName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
            val trendIconAndColor = when (info.trend) {
                RateTrend.UP -> Icons.Filled.ArrowDropUp to GreenChipColor
                RateTrend.DOWN -> Icons.Filled.ArrowDropDown to RedChipColor
                RateTrend.STABLE -> null
            }
            trendIconAndColor?.let { (icon, color) ->
                Icon(imageVector = icon, contentDescription = "Trend: ${info.trend.name}", modifier = Modifier.size(24.dp), tint = color)
            }
            Text(
                text = "${TextFormatter.toPrettyAmount(info.rateInBaseCurrency)} $baseCurrencySymbol",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
        }
    }
}
