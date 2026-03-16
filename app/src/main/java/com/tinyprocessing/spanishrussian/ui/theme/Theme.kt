package com.tinyprocessing.spanishrussian.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = Gray400,
    onSecondary = Black,
    tertiary = Gray600,
    background = Black,
    onBackground = White,
    surface = Gray900,
    onSurface = White,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray300,
    outline = Gray700,
    outlineVariant = Gray800,
)

@Composable
fun SpanishRussianTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}
