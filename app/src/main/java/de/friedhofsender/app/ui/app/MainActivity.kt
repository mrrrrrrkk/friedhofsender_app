package de.friedhofsender.app
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint
import de.friedhofsender.app.ui.main.MainScreen
import de.friedhofsender.app.ui.theme.FriedhofsenderTheme
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        setContent {
            FriedhofsenderTheme {
                MainScreen()
            }
        }
    }
}