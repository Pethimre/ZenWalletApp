package com.aestroon.common.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aestroon.common.data.entity.GoalEntity
import com.aestroon.common.domain.GoalsViewModel
import com.aestroon.common.presentation.IconProvider
import com.aestroon.common.utilities.TextFormatter
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val defaultGoalColors = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFF44336),
    Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF009688)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoalsViewModel = koinViewModel()
) {
    val goals by viewModel.goals.collectAsState()
    var showAddEditDialog by remember { mutableStateOf<GoalEntity?>(null) }
    var showAddFundsDialog by remember { mutableStateOf<GoalEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<GoalEntity?>(null) }
    var goalReached by remember { mutableStateOf<GoalEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.goalReachedEvent.collect {
            goalReached = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saving Goals") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showAddEditDialog = GoalEntity(
                    id = "", userId = "", name = "", targetAmount = 0L, currentAmount = 0L,
                    targetDate = null, iconName = IconProvider.goalIcons.first().name, color = "#4CAF50"
                )
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { padding ->
        if (goals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No saving goals yet. Tap '+' to add one!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onAddFunds = { showAddFundsDialog = goal },
                        onEdit = { showAddEditDialog = goal },
                        onDelete = { showDeleteDialog = goal }
                    )
                }
            }
        }
    }

    showAddEditDialog?.let { goal ->
        AddEditGoalDialog(
            goal = goal,
            onDismiss = { showAddEditDialog = null },
            onConfirm = {
                viewModel.addOrUpdateGoal(it)
                showAddEditDialog = null
            }
        )
    }

    showAddFundsDialog?.let { goal ->
        AddFundsDialog(
            goal = goal,
            onDismiss = { showAddFundsDialog = null },
            onConfirm = { amount ->
                viewModel.addFundsToGoal(goal, amount)
                showAddFundsDialog = null
            }
        )
    }

    showDeleteDialog?.let { goal ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Goal?") },
            text = { Text("Are you sure you want to delete the '${goal.name}' goal?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteGoal(goal); showDeleteDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }

    goalReached?.let { goal ->
        AlertDialog(
            onDismissRequest = { goalReached = null },
            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Congratulations!") },
            text = { Text("You've reached your saving goal for '${goal.name}'!") },
            confirmButton = {
                Button(onClick = { goalReached = null }) { Text("Awesome!") }
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: GoalEntity,
    onAddFunds: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) {
        (goal.currentAmount.toDouble() / goal.targetAmount.toDouble()).toFloat().coerceIn(0f, 1f)
    } else 0f
    val isCompleted = goal.currentAmount >= goal.targetAmount

    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = goal.icon,
                        contentDescription = goal.name,
                        tint = goal.composeColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(goal.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                if (isCompleted) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = MaterialTheme.colorScheme.primary)
                } else {
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "Options") }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Edit") }, onClick = { onEdit(); showMenu = false })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { onDelete(); showMenu = false })
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${TextFormatter.formatBalance(goal.currentAmount, "")} / ${TextFormatter.formatBalance(goal.targetAmount, "")}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = goal.composeColor
            )
            if (!isCompleted) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAddFunds, modifier = Modifier.align(Alignment.End)) {
                    Text("Add Funds")
                }
            }
        }
    }
}

@Composable
fun AddFundsDialog(goal: GoalEntity, onDismiss: () -> Unit, onConfirm: (amount: Double) -> Unit) {
    var amountStr by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Add Funds to ${goal.name}", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount to Add") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { onConfirm(amountStr.toDoubleOrNull() ?: 0.0) }) { Text("Confirm") }
                }
            }
        }
    }
}

@Composable
fun AddEditGoalDialog(goal: GoalEntity, onDismiss: () -> Unit, onConfirm: (goal: GoalEntity) -> Unit) {
    var name by remember { mutableStateOf(goal.name) }
    var targetAmountStr by remember { mutableStateOf(if (goal.targetAmount > 0) (goal.targetAmount / 100.0).toString() else "") }
    var currentAmountStr by remember { mutableStateOf((goal.currentAmount / 100.0).toString()) }
    var targetDateStr by remember { mutableStateOf(goal.targetDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) } ?: "") }

    // --- KEY CHANGE: State for the selected icon and color ---
    var selectedIconName by remember { mutableStateOf(goal.iconName ?: IconProvider.goalIcons.first().name) }
    var selectedColor by remember { mutableStateOf(goal.composeColor) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(if (goal.id.isBlank()) "New Goal" else "Edit Goal", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Goal Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = targetAmountStr, onValueChange = { targetAmountStr = it }, label = { Text("Target Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = currentAmountStr, onValueChange = { currentAmountStr = it }, label = { Text("Current Amount Saved") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = targetDateStr, onValueChange = { targetDateStr = it }, label = { Text("Target Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(16.dp))
                Text("Icon")
                LazyRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(IconProvider.goalIcons) { appIcon ->
                        IconButton(
                            onClick = { selectedIconName = appIcon.name },
                            modifier = Modifier.size(48.dp).clip(CircleShape)
                                .background(if (selectedIconName == appIcon.name) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(imageVector = appIcon.icon, contentDescription = appIcon.name,
                                tint = if (selectedIconName == appIcon.name) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Color")
                LazyRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(defaultGoalColors) { color ->
                        Box(modifier = Modifier.size(36.dp).background(color, CircleShape)
                            .border(width = if (color == selectedColor) 2.dp else 1.dp, color = MaterialTheme.colorScheme.onSurface, shape = CircleShape)
                            .clickable { selectedColor = color })
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = {
                        // --- KEY CHANGE: The updatedGoal now includes the icon and color ---
                        val updatedGoal = goal.copy(
                            name = name,
                            targetAmount = ((targetAmountStr.toDoubleOrNull() ?: 0.0) * 100).toLong(),
                            currentAmount = ((currentAmountStr.toDoubleOrNull() ?: 0.0) * 100).toLong(),
                            targetDate = try { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(targetDateStr)?.time } catch (e: Exception) { null },
                            iconName = selectedIconName,
                            color = String.format("#%08X", selectedColor.toArgb())
                        )
                        onConfirm(updatedGoal)
                    }) { Text("Save") }
                }
            }
        }
    }
}
