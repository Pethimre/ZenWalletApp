package com.aestroon.common.presentation.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aestroon.common.data.entity.CategoryEntity
import com.aestroon.common.data.entity.PlannedPaymentEntity
import com.aestroon.common.data.entity.RecurrenceType
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.domain.PlannedPaymentsViewModel
import com.aestroon.common.navigation.ScreenNavItems
import com.aestroon.common.presentation.DropdownSelector
import com.aestroon.common.presentation.SegmentedButtonRow
import com.aestroon.common.theme.GreenChipColor
import com.aestroon.common.theme.RedChipColor
import com.aestroon.common.utilities.TextFormatter
import org.koin.androidx.compose.getViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannedPaymentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlannedPaymentsViewModel = getViewModel()
) {
    val payments by viewModel.plannedPayments.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val baseCurrency by viewModel.baseCurrency.collectAsState()
    val exchangeRates by viewModel.exchangeRates.collectAsState()

    var showAddEditSheet by remember { mutableStateOf(false) }
    var paymentToEdit by remember { mutableStateOf<PlannedPaymentEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planned Payments") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                paymentToEdit = null
                showAddEditSheet = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Planned Payment")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (payments.isEmpty()) {
                item {
                    Text(
                        "No planned payments yet. Tap the '+' button to add one.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                    )
                }
            } else {
                items(payments, key = { it.id }) { payment ->
                    PlannedPaymentCard(
                        payment = payment,
                        baseCurrency = baseCurrency,
                        exchangeRates = exchangeRates,
                        onPay = { viewModel.pay(payment) },
                        onSkip = { viewModel.skip(payment) },
                        onEdit = {
                            paymentToEdit = payment
                            showAddEditSheet = true
                        },
                        onDelete = { viewModel.deletePayment(payment) }
                    )
                }
            }
        }

        if (showAddEditSheet) {
            AddEditPlannedPaymentSheet(
                existingPayment = paymentToEdit,
                wallets = wallets,
                categories = categories,
                onDismiss = { showAddEditSheet = false },
                onConfirm = { existing, name, description, dueDate, amount, wallet, category, recurrenceType, recurrenceValue, transactionType, toWallet ->
                    viewModel.addOrUpdatePayment(
                        existing, name, description, dueDate, amount, wallet, category, recurrenceType, recurrenceValue, transactionType, toWallet
                    )
                    showAddEditSheet = false
                }
            )
        }
    }
}

@Composable
fun PlannedPaymentCard(
    payment: PlannedPaymentEntity,
    baseCurrency: String,
    exchangeRates: Map<String, Double>?,
    onPay: () -> Unit,
    onSkip: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    val amountColor = when (payment.transactionType) {
        TransactionType.INCOME -> GreenChipColor
        TransactionType.EXPENSE -> RedChipColor
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.primary
    }

    val convertedAmount = remember(payment, baseCurrency, exchangeRates) {
        if (exchangeRates == null || payment.currency == baseCurrency) return@remember null
        val baseRate = exchangeRates[baseCurrency] ?: return@remember null
        val paymentRate = exchangeRates[payment.currency] ?: return@remember null
        (payment.amount / 100.0) * (baseRate / paymentRate)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(payment.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    payment.description?.let {
                        Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${TextFormatter.toBasicFormat(payment.amount / 100.0)} ${payment.currency}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                    convertedAmount?.let {
                        Text(
                            text = "≈ ${TextFormatter.toPrettyAmountWithCurrency(it, baseCurrency)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Due: ${simpleDateFormat.format(payment.dueDate)}", style = MaterialTheme.typography.bodyMedium)
            Text("Repeats: ${payment.recurrenceType.name.lowercase().replaceFirstChar { it.titlecase() }}", style = MaterialTheme.typography.bodyMedium)


            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = onPay, colors = ButtonDefaults.buttonColors(containerColor = GreenChipColor)) {
                            Text("Pay")
                        }
                        TextButton(onClick = onSkip) {
                            Text("Skip")
                        }
                        TextButton(onClick = onEdit) {
                            Text("Edit")
                        }
                        TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = RedChipColor)) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlannedPaymentSheet(
    existingPayment: PlannedPaymentEntity?,
    wallets: List<WalletEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        existingPayment: PlannedPaymentEntity?,
        name: String,
        description: String?,
        dueDate: Date,
        amount: Long,
        fromWallet: WalletEntity,
        category: CategoryEntity?,
        recurrenceType: RecurrenceType,
        recurrenceValue: Int,
        transactionType: TransactionType,
        toWallet: WalletEntity?
    ) -> Unit
) {
    var name by remember { mutableStateOf(existingPayment?.name ?: "") }
    var description by remember { mutableStateOf(existingPayment?.description ?: "") }
    var amountStr by remember { mutableStateOf(existingPayment?.let { (it.amount / 100.0).toString() } ?: "") }
    var fromWallet by remember { mutableStateOf(wallets.find { it.id == existingPayment?.walletId } ?: wallets.firstOrNull()) }
    var toWallet by remember { mutableStateOf(wallets.find { it.id == existingPayment?.toWalletId }) }
    var category by remember { mutableStateOf(categories.find { it.id == existingPayment?.categoryId }) }
    var recurrenceType by remember { mutableStateOf(existingPayment?.recurrenceType ?: RecurrenceType.ONCE) }
    var recurrenceValue by remember { mutableStateOf(existingPayment?.recurrenceValue?.toString() ?: "1") }
    var date by remember { mutableStateOf(existingPayment?.dueDate ?: Date()) }
    var selectedType by remember { mutableStateOf(existingPayment?.transactionType ?: TransactionType.EXPENSE) }

    val simpleDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.time)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            date = Date(millis)
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text(if (existingPayment == null) "New Planned Payment" else "Edit Planned Payment", style = MaterialTheme.typography.headlineSmall) }
            item {
                SegmentedButtonRow(
                    selectedType = selectedType,
                    onTypeSelected = { newType ->
                        selectedType = newType
                        if (newType != TransactionType.TRANSFER) {
                            toWallet = null
                        }
                    }
                )
            }
            item {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (Optional)") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = { Text("Amount") },
                    leadingIcon = { fromWallet?.let { Text(it.currency) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                val label = if (selectedType == TransactionType.INCOME) "To Wallet" else "From Wallet"
                DropdownSelector(label = label, items = wallets, selectedItem = fromWallet, onItemSelected = { fromWallet = it }, itemToString = { it.displayName })
            }

            if (selectedType == TransactionType.TRANSFER) {
                item {
                    val availableToWallets = wallets.filter { it.id != fromWallet?.id }
                    DropdownSelector(
                        label = "To Wallet",
                        items = availableToWallets,
                        selectedItem = toWallet,
                        onItemSelected = { toWallet = it },
                        itemToString = { it.displayName }
                    )
                }
            }

            if (selectedType != TransactionType.TRANSFER) {
                item {
                    DropdownSelector(label = "Category (Optional)", items = categories, selectedItem = category, onItemSelected = { category = it }, itemToString = { it.name })
                }
            }

            item {
                OutlinedTextField(
                    value = simpleDateFormat.format(date),
                    onValueChange = {},
                    label = { Text("Due Date") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                )
            }
            item {
                DropdownSelector(
                    label = "Repeats",
                    items = RecurrenceType.entries,
                    selectedItem = recurrenceType,
                    onItemSelected = { recurrenceType = it },
                    itemToString = { it.name.lowercase().replaceFirstChar { char -> char.titlecase() } }
                )
            }
            if (recurrenceType != RecurrenceType.ONCE) {
                item {
                    OutlinedTextField(
                        value = recurrenceValue,
                        onValueChange = { recurrenceValue = it.filter { c -> c.isDigit() } },
                        label = { Text("Every") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = {
                        val amountLong = (amountStr.replace(",", ".").toDoubleOrNull() ?: 0.0).times(100).toLong()

                        // --- DEBUG LOGGING ---
                        Log.d("SheetDebug", "SAVE CLICKED:")
                        Log.d("SheetDebug", "  - Type: $selectedType")
                        Log.d("SheetDebug", "  - From/To Wallet: ${fromWallet?.displayName} (ID: ${fromWallet?.id})")
                        Log.d("SheetDebug", "  - Destination Wallet (for Transfer): ${toWallet?.displayName} (ID: ${toWallet?.id})")
                        Log.d("SheetDebug", "  - Category: ${category?.name} (ID: ${category?.id})")

                        if (amountLong > 0 && name.isNotBlank() && fromWallet != null) {
                            onConfirm(
                                existingPayment, name, description.ifBlank { null }, date, amountLong, fromWallet!!, category, recurrenceType, recurrenceValue.toIntOrNull() ?: 1, selectedType, toWallet
                            )
                        }
                    }) { Text(if (existingPayment == null) "Save" else "Update") }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
