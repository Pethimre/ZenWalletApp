package com.aestroon.common.presentation.screen

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.domain.LoansViewModel
import org.koin.androidx.compose.getViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanEntryScreen(
    loanId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: LoansViewModel = getViewModel()
    val loan by viewModel.getLoanById(loanId).collectAsState(initial = null)
    val wallets by viewModel.wallets.collectAsState()

    var note by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedWallet by remember { mutableStateOf<WalletEntity?>(null) }
    var createMainTransaction by remember { mutableStateOf(true) }
    var isInterest by remember { mutableStateOf(false) }
    var entryDate by remember { mutableStateOf(Date()) }
    var showWalletDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    calendar.time = entryDate
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            entryDate = calendar.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Record") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.Cancel, "Cancel") } }
            )
        },
        bottomBar = {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onNavigateBack, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(
                    onClick = {
                        val amountLong = (amount.toDoubleOrNull()?.times(100))?.toLong() ?: 0
                        if (loan != null && selectedWallet != null && amountLong > 0) {
                            viewModel.addLoanEntry(loan!!, selectedWallet!!, amountLong, entryDate, note.ifBlank { null }, isInterest, createMainTransaction)
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = amount.isNotBlank() && selectedWallet != null && loan != null
                ) { Text("Add") }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Enter Record Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(entryDate),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = { IconButton(onClick = { datePickerDialog.show() }) { Icon(Icons.Default.DateRange, "Select Date") } },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded = showWalletDropdown, onExpandedChange = { showWalletDropdown = !showWalletDropdown }) {
                OutlinedTextField(
                    value = selectedWallet?.displayName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Associated Account") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Wallet") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = showWalletDropdown, onDismissRequest = { showWalletDropdown = false }) {
                    wallets.forEach { wallet ->
                        DropdownMenuItem(
                            text = { Text(wallet.displayName) },
                            onClick = {
                                selectedWallet = wallet
                                showWalletDropdown = false
                            }
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = createMainTransaction, onCheckedChange = { createMainTransaction = it })
                Text("Create a Main Transaction", modifier = Modifier.clickable { createMainTransaction = !createMainTransaction })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isInterest, onCheckedChange = { isInterest = it })
                Text("Mark as Interest", modifier = Modifier.clickable { isInterest = !isInterest })
            }
        }
    }
}
