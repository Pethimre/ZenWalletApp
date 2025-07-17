package com.aestroon.common.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aestroon.common.domain.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay

@Composable
fun VerifyEmailScreen(
    viewModel: AuthViewModel,
    email: String,
    onNavigateToLogin: () -> Unit
) {
    val uiState: AuthViewModel.UiState by viewModel.verificationUiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthViewModel.UiState.Success) {
            // Give the user a moment to read the success message
            delay(2000)
            onNavigateToLogin()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetVerificationState()
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
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Verify Your Email",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "We've sent a verification link to\n$email.\nPlease check your inbox and click the link to continue.",
                textAlign = TextAlign.Center
            )

            Button(
                onClick = { viewModel.checkVerificationStatus() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("I've Verified, Continue")
            }

            OutlinedButton(
                onClick = { viewModel.resendVerificationEmail(email) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Resend Email")
            }

            when (val currentState = uiState) {
                is AuthViewModel.UiState.Loading -> CircularProgressIndicator()
                is AuthViewModel.UiState.Error -> Text(currentState.message, color = MaterialTheme.colorScheme.error)
                is AuthViewModel.UiState.Success -> Text(currentState.message, color = MaterialTheme.colorScheme.primary)
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text("Wrong email? ")
                Text(
                    text = "Back to Login",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onNavigateToLogin)
                )
            }
        }
    }
}
