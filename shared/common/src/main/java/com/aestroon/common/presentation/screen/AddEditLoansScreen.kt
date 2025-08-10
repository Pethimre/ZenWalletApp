package com.aestroon.common.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aestroon.common.data.entity.LoanEntity
import com.aestroon.common.data.entity.LoanType
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.domain.LoansViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLoanScreen(
    loanId: String?,
    onNavigateBack: () -> Unit
) {
    val viewModel: LoansViewModel = getViewModel()
    val authRepository: AuthRepository = koinInject()
    val isEditing = loanId != null

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var principal by remember { mutableStateOf("") }
    var loanType by remember { mutableStateOf(LoanType.LENT) }
    var selectedColor by remember { mutableStateOf(Color(0xFF6200EE)) }

    LaunchedEffect(loanId) {
        if (isEditing) {
            val existingLoan = viewModel.getLoanById(loanId!!).first()
            if (existingLoan != null) {
                name = existingLoan.name
                description = existingLoan.description ?: ""
                principal = (existingLoan.principal / 100).toString()
                loanType = existingLoan.type
                selectedColor = try {
                    Color(android.graphics.Color.parseColor(existingLoan.color))
                } catch (e: Exception) {
                    Color(0xFF6200EE)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Loan" else "Add Loan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val principalLong = principal.toLongOrNull()?.times(100) ?: 0
                    if (name.isNotBlank() && principalLong > 0) {
                        val colorString = String.format("#%06X", 0xFFFFFF and selectedColor.toArgb())

                        viewModel.addOrUpdateLoan(
                            LoanEntity(
                                id = loanId ?: UUID.randomUUID().toString(),
                                name = name,
                                description = description.ifBlank { null },
                                principal = principalLong,
                                remaining = if (isEditing) (viewModel.loans.value.find { it.id == loanId }?.remaining ?: principalLong) else principalLong,
                                color = colorString,
                                iconName = "Default",
                                type = loanType,
                                userId = runBlocking { authRepository.userIdFlow.first() ?: "" }
                            )
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Save Loan")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Loan Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (Optional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = principal, onValueChange = { principal = it }, label = { Text("Principal Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            Text("Loan Type", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = loanType == LoanType.LENT, onClick = { loanType = LoanType.LENT })
                Text("I Lent Money", modifier = Modifier.clickable { loanType = LoanType.LENT })
                Spacer(Modifier.width(16.dp))
                RadioButton(selected = loanType == LoanType.BORROWED, onClick = { loanType = LoanType.BORROWED })
                Text("I Borrowed Money", modifier = Modifier.clickable { loanType = LoanType.BORROWED })
            }

            Text("Color", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta)
                colors.forEach { color ->
                    Box(modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { selectedColor = color }
                        .then(if (selectedColor == color) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                    )
                }
            }
        }
    }
}
