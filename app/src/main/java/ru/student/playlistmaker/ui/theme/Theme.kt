package ru.student.playlistmaker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

object PlaylistColors {
    val Blue = Color(0xFF3772E7)
    val Ink = Color(0xFF1A1B22)
    val Gray = Color(0xFFAEAFB4)
}

val PlaylistFont = FontFamily.SansSerif

@Composable
fun PlaylistTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) darkColorScheme(
        primary = PlaylistColors.Blue,
        background = PlaylistColors.Ink,
        surface = PlaylistColors.Ink,
        onBackground = Color.White,
        onSurface = Color.White,
        onSurfaceVariant = Color.White,
    ) else lightColorScheme(
        primary = PlaylistColors.Blue,
        background = Color.White,
        surface = Color.White,
        onBackground = PlaylistColors.Ink,
        onSurface = PlaylistColors.Ink,
        onSurfaceVariant = PlaylistColors.Gray,
    )
    MaterialTheme(colorScheme = colors, content = content)
}
