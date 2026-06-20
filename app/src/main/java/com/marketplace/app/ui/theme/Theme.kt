package com.marketplace.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Indigo = Color(0xFF3949AB)
private val Teal = Color(0xFF00897B)

private val LightColors = lightColorScheme(
    primary = Indigo,
    secondary = Teal,
    tertiary = Teal,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FA8DA),
    secondary = Color(0xFF4DB6AC),
    tertiary = Color(0xFF4DB6AC),
)

@Composable
fun MarketplaceTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content,
    )
}
