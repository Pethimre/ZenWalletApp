package com.aestroon.zenwallet

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import appModule
import com.aestroon.common.domain.AuthViewModel
import com.aestroon.common.presentation.screen.BiometricPromptManager
import com.aestroon.common.theme.ZenWalletTheme
import com.aestroon.common.data.di.newsModule
import com.aestroon.common.data.repository.UserPreferencesRepository
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.startKoin

class MainActivity : FragmentActivity() {

    private val authViewModel: AuthViewModel by viewModel()
    private val userPrefsRepo: UserPreferencesRepository by inject()
    private lateinit var biometricPromptManager: BiometricPromptManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidContext(this@MainActivity)
            modules(appModule, newsModule)
        }

        biometricPromptManager = BiometricPromptManager(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ZenWalletTheme {
                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
                val restoreComplete by authViewModel.restoreComplete.collectAsState()
                val isBiometricLockEnabled by userPrefsRepo.isBiometricLockEnabled.collectAsState()

                var biometricUnlockAttempted by rememberSaveable { mutableStateOf(false) }
                var biometricUnlockSuccess by rememberSaveable { mutableStateOf(false) }

                val authenticatedNavController = rememberNavController()

                Log.d("AppStartup", "Recomposition -> isLoggedIn: $isLoggedIn, restoreComplete: $restoreComplete, needsBiometric: ${isBiometricLockEnabled && isLoggedIn}")

                LaunchedEffect(Unit) {
                    authViewModel.restoreSession()
                }

                val needsBiometricUnlock = restoreComplete && isLoggedIn && isBiometricLockEnabled

                LaunchedEffect(needsBiometricUnlock) {
                    if (needsBiometricUnlock && !biometricUnlockAttempted) {
                        biometricUnlockAttempted = true
                        biometricPromptManager.showBiometricPrompt(
                            onSuccess = { biometricUnlockSuccess = true },
                            onFailure = { authViewModel.logout() }
                        )
                    }
                }

                when {
                    !restoreComplete -> {
                        SplashScreen()
                    }
                    needsBiometricUnlock && !biometricUnlockSuccess -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    isLoggedIn -> {
                        AuthenticatedNavGraph(
                            navController = authenticatedNavController,
                            onLogoutClicked = { authViewModel.logout() }
                        )
                    }
                    else -> {
                        UnauthenticatedNavGraph(authViewModel)
                    }
                }
            }
        }
    }
}
