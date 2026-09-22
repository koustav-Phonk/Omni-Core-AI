package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WorkstationDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF040A10),
    primaryContainer = Color(0xFF132230),
    onPrimaryContainer = Color(0xFFB3E5FC),
    secondary = Color(0xFF00E676),
    onSecondary = Color(0xFF001F0E),
    tertiary = Color(0xFFFFB300),
    background = Color(0xFF090A0D),
    onBackground = Color(0xFFE8ECF2),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE8ECF2),
    surfaceVariant = Color(0xFF181B22),
    onSurfaceVariant = Color(0xFFA2ACB8),
    outline = Color(0xFF2B3240)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = WorkstationDarkColorScheme,
        typography = Typography,
        content = content
    )
}
