package com.example.todo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*;
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
    surface = Color(0xFFFDF8FF),
    background = Color(0xFFFDF8FF),
    error = Color(0xFFB3261E),
    // Custom gray-blue background and primary
    // We'll override the background to be a cold light gray/blue
    background = Color(0xFFE6EAF0), // Light gray-blue
    primary = Color(0xFF5A6D7E),   // Steel/blue
    secondary = Color(0xFF8A9BA8), // Gray
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8),
    surface = Color(0xFF1B1B1F),
    background = Color(0xFF1B1B1F),
    error = Color(0xFFCF6679),
    // For dark theme, we can keep the Material dark scheme or adjust similarly
    background = Color(0xFF1B1B1F), // Dark background
    primary = Color(0xFFD0BCFF),   // Light purple for primary in dark
    secondary = Color(0xFFCCC2DC), // Light gray
)

@Composable
fun TodoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}