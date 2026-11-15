package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RoyalPurple,
    onPrimary = Color.White,
    primaryContainer = SurfaceCard,
    onPrimaryContainer = Color.White,
    secondary = PeacockCyan,
    onSecondary = MidnightDeep,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = PeacockCyan,
    tertiary = IndiaSaffron,
    onTertiary = MidnightDeep,
    background = MidnightDeep,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor
)

private val LightColorScheme = darkColorScheme(
    // We emphasize the rich dark-social room experience, tuned for high contrast
    primary = RoyalPurple,
    onPrimary = Color.White,
    secondary = PeacockCyan,
    onSecondary = MidnightDeep,
    tertiary = IndiaSaffron,
    background = MidnightDeep,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary
)

@Composable
fun UniqueChatIndiaTheme(
    darkTheme: Boolean = true, // Premium social voice apps look best in dark lounge mode
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
