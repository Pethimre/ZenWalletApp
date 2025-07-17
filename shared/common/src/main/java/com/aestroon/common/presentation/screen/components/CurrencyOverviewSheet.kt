package com.aestroon.common.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aestroon.common.data.serializable.Currency
import com.aestroon.common.presentation.state.ExchangeRateUiState
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyOverviewSheet(
    uiState: ExchangeRateUiState,
    allCurrencies: List<Currency>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState) {
                is ExchangeRateUiState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Fetching Rates...")
                }
                is ExchangeRateUiState.Success -> {
                    val ratesResponse = uiState.rates
                    Text(
                        "Value of 1 Foreign Currency in ${ratesResponse.base_code}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(ratesResponse.conversion_rates.toList()) { (code, rate) ->
                            val currencyName = allCurrencies.find { it.code == code }?.name ?: code
                            val inverseRate = 1 / rate
                            RateItem(currencyName, code, inverseRate, ratesResponse.base_code)
                        }
                    }
                }
                is ExchangeRateUiState.Error -> {
                    Icon(Icons.Default.ErrorOutline, "Error", modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(uiState.message, textAlign = TextAlign.Center)
                }
                is ExchangeRateUiState.Idle -> {
                    Text("Select a currency to see rates.")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    }
}

@Composable
fun RateItem(name: String, code: String, inverseRate: Double, baseCode: String) {
    val formatter = DecimalFormat("#,##0.00####")
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$code ($name)", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text("${formatter.format(inverseRate)} $baseCode", style = MaterialTheme.typography.bodyLarge)
    }
}