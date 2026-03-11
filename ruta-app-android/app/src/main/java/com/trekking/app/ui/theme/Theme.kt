package com.trekking.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4c669f),
    secondary = Color(0xFF3b5998),
    tertiary = Color(0xFF192f6a)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4c669f),
    secondary = Color(0xFF3b5998),
    tertiary = Color(0xFF192f6a),
    background = Color(0xFFF0F4F8),
    surface = Color(0xFFFFFFFF),
)

@Composable
fun TrekkingAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
