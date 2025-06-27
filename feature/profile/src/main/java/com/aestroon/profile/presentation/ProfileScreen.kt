package com.aestroon.profile.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.aestroon.profile.domain.ProfileViewModel
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutClicked: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var displayName by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // This effect will update the local 'displayName' state whenever the user object
    // from the ViewModel changes. This is important for initializing the form.
    LaunchedEffect(user) {
        user?.userMetadata?.get("display_name")?.jsonPrimitive?.content?.let {
            displayName = it
        }
    }

    // This effect will show a snackbar message upon successful or failed updates.
    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is ProfileViewModel.UiState.Success) {
            snackbarHostState.showSnackbar(currentState.message)
            viewModel.resetUiState() // Reset state after showing message
        } else if (currentState is ProfileViewModel.UiState.Error) {
            snackbarHostState.showSnackbar(currentState.message, withDismissAction = true)
            viewModel.resetUiState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Display Name field
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email field (read-only)
            OutlinedTextField(
                value = user?.email ?: "",
                onValueChange = {},
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                colors = TextFieldDefaults.colors(
                    disabledTextColor = Color.Gray,
                    disabledContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.DarkGray,
                    disabledLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            if (uiState is ProfileViewModel.UiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
            } else {
                // Save button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.updateProfile(displayName)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Save Changes")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout button
            OutlinedButton(
                onClick = onLogoutClicked,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
