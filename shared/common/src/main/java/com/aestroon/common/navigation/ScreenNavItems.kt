package com.aestroon.common.navigation

sealed class ScreenNavItems(val route: String) {
    object Splash : ScreenNavItems("splash")
    object Login : ScreenNavItems("login")
    object Home : ScreenNavItems("home")
    object Profile : ScreenNavItems("profile")
    object Portfolio : ScreenNavItems("portfolio")
    object Calendar : ScreenNavItems("calendar")
    object Settings : ScreenNavItems("settings")
    object Shared : ScreenNavItems("shared")
    object Wallets : ScreenNavItems("wallets")
}
