package com.aestroon.authentication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aestroon.authentication.domain.AuthViewModel
import com.aestroon.common.components.LinkText
import com.aestroon.common.components.PasswordInput
import com.aestroon.common.components.PrimaryButton
import com.aestroon.common.theme.AppDimensions.small
import com.aestroon.common.theme.LightBlueChipColor
import com.aestroon.common.theme.PrimaryColor
import com.aestroon.common.theme.RedChipColor
import com.aestroon.common.utilities.network.ConnectivityObserver

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRequiresVerification: (String) -> Unit,
) {
    val uiState by viewModel.signUpUiState.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()

    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is AuthViewModel.NavigationEvent.ToVerifyEmail -> {
                    onRequiresVerification(event.email)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setAuthFlowActive(true)
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.setAuthFlowActive(false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.onBackground, MaterialTheme.colorScheme.background),
                    endY = 0.3f,
                )
            )
            .padding(horizontal = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(PrimaryColor, CircleShape))
                Spacer(modifier = Modifier.width(small.dp))
                Box(modifier = Modifier.size(24.dp).background(LightBlueChipColor, CircleShape))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Create Account", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (networkStatus != ConnectivityObserver.Status.Available) {
                Text("You are offline. Your account will be synced later.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("Display Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            PasswordInput(password = password, placeholderLabel = "Password", onValueChange = { password = it; passwordError = null })
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                if (passwordError != null) {
                    Icon(Icons.Default.Error, tint = RedChipColor, contentDescription = "Password error")
                }
                PasswordInput(password = confirmPassword, placeholderLabel = "Confirm Password", onValueChange = { confirmPassword = it; passwordError = null })
            }


            passwordError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState !is AuthViewModel.UiState.Loading) {
                PrimaryButton(
                    onClick = {
                        if (password != confirmPassword) {
                            passwordError = "Passwords do not match."
                        } else if (password.length < 6) {
                            passwordError = "Password must be at least 6 characters."
                        }
                        else {
                            viewModel.signUp(displayName, email, password)
                        }
                    },
                    text = "Sign Up",
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is AuthViewModel.UiState.Loading -> CircularProgressIndicator()
                is AuthViewModel.UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is AuthViewModel.UiState.Success -> Text(state.message, color = LightBlueChipColor)
                else -> {}
            }

            Spacer(modifier = Modifier.height(32.dp))

            LinkText(
                labelText = "Already have an account?",
                interactiveText = "Log In",
                onClick = onNavigateToLogin,
            )
        }
    }
}