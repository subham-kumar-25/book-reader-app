package com.bookreader.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Reading themes
enum class ReadingTheme { LIGHT, DARK, SEPIA }

// Light theme colors
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF5F5F5)
val LightOnBackground = Color(0xFF1A1A1A)
val LightPrimary = Color(0xFF2E7D32)

// Dark theme colors
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkOnBackground = Color(0xFFE8E8E8)
val DarkPrimary = Color(0xFF81C784)

// Sepia theme colors
val SepiaBackground = Color(0xFFF5E6C8)
val SepiaSurface = Color(0xFFEDD9A3)
val SepiaOnBackground = Color(0xFF3E2723)
val SepiaPrimary = Color(0xFF795548)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnBackground,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnBackground,
)

private val SepiaColorScheme = lightColorScheme(
    primary = SepiaPrimary,
    background = SepiaBackground,
    surface = SepiaSurface,
    onBackground = SepiaOnBackground,
    onSurface = SepiaOnBackground,
)

@Composable
fun BookReaderTheme(
    readingTheme: ReadingTheme = ReadingTheme.LIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (readingTheme) {
        ReadingTheme.LIGHT -> LightColorScheme
        ReadingTheme.DARK -> DarkColorScheme
        ReadingTheme.SEPIA -> SepiaColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
