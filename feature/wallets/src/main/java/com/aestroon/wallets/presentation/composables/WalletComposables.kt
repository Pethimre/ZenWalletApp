import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.data.model.WalletsSummary
import com.aestroon.common.data.serializable.Currency
import com.aestroon.common.presentation.IconProvider
import java.text.NumberFormat
import java.util.Locale

fun Color.toHexString(): String {
    return String.format("#%08X", this.toArgb())
}

fun String.toColor(): Color {
    return try {
        if (this.startsWith("#")) Color(android.graphics.Color.parseColor(this)) else Color(android.graphics.Color.parseColor("#$this"))
    } catch (e: IllegalArgumentException) {
        Color.Gray
    }
}

val defaultWalletColors = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFF44336),
    Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF009688)
)

fun generateRandomColor() = defaultWalletColors.random()

fun formatBalance(balanceInCents: Long, currencyCode: String): String {
    val amount = balanceInCents / 100.0
    return try {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            currency = java.util.Currency.getInstance(currencyCode)
        }.format(amount)
    } catch (e: Exception) {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount)
    }
}

@Composable
fun SpendingWalletCard(
    wallet: WalletEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = wallet.composeColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = wallet.icon,
                    contentDescription = "Wallet Icon",
                    tint = wallet.composeColor,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(wallet.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (wallet.isSynced) "Synced" else "Pending Sync...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = formatBalance(wallet.balance, wallet.currency),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWalletDialog(
    existingWallet: WalletEntity?,
    allCurrencies: List<Currency>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, balance: String, goalAmount: String, color: Color, currency: String, iconName: String, included: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(existingWallet?.displayName ?: "") }
    var balanceStr by remember { mutableStateOf((existingWallet?.balance ?: 0L).div(100.0).toString()) }
    var goalAmountStr by remember { mutableStateOf((existingWallet?.goalAmount ?: 0L).div(100.0).toString()) }
    var selectedColor by remember { mutableStateOf(existingWallet?.composeColor ?: generateRandomColor()) }
    var selectedCurrency by remember { mutableStateOf(existingWallet?.currency ?: "HUF") }
    var selectedIconName by remember { mutableStateOf(existingWallet?.iconName ?: IconProvider.walletIcons.first().name) }
    var isIncluded by remember { mutableStateOf(existingWallet?.included ?: true) }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    if (existingWallet == null) "Add New Wallet" else "Edit Wallet",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Wallet Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = balanceStr, onValueChange = { balanceStr = it }, label = { Text("Current Balance") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = goalAmountStr, onValueChange = { goalAmountStr = it }, label = { Text("Goal Amount (Optional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))

                // Icon Picker
                Text("Icon:")
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(IconProvider.walletIcons, key = { it.name }) { walletIcon ->
                        IconButton(
                            onClick = { selectedIconName = walletIcon.name },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedIconName == walletIcon.name) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        ) {
                            Icon(
                                imageVector = walletIcon.icon,
                                contentDescription = walletIcon.name,
                                tint = if (selectedIconName == walletIcon.name) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Color Picker
                Text("Color:")
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(defaultWalletColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(color, CircleShape)
                                .border(
                                    width = if (color == selectedColor) 2.dp else 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Currency Dropdown
                ExposedDropdownMenuBox(
                    expanded = currencyDropdownExpanded,
                    onExpandedChange = { currencyDropdownExpanded = !currencyDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCurrency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Currency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = currencyDropdownExpanded,
                        onDismissRequest = { currencyDropdownExpanded = false }
                    ) {
                        allCurrencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text("${currency.name} (${currency.code})") },
                                onClick = {
                                    selectedCurrency = currency.code
                                    currencyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Included in Total Switch
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isIncluded = !isIncluded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Include in Total Balance")
                    Switch(checked = isIncluded, onCheckedChange = { isIncluded = it })
                }

                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = { onConfirm(name, balanceStr, goalAmountStr, selectedColor, selectedCurrency, selectedIconName, isIncluded) },
                        enabled = name.isNotBlank() && balanceStr.toDoubleOrNull() != null
                    ) {
                        Text(if (existingWallet == null) "Add" else "Save")
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteWalletDialog(walletName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Wallet") },
        text = { Text("Are you sure you want to delete the wallet \"$walletName\"?") },
        confirmButton = {
            Button(onClick = { onConfirm(); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Delete")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun OfflineWarningBanner(isVisible: Boolean) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "You have changes that will be synced when you're back online.",
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun PieChart(
    data: List<Pair<String, Float>>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 20.dp
) {
    if (data.isEmpty()) return
    val total = data.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(0.0001f)

    Canvas(modifier = modifier.size(180.dp)) {
        var startAngle = -90f
        data.forEachIndexed { index, item ->
            val sweepAngle = (item.second / total) * 360f
            if (sweepAngle > 0.1f) {
                drawArc(
                    color = colors.getOrElse(index) { Color.Gray },
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
                )
            }
            startAngle += sweepAngle
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OverallSummaryCard(summary: WalletsSummary, baseCurrency: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Wallets Overview", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow("Total Balance:", formatBalance(summary.totalBalance, baseCurrency))

            if (summary.balanceBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Balance Breakdown:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    PieChart(
                        data = summary.balanceBreakdown.map { it.first.displayName to it.second },
                        colors = summary.balanceBreakdown.map { it.first.composeColor }
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    summary.balanceBreakdown.forEach { (wallet, _) ->
                        LegendItem(color = wallet.composeColor, label = wallet.displayName)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}