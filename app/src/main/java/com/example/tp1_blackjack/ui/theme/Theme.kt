package com.example.tp1_blackjack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// A fixed casino palette avoids unpredictable wallpaper-based Material colors.
private val CasinoDarkColors = darkColorScheme(
    primary = Color(0xFF326449),
    onPrimary = Color.White,
    secondary = Color(0xFFDBC98D),
    onSecondary = Color(0xFF231D0C),
    tertiary = Color(0xFF93DEAA),
    onTertiary = Color(0xFF052B17),
    background = Color(0xFF081810),
    onBackground = Color.White,
    surface = Color(0xFF13251C),
    onSurface = Color.White,
)

private val CasinoLightColors = lightColorScheme(
    primary = Color(0xFF285B40),
    onPrimary = Color.White,
    secondary = Color(0xFF776321),
    onSecondary = Color.White,
    tertiary = Color(0xFF166B42),
    onTertiary = Color.White,
    background = Color(0xFFF4F6F0),
    onBackground = Color(0xFF15251B),
    surface = Color.White,
    onSurface = Color(0xFF15251B),
)

@Composable
fun TP1_BlackJackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) CasinoDarkColors else CasinoLightColors,
        typography = Typography,
        content = content,
    )
}
