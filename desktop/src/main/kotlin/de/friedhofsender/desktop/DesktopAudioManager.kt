package de.friedhofsender.desktop

import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.zip.ZipInputStream

class DesktopAudioManager {
    private var speakProcess: Process? = null
    private var speakJob: Job? = null
    private var streamProcess: Process? = null
    private var volumeJob: Job? = null

    private val audioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var currentMusicVolume: Float = 0.35f
    private var currentTtsVolume: Float = 0.8f
    private var isMuted: Boolean = false

    private val appDir = File(System.getProperty("user.home"), ".friedhofsender").apply { mkdirs() }
    private val ffplayExe = File(appDir, "ffplay.exe")
    private var lastStreamUrl: String = "https://www.friedhofsender.de/live/stream.m3u8"

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            stopAllNativeProcesses()
        })
    }

    fun setMusicVolume(volume: Float) {
        currentMusicVolume = volume.coerceIn(0f, 1f)
        triggerSmoothVolumeUpdate()
    }

    fun setMute(muted: Boolean) {
        isMuted = muted
        triggerSmoothVolumeUpdate()
    }

    private fun triggerSmoothVolumeUpdate() {
        if (streamProcess?.isAlive == true) {
            volumeJob?.cancel()
            volumeJob = audioScope.launch {
                delay(250)
                playStream(lastStreamUrl)
            }
        }
    }

    fun setTtsVolume(volume: Float) {
        currentTtsVolume = volume.coerceIn(0f, 1f)
    }

    fun speak(rawText: String, onFinished: () -> Unit) {
        stopTTS()

        speakJob = audioScope.launch {
            try {
                val cleanText = sanitizeTextForSpeech(rawText)
                if (cleanText.isBlank()) {
                    withContext(Dispatchers.Main) { onFinished() }
                    return@launch
                }

                val osName = System.getProperty("os.name").lowercase()
                if (!osName.contains("win")) {
                    println("TTS wird auf diesem Betriebssystem ($osName) aktuell nicht unterstützt.")
                    delay(1500) // Simuliert eine kurze Sprachdauer für das UI
                    return@launch
                }

                val base64Text = Base64.getEncoder().encodeToString(cleanText.toByteArray(StandardCharsets.UTF_8))
                val tempScript = File.createTempFile("friedhof_tts_", ".ps1")
                tempScript.deleteOnExit()

                val effectiveVolume = if (isMuted) 0 else (currentTtsVolume * 100).toInt()

                val psContent = """
                    [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
                    ${'$'}bytes = [System.Convert]::FromBase64String("$base64Text")
                    ${'$'}text = [System.Text.Encoding]::UTF8.GetString(${'$'}bytes)
                    
                    Add-Type -AssemblyName System.Speech
                    ${'$'}synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
                    ${'$'}synth.Volume = $effectiveVolume

                    try {
                        ${'$'}voices = ${'$'}synth.GetInstalledVoices()
                        ${'$'}naturalVoice = ${'$'}voices | Where-Object { ${'$'}_.VoiceInfo.Name -like "*Natural*" -or ${'$'}_.VoiceInfo.Name -like "*Neural*" -or ${'$'}_.VoiceInfo.Name -like "*Katja*" -or ${'$'}_.VoiceInfo.Name -like "*Hedda*" } | Select-Object -First 1
                        if (${'$'}naturalVoice) {
                            ${'$'}synth.SelectVoice(${'$'}naturalVoice.VoiceInfo.Name)
                        } else {
                            ${'$'}deVoice = ${'$'}voices | Where-Object { ${'$'}_.VoiceInfo.Culture -like "de-*" } | Select-Object -First 1
                            if (${'$'}deVoice) { ${'$'}synth.SelectVoice(${'$'}deVoice.VoiceInfo.Name) }
                        }
                    } catch {}

                    ${'$'}synth.Speak(${'$'}text)
                """.trimIndent()

                tempScript.writeText(psContent, StandardCharsets.UTF_8)

                val pb = ProcessBuilder("powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", tempScript.absolutePath)
                speakProcess = pb.start()
                speakProcess?.waitFor()

                tempScript.delete()
            } catch (e: Exception) {
                println("TTS Fehler: ${e.message}")
            } finally {
                withContext(Dispatchers.Main) {
                    onFinished()
                }
            }
        }
    }

    fun stopTTS() {
        speakJob?.cancel()
        speakJob = null
        try {
            speakProcess?.let {
                it.destroyForcibly()
                if (it.isAlive) {
                    val osName = System.getProperty("os.name").lowercase()
                    if (osName.contains("win")) {
                        ProcessBuilder("taskkill", "/f", "/pid", it.pid().toString()).start()
                    }
                }
            }
            speakProcess = null
        } catch (_: Exception) {}
    }

    private fun sanitizeTextForSpeech(input: String): String {
        return input
            .replace(Regex("\\[.*?\\]"), " ")
            .replace(Regex("\\(.*?\\)"), " ")
            .replace(Regex("\\{.*?\\}"), " ")
            .replace("\r\n", ". ")
            .replace("\n", ". ")
            .replace("\r", ". ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun playStream(streamUrl: String) {
        lastStreamUrl = streamUrl
        val oldProcess = streamProcess

        audioScope.launch {
            try {
                ensureFFplayAvailable()

                val volumeMultiplier = if (isMuted) 0.0f else currentMusicVolume

                val pb = ProcessBuilder(
                    ffplayExe.absolutePath,
                    "-nodisp",
                    "-autoexit",
                    "-af", "volume=$volumeMultiplier",
                    "-loglevel", "quiet",
                    "-fflags", "+genpts+ignidx",
                    "-probesize", "5000000",
                    "-analyzeduration", "5000000",
                    "-i", streamUrl
                )

                val newProcess = pb.start()
                streamProcess = newProcess

                delay(150)
                oldProcess?.let {
                    it.destroyForcibly()
                    if (it.isAlive) {
                        val osName = System.getProperty("os.name").lowercase()
                        if (osName.contains("win")) {
                            ProcessBuilder("taskkill", "/f", "/pid", it.pid().toString()).start()
                        }
                    }
                }

            } catch (e: Exception) {
                println("Stream Error: ${e.message}")
            }
        }
    }

    fun stopStream() {
        volumeJob?.cancel()
        try {
            streamProcess?.let {
                it.destroyForcibly()
                if (it.isAlive) {
                    val osName = System.getProperty("os.name").lowercase()
                    if (osName.contains("win")) {
                        ProcessBuilder("taskkill", "/f", "/pid", it.pid().toString()).start()
                    }
                }
            }
            streamProcess = null
        } catch (_: Exception) {}
    }

    fun stopAllNativeProcesses() {
        stopTTS()
        stopStream()
        audioScope.cancel()
        
        try {
            val osName = System.getProperty("os.name").lowercase()
            if (osName.contains("win")) {
                ProcessBuilder("taskkill", "/f", "/im", "ffplay.exe").start()
            }
        } catch (_: Exception) {}
    }

    private suspend fun ensureFFplayAvailable() = withContext(Dispatchers.IO) {
        if (ffplayExe.exists() && ffplayExe.length() < 100000L) {
            ffplayExe.delete()
        }

        if (!ffplayExe.exists()) {
            // Hinweis: Für Windows wird ffplay geladen. Für Linux bräuchte man ggf. die Linux-Binary von ffbinaries, 
            // oder man setzt voraus, dass ffmpeg/ffplay paketweit über apt installiert ist.
            val downloadUrl = "https://github.com/ffbinaries/ffbinaries-prebuilt/releases/download/v4.4.1/ffplay-4.4.1-win-64.zip"
            val tempZip = File(appDir, "ffplay.zip")

            val connection = URI.create(downloadUrl).toURL().openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            connection.getInputStream().use { input ->
                FileOutputStream(tempZip).use { output ->
                    input.copyTo(output)
                }
            }

            ZipInputStream(tempZip.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == "ffplay.exe") {
                        FileOutputStream(ffplayExe).use { out ->
                            zip.copyTo(out)
                        }
                        break
                    }
                    entry = zip.nextEntry
                }
            }
            tempZip.delete()
        }
    }
}