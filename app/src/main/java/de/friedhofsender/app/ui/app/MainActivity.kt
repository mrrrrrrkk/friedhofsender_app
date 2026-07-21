package de.friedhofsender.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // NEU: Import für Vollbild
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint
import de.friedhofsender.app.ui.main.MainScreen
import de.friedhofsender.app.ui.theme.FriedhofsenderTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Runtime‑Permission Launcher
    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // Wir müssen hier nichts tun – PcmTap startet erst im Service.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        // ⭐ 1. Vollbild-Modus aktivieren (Muss VOR super.onCreate stehen)
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        // ⭐ Mikrofon‑Permission anfragen
        requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)

        setContent {
            FriedhofsenderTheme {
                MainScreen()
            }
        }
    }
}