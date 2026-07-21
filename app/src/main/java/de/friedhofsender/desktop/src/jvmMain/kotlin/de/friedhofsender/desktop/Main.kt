package de.friedhofsender.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import de.friedhofsender.shared.models.StreamInfo

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Friedhofsender - Desktop Companion",
        state = rememberWindowState(width = 1024.dp, height = 720.dp) // Standard-Fenstergröße für Desktop
    ) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            DesktopMainScreen()
        }
    }
}

@Composable
fun DesktopMainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val currentStream = remember { StreamInfo(title = "Friedhofsender Live", artist = "Groq Auto-DJ") }

    Row(modifier = Modifier.fillMaxSize()) {
        // 1. Linke Desktop-Sidebar (NavigationRail)
        NavigationRail(
            modifier = Modifier.fillMaxHeight(),
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            NavigationRailItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Text("📻") },
                label = { Text("Radio") }
            )
            NavigationRailItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Text("🎙️") },
                label = { Text("Groq KI") }
            )
            NavigationRailItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Text("⚙️") },
                label = { Text("Settings") }
            )
        }

        // 2. Rechter Hauptbereich (Inhalt je nach Sidebar-Auswahl)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab) {
                0 -> RadioPlayerView(currentStream)
                1 -> Text("Groq KI Steuerung (Kommt im nächsten Schritt)", style = MaterialTheme.typography.headlineMedium)
                2 -> Text("Einstellungen (Audio-Ausgabe, Server-URL)", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

@Composable
fun RadioPlayerView(stream: StreamInfo) {
    Card(
        modifier = Modifier.width(400.dp).padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "LIVE STREAM", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stream.title, style = MaterialTheme.typography.headlineSmall)
            Text(text = stream.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { /* Audio-Play Logik */ }) {
                Text("▶ Play / Stop")
            }
        }
    }
}