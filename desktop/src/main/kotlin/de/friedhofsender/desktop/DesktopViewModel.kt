package de.friedhofsender.desktop

import de.friedhofsender.shared.repository.FriedhofRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

class DesktopViewModel {
    private val repository = FriedhofRepository()
    private val audioManager = DesktopAudioManager()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val text = MutableStateFlow("")
    val status = MutableStateFlow("Bereit.")
    val nowPlaying = MutableStateFlow("Lade Status...")
    val isPlaying = MutableStateFlow(false)
    val isMuted = MutableStateFlow(false)
    val isSpeaking = MutableStateFlow(false)
    val musicVolume = MutableStateFlow(0.35f)
    val ttsVolume = MutableStateFlow(0.8f)
    val isNoise = MutableStateFlow(false)
    val topicInput = MutableStateFlow("")

    init {
        audioManager.setMusicVolume(musicVolume.value)
        audioManager.setTtsVolume(ttsVolume.value)

        refreshNowPlaying()
        scope.launch {
            while (isActive) {
                delay(20000)
                refreshNowPlaying()
            }
        }
    }

    fun generateBroadcast() {
        val currentTopic = topicInput.value
        scope.launch {
            status.value = "Empfange..."
            isNoise.value = true
            try {
                text.value = repository.generateBroadcast(currentTopic)
            } catch (e: Exception) {
                text.value = "Übertragungsfehler beim Empfang."
            } finally {
                delay(1200)
                isNoise.value = false
                status.value = "Bereit."
            }
        }
    }

    fun clearTopic() {
        topicInput.value = ""
    }

    fun toggleSpeak() {
        if (isSpeaking.value) {
            audioManager.stopTTS()
            isSpeaking.value = false
        } else {
            if (text.value.isNotBlank()) {
                isSpeaking.value = true
                audioManager.speak(text.value) {
                    isSpeaking.value = false
                }
            }
        }
    }

    fun toggleMusic() {
        if (isPlaying.value) {
            audioManager.stopStream()
            isPlaying.value = false
        } else {
            audioManager.playStream(repository.getMusicUrl())
            isPlaying.value = true
        }
    }

    fun toggleMute() {
        val newMuteState = !isMuted.value
        isMuted.value = newMuteState
        audioManager.setMute(newMuteState)
    }

    fun setMusicVolume(value: Float) {
        musicVolume.value = value
        audioManager.setMusicVolume(value)
    }

    fun setTtsVolume(value: Float) {
        ttsVolume.value = value
        audioManager.setTtsVolume(value)
    }

    private fun refreshNowPlaying() {
        scope.launch {
            nowPlaying.value = repository.getNowPlaying()
        }
    }

    fun onCloseApp() {
        audioManager.stopAllNativeProcesses()
    }
}