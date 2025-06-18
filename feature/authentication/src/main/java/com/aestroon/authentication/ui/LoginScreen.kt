package com.aestroon.authentication.ui

import android.hardware.lights.Light
import android.util.Log
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aestroon.authentication.domain.AuthViewModel
import com.aestroon.common.components.PrimaryButton
import com.aestroon.common.components.LinkText
import com.aestroon.common.components.PasswordInput
import com.aestroon.common.theme.AppDimensions.small
import com.aestroon.common.theme.LightBlueChipColor
import com.aestroon.common.theme.PrimaryColor
import com.aestroon.common.theme.ZenWalletTheme
import org.koin.compose.getKoin

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.onBackground, MaterialTheme.colorScheme.background)
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
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(PrimaryColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(small.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(LightBlueChipColor, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Log in", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordInput(
                password = password,
                placeholderLabel = "Password",
                onValueChange = { password = it },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                onClick = { viewModel.login(email, password) },
                text = "login"
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is AuthViewModel.LoginUiState.Loading -> CircularProgressIndicator()
                is AuthViewModel.LoginUiState.Error -> Text(
                    (uiState as AuthViewModel.LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )

                is AuthViewModel.LoginUiState.Success -> Text("Welcome!")
                else -> {}
            }

            Spacer(modifier = Modifier.height(32.dp))

            LinkText(
                labelText = "Don’t have an account?",
                interactiveText = "Sign Up",
                onClick = {},
            )
        }
    }
}
