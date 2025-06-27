package com.aestroon.zenwallet

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aestroon.authentication.VerifyEmailScreen
import com.aestroon.authentication.domain.AuthViewModel
import com.aestroon.authentication.ui.LoginScreen
import com.aestroon.authentication.ui.SignUpScreen
import com.aestroon.common.navigation.ScreenNavItems

@Composable
fun UnauthenticatedNavGraph(viewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = ScreenNavItems.Login.route) {
        composable(ScreenNavItems.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToSignUp = { navController.navigate(ScreenNavItems.SignUp.route) },
                onRequiresVerification = { email ->
                    navController.navigate(ScreenNavItems.VerifyEmail.createRoute(email))
                }
            )
        }
        composable(ScreenNavItems.SignUp.route) {
            SignUpScreen(
                viewModel = viewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRequiresVerification = { email ->
                    navController.navigate(ScreenNavItems.VerifyEmail.createRoute(email))
                },
            )
        }
        composable(ScreenNavItems.VerifyEmail.route) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyEmailScreen(
                viewModel = viewModel,
                email = email,
                onNavigateToLogin = {
                    navController.popBackStack(ScreenNavItems.Login.route, inclusive = false)
                }
            )
        }
    }
}
