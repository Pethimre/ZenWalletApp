package com.aestroon.common.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aestroon.common.data.model.CalculationResult
import com.aestroon.common.domain.CalculatorViewModel
import com.aestroon.common.domain.CompoundingFrequency
import com.aestroon.common.utilities.TextFormatter
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompoundInterestCalculatorScreen(
    onNavigateBack: () -> Unit,
    viewModel: CalculatorViewModel = koinViewModel()
) {
    var principalStr by remember { mutableStateOf("1000") }
    var monthlyContributionStr by remember { mutableStateOf("100") }
    var yearsStr by remember { mutableStateOf("10") }
    var rateStr by remember { mutableStateOf("7") }
    val result by viewModel.result.collectAsState()
    val baseCurrency by viewModel.baseCurrency.collectAsState()
    var frequency by remember { mutableStateOf(CompoundingFrequency.MONTHLY) }
    var isFrequencyDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.calculate(principalStr, monthlyContributionStr, yearsStr, rateStr, frequency)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compound Interest Calculator") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = principalStr,
                        onValueChange = { principalStr = it },
                        label = { Text("Initial Investment") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        suffix = { Text(baseCurrency) }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = monthlyContributionStr,
                        onValueChange = { monthlyContributionStr = it },
                        label = { Text("Monthly Contribution") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        suffix = { Text(baseCurrency) }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = yearsStr,
                        onValueChange = { yearsStr = it },
                        label = { Text("Years to Grow") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rateStr,
                        onValueChange = { rateStr = it },
                        label = { Text("Estimated Annual Interest Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        suffix = { Text("%") }
                    )
                    ExposedDropdownMenuBox(
                        expanded = isFrequencyDropdownExpanded,
                        onExpandedChange = { isFrequencyDropdownExpanded = !isFrequencyDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = frequency.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Compounding Frequency") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFrequencyDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isFrequencyDropdownExpanded,
                            onDismissRequest = { isFrequencyDropdownExpanded = false }
                        ) {
                            CompoundingFrequency.entries.forEach { selection ->
                                DropdownMenuItem(
                                    text = { Text(selection.displayName) },
                                    onClick = {
                                        frequency = selection
                                        isFrequencyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.calculate(
                                principalStr = principalStr,
                                monthlyContributionStr = monthlyContributionStr,
                                yearsStr = yearsStr,
                                rateStr = rateStr,
                                frequency = frequency
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Calculate")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Projected Growth", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            if (result.compoundGrowthData.isNotEmpty()) {
                AnimatedLineChart(
                    data = listOf(result.compoundGrowthData, result.linearGrowthData),
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = MaterialTheme.colorScheme.primary, label = "Compound Growth")
                    Spacer(Modifier.width(16.dp))
                    LegendItem(color = MaterialTheme.colorScheme.tertiary, label = "Linear Savings")
                }
            }

            Spacer(Modifier.height(16.dp))
            ResultSummaryCard(result = result, baseCurrency = baseCurrency)
        }
    }
}

@Composable
fun ResultSummaryCard(result: CalculationResult, baseCurrency: String) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ResultRow("Future Value:", TextFormatter.formatSimpleBalance(result.futureValue, baseCurrency))
            ResultRow("Total Contributions:", TextFormatter.formatSimpleBalance(result.totalContributions, baseCurrency))
            ResultRow("Total Interest Earned:", TextFormatter.formatSimpleBalance(result.totalInterest, baseCurrency), isInterest = true)
        }
    }
}

@Composable
fun ResultRow(label: String, value: String, isInterest: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isInterest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}