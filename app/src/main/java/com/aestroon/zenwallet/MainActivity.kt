package com.aestroon.zenwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import appModule
import com.aestroon.authentication.domain.AuthViewModel
import com.aestroon.common.theme.ZenWalletTheme
import com.aestroon.home.news.di.newsModule
import org.koin.android.ext.koin.androidContext
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

        enableEdgeToEdge()

        setContent {
            ZenWalletTheme {
                val authViewModel: AuthViewModel = getKoin().get()
                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
                val restoreComplete by authViewModel.restoreComplete.collectAsState()

                LaunchedEffect(Unit) {
                    authViewModel.restoreSession()
                }

                if (!restoreComplete) {
                    SplashScreen()
                } else if (!isLoggedIn) {
                    UnauthenticatedNavGraph(authViewModel)
                } else {
                    AuthenticatedNavGraph(onLogoutClicked = { authViewModel.logout() })
                }
            }
        }
    }
}
