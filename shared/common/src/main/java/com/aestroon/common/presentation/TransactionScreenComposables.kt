package com.aestroon.common.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aestroon.common.data.entity.CategoryEntity
import com.aestroon.common.data.entity.TransactionEntity
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.entity.WalletEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionSheet(
    existingTransaction: TransactionEntity?,
    wallets: List<WalletEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        amount: Long,
        name: String,
        description: String?,
        date: Date,
        fromWallet: WalletEntity,
        category: CategoryEntity?,
        type: TransactionType,
        toWallet: WalletEntity?
    ) -> Unit
) {
    val isEditMode = existingTransaction != null
    val title = if (isEditMode) "Edit Transaction" else "New Transaction"

    var selectedType by remember { mutableStateOf(existingTransaction?.transactionType ?: TransactionType.EXPENSE) }
    var amountStr by remember { mutableStateOf(if (isEditMode) (existingTransaction!!.amount / 100.0).toString() else "") }
    var name by remember { mutableStateOf(existingTransaction?.name ?: "") }
    var description by remember { mutableStateOf(existingTransaction?.description ?: "") }
    var fromWallet by remember { mutableStateOf(wallets.find { it.id == existingTransaction?.walletId } ?: wallets.firstOrNull()) }
    var toWallet by remember { mutableStateOf(wallets.find { it.id == existingTransaction?.toWalletId }) }
    var category by remember { mutableStateOf(categories.find { it.id == existingTransaction?.categoryId }) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false } }
    var dateInput by remember { mutableStateOf(dateFormat.format(existingTransaction?.date ?: Date())) }
    var isDateInvalid by remember { mutableStateOf(false) }

    val parsedDate = remember(dateInput) {
        try {
            isDateInvalid = false
            dateFormat.parse(dateInput)
        } catch (e: Exception) {
            isDateInvalid = true
            null
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text(title, style = MaterialTheme.typography.headlineSmall) }
            item { SegmentedButtonRow(selectedType = selectedType, onTypeSelected = { selectedType = it }) }
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
            item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (Optional)") }, modifier = Modifier.fillMaxWidth()) }
            item {
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { Text("Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    supportingText = { if (isDateInvalid) Text("Please use YYYY-MM-DD format") },
                    isError = isDateInvalid,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                DropdownSelector(
                    label = "From Wallet",
                    items = wallets,
                    selectedItem = fromWallet,
                    onItemSelected = { fromWallet = it },
                    itemToString = { it.displayName }
                )
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
                    DropdownSelector(
                        label = "Category",
                        items = categories,
                        selectedItem = category,
                        onItemSelected = { category = it },
                        itemToString = { it.name }
                    )
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        enabled = !isDateInvalid,
                        onClick = {
                            val amountLong = (amountStr.replace(",", ".").toDoubleOrNull() ?: 0.0).times(100).toLong()
                            if (amountLong > 0 && name.isNotBlank() && fromWallet != null && parsedDate != null) {
                                onConfirm(amountLong, name, description.ifBlank { null }, parsedDate, fromWallet!!, category, selectedType, toWallet)
                            }
                        }
                    ) { Text("Save") }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SegmentedButtonRow(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        TransactionType.values().forEach { type ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = type.ordinal,
                    count = TransactionType.values().size
                ),
                onClick = { onTypeSelected(type) },
                selected = type == selectedType
            ) {
                Text(type.name.lowercase().replaceFirstChar { it.titlecase() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownSelector(
    label: String, items: List<T>, selectedItem: T?,
    onItemSelected: (T) -> Unit, itemToString: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedItem?.let(itemToString) ?: "", onValueChange = {}, readOnly = true,
            label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text(itemToString(item)) }, onClick = {
                    onItemSelected(item)
                    expanded = false
                })
            }
        }
    }
}
