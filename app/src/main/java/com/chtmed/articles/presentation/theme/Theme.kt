package com.chtmed.articles.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = md_dark_primary,
    onPrimary = md_dark_onPrimary,
    primaryContainer = md_dark_primaryContainer,
    onPrimaryContainer = md_dark_onPrimaryContainer,
    secondary = md_dark_secondary,
    tertiary = md_dark_tertiary,
    tertiaryContainer = md_dark_tertiaryContainer,
    onTertiaryContainer = md_dark_onTertiaryContainer,
    background = md_dark_background,
    surface = md_dark_surface,
    surfaceVariant = md_dark_surfaceVariant,
    onBackground = md_dark_onSurface,
    onSurface = md_dark_onSurface,
    onSurfaceVariant = md_dark_onSurfaceVariant,
    outline = md_dark_outline,
    error = md_dark_error
)

private val LightColorScheme = lightColorScheme(
    primary = md_light_primary,
    onPrimary = md_light_onPrimary,
    primaryContainer = md_light_primaryContainer,
    onPrimaryContainer = md_light_onPrimaryContainer,
    secondary = md_light_secondary,
    tertiary = md_light_tertiary,
    tertiaryContainer = md_light_tertiaryContainer,
    onTertiaryContainer = md_light_onTertiaryContainer,
    background = md_light_background,
    surface = md_light_surface,
    surfaceVariant = md_light_surfaceVariant,
    onBackground = md_light_onSurface,
    onSurface = md_light_onSurface,
    onSurfaceVariant = md_light_onSurfaceVariant,
    outline = md_light_outline,
    error = md_light_error
)

/**
 * Dynamic color is intentionally not used: the app has a custom editorial
 * palette + a bundled display font, and wallpaper-derived Material You
 * colors would override that designed look on Android 12+.
 */
@Composable
fun ArticlesAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
