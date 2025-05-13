package com.aestroon.zenwallet

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aestroon.authentication.domain.AuthViewModel
import com.aestroon.authentication.ui.LoginScreen
import com.aestroon.common.navigation.ScreenNavItems

@Composable
fun UnauthenticatedNavGraph(viewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = ScreenNavItems.Login.route) {
        composable(ScreenNavItems.Login.route) {
            LoginScreen(viewModel)
        }
    }
}
