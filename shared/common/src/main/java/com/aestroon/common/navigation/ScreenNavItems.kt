package com.aestroon.common.navigation

sealed class ScreenNavItems(val route: String) {
    object Splash : ScreenNavItems("splash")
    object VerifyEmail : ScreenNavItems("verifyEmail/{email}") {
        fun createRoute(email: String) = "verifyEmail/$email"
    }
    object Login : ScreenNavItems("login")
    object SignUp : ScreenNavItems("signUp")
    object Home : ScreenNavItems("home")
    object Profile : ScreenNavItems("profile")
    object Portfolio : ScreenNavItems("portfolio")
    object Calendar : ScreenNavItems("calendar")
    object Settings : ScreenNavItems("settings")
    object CurrencySelection : ScreenNavItems("currency_selection")
    object Shared : ScreenNavItems("shared")
    object Wallets : ScreenNavItems("wallets")
}
