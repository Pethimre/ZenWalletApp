package com.aestroon.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.items
import com.aestroon.profile.domain.ProfileSettingsUiState
import com.aestroon.profile.domain.ProfileViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyOverviewSheet(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit,
) {
    val ratesResponse by viewModel.exchangeRates.collectAsState()
    val uiState by viewModel.profileSettingsUiState.collectAsState()
    val allCurrencies by viewModel.allCurrencies.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Value of 1 Foreign Currency in ${ratesResponse?.base_code ?: ""}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState is ProfileSettingsUiState.Loading) {
                CircularProgressIndicator()
            } else if (ratesResponse != null) {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(ratesResponse!!.conversion_rates.toList()) { (code, rate) ->
                        ratesResponse?.base_code?.let {
                            val currencyName = allCurrencies.find { it.code == code }?.name ?: code
                            val inverseRate = 1 / rate
                            RateItem(currencyName, code, inverseRate, it)
                        }
                    }
                }
            } else {
                Text("Could not load exchange rates.")
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