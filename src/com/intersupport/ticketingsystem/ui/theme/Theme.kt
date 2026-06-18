package com.intersupport.ticketingsystem.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = AccentCyan,
    tertiary = TextGray,
    background = BackgroundBlack,
    surface = SurfaceDark,
    onPrimary = TextWhite,
    onSecondary = BackgroundBlack,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    error = ErrorRed
)

// We force the dark modern theme everywhere for the requested sleek black and blue UI
@Composable
fun INTERSUPPORTTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}