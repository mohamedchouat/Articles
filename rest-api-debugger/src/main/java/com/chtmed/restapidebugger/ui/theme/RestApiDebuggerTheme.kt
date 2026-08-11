package com.chtmed.restapidebugger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DebuggerBackground = Color(0xFF10131A)
private val DebuggerSurface = Color(0xFF1A1E27)
private val DebuggerOnSurface = Color(0xFFE3E6EC)
private val DebuggerOnSurfaceVariant = Color(0xFF9CA3B0)
private val DebuggerOutline = Color(0xFF2B303C)
private val DebuggerPrimary = Color(0xFF7C9CFF)

private val DebuggerColorScheme = darkColorScheme(
    primary = DebuggerPrimary,
    background = DebuggerBackground,
    surface = DebuggerSurface,
    surfaceVariant = DebuggerSurface,
    onBackground = DebuggerOnSurface,
    onSurface = DebuggerOnSurface,
    onSurfaceVariant = DebuggerOnSurfaceVariant,
    outline = DebuggerOutline
)

private val DebuggerTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp
    )
)

/**
 * Deliberately self-contained developer-tool theme (dark, monospace-leaning)
 * so this module never depends on the host app's theme/resources.
 */
@Composable
fun RestApiDebuggerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DebuggerColorScheme,
        typography = DebuggerTypography,
        content = content
    )
}
