package com.isax.launcher.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object IsaxColors {
    val Cyan = Color(0xFF00F0FF)
    val Violet = Color(0xFF7A5CFF)
    val Deep = Color(0xFF0B101D)
    val Deep2 = Color(0xFF111A2E)
    val Glass = Color(0xCC0B101D)
    val Text = Color(0xFFE8F6FF)
}

@Composable
fun IsaxTheme(accent: Color = IsaxColors.Cyan, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = accent,
            background = IsaxColors.Deep,
            surface = IsaxColors.Deep2,
            onPrimary = IsaxColors.Deep,
            onBackground = IsaxColors.Text,
            onSurface = IsaxColors.Text
        ),
        content = content
    )
}
