package com.helpmom.mother.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = HelpMomPrimary,
    onPrimary = HelpMomOnPrimary,
    primaryContainer = HelpMomPrimaryContainer,
    onPrimaryContainer = HelpMomOnPrimaryContainer,
    secondary = HelpMomSecondary,
    onSecondary = HelpMomOnSecondary,
    secondaryContainer = HelpMomSecondaryContainer,
    onSecondaryContainer = HelpMomOnSecondaryContainer,
    surface = HelpMomSurface,
    onSurface = HelpMomText,
    surfaceVariant = HelpMomSurfaceVariant,
    onSurfaceVariant = HelpMomTextSecondary,
    background = HelpMomBackground,
    onBackground = HelpMomText,
    error = HealthRed,
    errorContainer = HealthRedContainer,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = HelpMomPrimary,
    onPrimary = HelpMomOnPrimary,
    primaryContainer = HelpMomPrimaryContainer,
    onPrimaryContainer = HelpMomOnPrimaryContainer,
    secondary = HelpMomSecondary,
    onSecondary = HelpMomOnSecondary,
    secondaryContainer = HelpMomSecondaryContainer,
    onSecondaryContainer = HelpMomOnSecondaryContainer,
    surface = HelpMomSurface,
    onSurface = HelpMomText,
    surfaceVariant = HelpMomSurfaceVariant,
    onSurfaceVariant = HelpMomTextSecondary,
    background = HelpMomBackground,
    onBackground = HelpMomText,
    error = HealthRed,
    errorContainer = HealthRedContainer,
    onError = Color.White
)

@Composable
fun HelpMomTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = HelpMomTypography,
        content = content
    )
}
