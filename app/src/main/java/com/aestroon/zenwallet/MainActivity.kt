package com.aestroon.zenwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import appModule
import com.aestroon.authentication.domain.AuthViewModel
import com.aestroon.authentication.ui.HomeScreen
import com.aestroon.authentication.ui.LoginScreen
import com.aestroon.components.theme.ZenWalletTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.getKoin
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidContext(this@MainActivity)
            modules(appModule)
        }

        enableEdgeToEdge()
        setContent {
            ZenWalletTheme {
                val viewModel: AuthViewModel = getKoin().get()
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()
                val restoreComplete by viewModel.restoreComplete.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.restoreSession()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when {
                            !restoreComplete -> SplashScreen()
                            isLoggedIn -> HomeScreen(viewModel)
                            else -> LoginScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
