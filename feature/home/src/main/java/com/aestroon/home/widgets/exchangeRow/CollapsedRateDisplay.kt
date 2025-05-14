package com.aestroon.home.widgets.exchangeRow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aestroon.home.mockProvider.CurrencyExchangeInfo

@Composable
internal fun CollapsedRatesDisplay(
    rates: List<CurrencyExchangeInfo>, // Subset of rates for collapsed view
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp), // Consistent padding
        horizontalArrangement = Arrangement.SpaceAround, // Distribute items evenly
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display a configured number of rates, e.g., the first 3
        rates.take(NUMBER_OF_CURRENCIES_ON_COMPACT).forEach { rateInfo ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = rateInfo.icon,
                    contentDescription = rateInfo.currencyCode,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, // Good contrast on light/dark
                    modifier = Modifier.size(22.dp) // Slightly larger icon for readability
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = String.format("%.2f", rateInfo.rateInHUF),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // Add an expand icon to suggest clickability
        Icon(
            imageVector = Icons.Filled.ExpandMore,
            contentDescription = "Expand for details",
            tint = MaterialTheme.colorScheme.primary, // Use primary color for interactive cue
            modifier = Modifier.size(24.dp)
        )
    }
}