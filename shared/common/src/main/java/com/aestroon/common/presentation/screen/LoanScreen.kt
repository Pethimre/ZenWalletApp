package com.aestroon.common.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aestroon.common.data.entity.LoanEntity
import com.aestroon.common.data.entity.LoanType
import com.aestroon.common.domain.LoansViewModel
import com.aestroon.common.utilities.TextFormatter
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    onNavigateToLoanDetails: (String) -> Unit,
    onNavigateToAddLoan: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val viewModel: LoansViewModel = getViewModel()
    val loans by viewModel.loans.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loans") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddLoan) {
                Icon(Icons.Default.Add, contentDescription = "Add Loan")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val totalOwed = loans.filter { it.type == LoanType.LENT }.sumOf { it.remaining }
                Text(
                    "You're owed ${TextFormatter.formatBalance(totalOwed, "HUF")}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(loans, key = { it.id }) { loan ->
                LoanCard(loan = loan, onClick = { onNavigateToLoanDetails(loan.id) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanCard(loan: LoanEntity, onClick: () -> Unit) {
    val progress = if (loan.principal > 0) (loan.principal - loan.remaining).toFloat() / loan.principal else 0f
    val progressPercentage = (progress * 100).toInt()

    val cardColor = try {
        Color(android.graphics.Color.parseColor(loan.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary // Fallback color
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.9f)),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                // Icon can be added here later based on iconName
                Text(loan.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                Text(loan.type.name, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                TextFormatter.formatBalance(loan.remaining, "HUF"),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${TextFormatter.formatBalance(loan.principal - loan.remaining, "HUF")} / ${TextFormatter.formatBalance(loan.principal, "HUF")}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    "$progressPercentage%",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small),
                color = Color.White.copy(alpha = 0.9f),
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}
