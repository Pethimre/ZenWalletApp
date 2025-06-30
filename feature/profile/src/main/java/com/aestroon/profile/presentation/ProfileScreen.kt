package com.aestroon.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aestroon.profile.domain.ProfileSettingsUiState
import com.aestroon.profile.domain.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutClicked: () -> Unit
) {
    val uiState by viewModel.profileSettingsUiState.collectAsState()
    val user by viewModel.user.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricLockEnabled.collectAsState()

    val displayName by viewModel.displayName.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val worthGoal by viewModel.worthGoal.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is ProfileSettingsUiState.Success) {
            snackbarHostState.showSnackbar(currentState.message)
            viewModel.resetUiState()
        } else if (currentState is ProfileSettingsUiState.Error) {
            snackbarHostState.showSnackbar(currentState.message, withDismissAction = true, duration = SnackbarDuration.Long)
            viewModel.resetUiState()
        }
    }

    if (showPasswordDialog) {
        PasswordChangeDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { newPassword ->
                viewModel.updatePassword(newPassword)
                showPasswordDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState is ProfileSettingsUiState.Loading && worthGoal.isBlank()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(modifier = Modifier.height(64.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    SettingsSectionTitle("Profile Settings")
                    SettingsTextField(
                        value = displayName,
                        onValueChange = { viewModel.displayName.value = it },
                        label = "Display Name"
                    )
                    SettingsTextFieldWithPlaceholder(
                        value = phone,
                        onValueChange = { viewModel.phone.value = it },
                        label = "Phone Number",
                        placeholder = "+36 30 123 4567",
                        keyboardType = KeyboardType.Phone
                    )
                    SettingsTextField(
                        value = user?.email ?: "",
                        onValueChange = {},
                        label = "Email",
                        enabled = false
                    )
                    SettingsTextField(
                        value = worthGoal,
                        onValueChange = { viewModel.worthGoal.value = it },
                        label = "Net Worth Goal",
                        keyboardType = KeyboardType.Number
                    )
                    OutlinedTextField(
                        value = "HUF",
                        onValueChange = {},
                        label = { Text("Currency") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown") },
                        colors = settingsTextFieldColors()
                    )
                    TextButton(onClick = { showPasswordDialog = true }) {
                        Text("Change Password")
                    }

                    Spacer(Modifier.height(24.dp))

                    SettingsSectionTitle("App Settings")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable Biometric Lock", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { viewModel.setBiometricLockEnabled(it) },
                            enabled = uiState !is ProfileSettingsUiState.Loading,
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    if (uiState is ProfileSettingsUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
                    }
                    Button(
                        onClick = { viewModel.updateProfile() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                        enabled = uiState !is ProfileSettingsUiState.Loading
                    ) {
                        Text("Save Changes")
                    }
                    OutlinedButton(
                        onClick = onLogoutClicked,
                        modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Logout")
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = settingsTextFieldColors(enabled)
    )
}

@Composable
private fun SettingsTextFieldWithPlaceholder(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = settingsTextFieldColors(enabled)
    )
}

@Composable
private fun settingsTextFieldColors(enabled: Boolean = true) = if (enabled)
    TextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = Color.Gray,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = Color.Gray,
        focusedPlaceholderColor = Color.DarkGray
    ) else TextFieldDefaults.colors(
    disabledTextColor = Color.Gray,
    disabledContainerColor = Color.Transparent,
    disabledIndicatorColor = Color.DarkGray,
    disabledLabelColor = Color.Gray
)

@Composable
private fun PasswordChangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = isError
                )
                if (isError) {
                    Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                enabled = password.isNotEmpty() && !isError
            ) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
