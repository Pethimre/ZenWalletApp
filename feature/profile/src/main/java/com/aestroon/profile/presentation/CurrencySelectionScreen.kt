package com.aestroon.profile.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aestroon.profile.data.serializable.Currency
import com.aestroon.profile.domain.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionScreen(
    viewModel: ProfileViewModel,
    onNavigateUp: () -> Unit
) {
    val currencies by viewModel.filteredCurrencies.collectAsState()
    val searchQuery by viewModel.currencySearchQuery.collectAsState()
    val selectedCurrency by viewModel.baseCurrency.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Base Currency") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onCurrencySearchQueryChanged(it) },
                label = { Text("Search by name or code") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(currencies) { currency ->
                    CurrencyItem(
                        currency = currency,
                        isSelected = currency.code == selectedCurrency,
                        onSelect = {
                            viewModel.onBaseCurrencySelected(currency.code)
                            onNavigateUp()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CurrencyItem(
    currency: Currency,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = currency.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = currency.code, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
