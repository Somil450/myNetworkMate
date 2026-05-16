package com.signalsense.ai.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DeepBlue = Color(0xFF0A0E21)
val ElectricBlue = Color(0xFF00E5FF)
val NeonCyan = Color(0xFF18FFFF)
val GlassWhite = Color(0x1AFFFFFF)
val WarningOrange = Color(0xFFFFAB40)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    secondary = NeonCyan,
    tertiary = WarningOrange,
    background = DeepBlue,
    surface = DeepBlue,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun SignalSenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
