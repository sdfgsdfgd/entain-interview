package com.entain.nextraces.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF5465FF),
    onPrimary = Color(0xFFEFF4FF),
    surface = Color(0xFFF7F7FF),
    onSurface = Color(0xFF10172A),
    onSurfaceVariant = Color(0xFF4E5D80),
    background = Color(0xFFEFF2FF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DB2FF),
    onPrimary = Color(0xFF050914),
    surface = Color(0xFF1C2539),
    onSurface = Color(0xFFE5EBFF),
    onSurfaceVariant = Color(0xFFADB9DC),
    background = Color(0xFF0B1120)
)

@Composable
fun NextRacesTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
