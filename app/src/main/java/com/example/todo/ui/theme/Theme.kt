package com.example.todo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Grey-blue palette (defined in Color.kt, exported publicly ─────────────────
private val LightColors = lightColorScheme(
    primary             = SteelBlue,
    onPrimary           = Color(0xFFFFFFFF),
    primaryContainer    = SteelDim,
    onPrimaryContainer  = Color(0xFFD9E4EF),
    secondary           = SteelGray,
    onSecondary         = SteelDark,
    tertiary            = Color(0xFF6B8294),
    background          = Color(0xFFE8EDF2),
    onBackground        = SteelDark,
    surface             = Color(0xFFF4F7FA),
    onSurface           = SteelDark,
    surfaceVariant      = Color(0xFFF4F7FA),
    onSurfaceVariant    = Color(0xFF4A5B6A),
    error               = RedBg,
    onError             = Color(0xFFFFFFFF),
    outline             = Color(0xFF7A8E9E),
)

private val DarkColors = darkColorScheme(
    primary             = Color(0xFF7AA0C4),
    onPrimary           = Color(0xFF0E1419),
    primaryContainer    = SteelDim,
    onPrimaryContainer  = Color(0xFFD9E4EF),
    secondary           = SteelGray,
    onSecondary         = SteelDark,
    tertiary            = Color(0xFF6B8294),
    background          = Color(0xFF12161C),
    onBackground        = Color(0xFFD9E4EF),
    surface             = Color(0xFF1C222B),
    onSurface           = Color(0xFFD9E4EF),
    error               = RedBg,
    onError             = SteelDark,
    outline             = Color(0xFF7A8E9E),
)

@Composable
fun TodoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography   = Typography(),
        content      = content
    )
}
