package com.kilagbe.fakegps.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

val Teal = Color(0xFF0D9488)
val TealDark = Color(0xFF0F766E)
val TealSoft = Color(0xFFF0FBF9)
val BgColor = Color(0xFFF4F6F5)
val SurfaceColor = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF64748B)
val BorderColor = Color(0xFFE7ECEA)

private val colorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    background = BgColor,
    surface = SurfaceColor,
    onSurface = TextPrimary,
)

@Composable
fun FakeGPSTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
