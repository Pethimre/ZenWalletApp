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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aestroon.common.data.serializable.Currency
import com.aestroon.common.domain.CurrencyListUiState
import com.aestroon.common.domain.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionScreen(
    viewModel: ProfileViewModel,
    onNavigateUp: () -> Unit,
) {
    val uiState by viewModel.currencyListUiState.collectAsState()
    val filteredCurrencies by viewModel.filteredCurrencies.collectAsState()
    val searchQuery by viewModel.currencySearchQuery.collectAsState()
    val selectedCurrency by viewModel.baseCurrency.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllCurrencies()
    }

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            when (uiState) {
                is CurrencyListUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CurrencyListUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = (uiState as CurrencyListUiState.Error).message,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadAllCurrencies() }) {
                            Text("Try Again")
                        }
                    }
                }
                is CurrencyListUiState.Success -> {
                    if (filteredCurrencies.isEmpty() && searchQuery.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("No currencies found for \"$searchQuery\"", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredCurrencies) { currency ->
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
        }
    }
}

@Composable
fun CurrencyItem(
    currency: Currency,
    isSelected: Boolean,
    onSelect: () -> Unit,
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
