package com.aestroon.components.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    background = AppWhite,
    onBackground = TertiaryColor,
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    secondary = Color(0xFF4FC3F7),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

/**
 * If dark mode will be implemented the android:configChanges="uiMode" has to be removed.
 * That makes sure the Activity should handle the Theme switch, in its onConfigurationChanged(),
 * but of course we don't want to do it now. Without this the activity gets recreated, and an empty
 * activity will be shown because of the tricky splashscreen :(
 */
@Composable
fun ZenWalletTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (useDarkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colors,
        shapes = Shapes,
        content = content
    )
}
