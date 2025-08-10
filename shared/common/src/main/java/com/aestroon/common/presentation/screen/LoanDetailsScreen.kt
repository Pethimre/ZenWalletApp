package com.aestroon.common.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aestroon.common.data.entity.LoanEntryEntity
import com.aestroon.common.domain.LoansViewModel
import com.aestroon.common.utilities.TextFormatter
import org.koin.androidx.compose.getViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LoanDetailScreen(
    loanId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddEntry: (String) -> Unit,
    onNavigateToEditLoan: (String) -> Unit
) {
    val viewModel: LoansViewModel = getViewModel()
    val loanState by viewModel.getLoanWithEntries(loanId).collectAsState(initial = Pair(null, emptyList()))
    val (loan, entries) = loanState
    val baseCurrency by viewModel.baseCurrency.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Loan?") },
            text = { Text("Are you sure you want to delete this loan and all its entries? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        loan?.let {
                            viewModel.deleteLoan(it)
                            showDeleteConfirmation = false
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    loan?.let {
        val backgroundColor = try {
            Color(android.graphics.Color.parseColor(it.color))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(16.dp)
                    .padding(top = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Row {
                        Button(onClick = { onNavigateToEditLoan(loan.id) }) { Text("Edit") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { showDeleteConfirmation = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Icon(Icons.Default.Delete, "Delete") }
                    }
                }

                Column(modifier = Modifier.padding(top = 56.dp)) {
                    Text(it.name, color = MaterialTheme.colorScheme.onPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(it.type.name, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        TextFormatter.formatSimpleBalance(it.remaining, baseCurrency),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            "Paid: ${TextFormatter.formatSimpleBalance(it.principal - it.remaining, baseCurrency)} / ${TextFormatter.formatSimpleBalance(it.principal, baseCurrency)}"
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = if (it.principal > 0) (it.principal - it.remaining).toFloat() / it.principal else 0f,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { onNavigateToAddEntry(loanId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Add record")
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries) { entry ->
                    LoanEntryItem(entry = entry, baseCurrency = baseCurrency)
                }
            }
        }
    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun LoanEntryItem(entry: LoanEntryEntity, baseCurrency: String) {
    val viewModel: LoansViewModel = getViewModel()
    val wallets by viewModel.wallets.collectAsState()
    val wallet = wallets.find { it.id == entry.walletId }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(wallet?.composeColor ?: MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = wallet?.icon ?: Icons.Default.AccountBalanceWallet,
                    contentDescription = wallet?.displayName,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.note ?: wallet?.displayName ?: "Payment", fontWeight = FontWeight.SemiBold)
                Text(
                    SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault()).format(entry.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                TextFormatter.formatSimpleBalance(entry.amount, baseCurrency),
                fontWeight = FontWeight.Bold,
                color = if (entry.isInterest) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}
