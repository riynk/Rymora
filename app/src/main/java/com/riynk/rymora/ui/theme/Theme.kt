package com.riynk.rymora.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RymoraColorScheme = darkColorScheme(
    primary = RymoraPrimary,
    secondary = RymoraSecondary,
    background = RymoraBackground,
    surface = RymoraSurface,
    surfaceVariant = RymoraSurfaceVariant,
    onPrimary = RymoraBackground,
    onBackground = RymoraText,
    onSurface = RymoraText,
    onSurfaceVariant = RymoraMuted
)

@Composable
fun RymoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RymoraColorScheme,
        content = content
    )
}
