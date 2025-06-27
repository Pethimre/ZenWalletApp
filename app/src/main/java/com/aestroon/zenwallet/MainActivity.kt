package com.aestroon.zenwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import appModule
import com.aestroon.authentication.domain.AuthViewModel
import com.aestroon.common.theme.ZenWalletTheme
import com.aestroon.home.news.di.newsModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.getKoin
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidContext(this@MainActivity)
            modules(
                appModule,
                newsModule,
            )
        }

        // Enable edge-to-edge drawing
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ZenWalletTheme {
                val authViewModel: AuthViewModel = koinViewModel()
                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
                val restoreComplete by authViewModel.restoreComplete.collectAsState()

                LaunchedEffect(Unit) {
                    authViewModel.restoreSession()
                }

                when {
                    !restoreComplete -> {
                        SplashScreen()
                    }
                    isLoggedIn -> {
                        AuthenticatedNavGraph(
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
