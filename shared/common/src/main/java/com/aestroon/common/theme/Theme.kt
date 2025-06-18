package com.aestroon.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = AppBlack,
    background = AppWhite,
    onBackground = DarkGreyChipColor,
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = AppWhite,
    background = AppBlack,
    surface = DarkGreyChipColor,
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
