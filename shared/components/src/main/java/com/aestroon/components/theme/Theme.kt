package com.aestroon.components.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    background = AppWhite,
    onBackground = TertiaryColor,
)

/**
 * If dark mode will be implemented the android:configChanges="uiMode" has to be removed.
 * That makes sure the Activity should handle the Theme switch, in its onConfigurationChanged(),
 * but of course we don't want to do it now. Without this the activity gets recreated, and an empty
 * activity will be shown because of the tricky splashscreen :(
 */
@Composable
fun ZenWalletTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorPalette,
        shapes = Shapes,
        content = content
    )
}
