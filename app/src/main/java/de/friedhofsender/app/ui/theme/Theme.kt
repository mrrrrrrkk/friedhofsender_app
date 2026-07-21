package de.friedhofsender.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBB86FC),       // Neon-Violett
    onPrimary = Color.Black,
    background = Color(0xFF000000),    // Tiefes Schwarz
    surface = Color(0xFF140014),
    onSurface = Color(0xFFE6E6E6)
)

@Composable
fun FriedhofsenderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography(),
        content = content
    )
}
