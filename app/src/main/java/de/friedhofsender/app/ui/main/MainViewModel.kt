package de.friedhofsender.app.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.friedhofsender.app.audio.MusicPlayer
import de.friedhofsender.app.audio.TtsController
import de.friedhofsender.app.data.GroqRepository
import de.friedhofsender.app.data.WebRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicPlayer: MusicPlayer,
    private val webRepository: WebRepository,
    private val groqRepository: GroqRepository,
    private val tts: TtsController
) : ViewModel() {

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text

    private val _status = MutableStateFlow("Bereit.")
    val status: StateFlow<String> = _status

    private val _nowPlaying = MutableStateFlow("")
    val nowPlaying: StateFlow<String> = _nowPlaying

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    // ✅ Live Stream Status (unabhängig von Musik)
    private val _isLiveStreamPlaying = MutableStateFlow(true)
    val isLiveStreamPlaying: StateFlow<Boolean> = _isLiveStreamPlaying

    private val _musicVolume = MutableStateFlow(0.33f)
    val musicVolume: StateFlow<Float> = _musicVolume

    private val _ttsVolume = MutableStateFlow(0.8f)
    val ttsVolume: StateFlow<Float> = _ttsVolume

    private val _showIntro = MutableStateFlow(true)
    val showIntro: StateFlow<Boolean> = _showIntro

    private val _showTextOverlay = MutableStateFlow(false)
    val showTextOverlay: StateFlow<Boolean> = _showTextOverlay

    private val _isNoise = MutableStateFlow(false)
    val isNoise: StateFlow<Boolean> = _isNoise

    init {
        // ✅ Live Stream beim Start starten (OHNE Musik zu beeinflussen)
        startLiveStream()
        refreshNowPlaying()
        viewModelScope.launch {
            while (true) {
                delay(20000)
                refreshNowPlaying()
            }
        }
    }

    // ✅ SAUBER: Live Stream Toggle - NUR Live Stream, KEINE Musik-Änderung
    fun toggleLiveStream() {
        if (_isLiveStreamPlaying.value) {
            stopLiveStream()
        } else {
            startLiveStream()
        }
    }

    private fun startLiveStream() {
        musicPlayer.playUrl(
            webRepository.getLiveStreamUrl(),
            MusicPlayer.STREAM_TYPE_LIVE
        )
        musicPlayer.setVolume(
            if (_isMuted.value) 0f else _musicVolume.value,
            MusicPlayer.STREAM_TYPE_LIVE
        )
        _isLiveStreamPlaying.value = true
    }

    private fun stopLiveStream() {
        // ✅ WICHTIG: Stoppe NUR den Live-Stream, NICHT die Musik!
        musicPlayer.stop(MusicPlayer.STREAM_TYPE_LIVE)
        _isLiveStreamPlaying.value = false
    }

    fun handleSpeakAction() {
        if (_isSpeaking.value) {
            tts.stop()
            _isSpeaking.value = false
        } else {
            viewModelScope.launch {
                _isSpeaking.value = true
                tts.setMute(_isMuted.value)
                tts.setVolume(_ttsVolume.value)
                tts.speakStreaming(_text.value)
                _isSpeaking.value = false
            }
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        tts.setMute(_isMuted.value)

        // ✅ Setze Volume für BEIDE Streams unabhängig
        val volume = if (_isMuted.value) 0f else _musicVolume.value
        musicPlayer.setVolume(volume, MusicPlayer.STREAM_TYPE_MUSIC)
        musicPlayer.setVolume(volume, MusicPlayer.STREAM_TYPE_LIVE)
    }

    fun setMusicVolume(v: Float) {
        _musicVolume.value = v
        if (!_isMuted.value) {
            // ✅ Setze Volume für BEIDE Streams
            musicPlayer.setVolume(v, MusicPlayer.STREAM_TYPE_MUSIC)
            musicPlayer.setVolume(v, MusicPlayer.STREAM_TYPE_LIVE)
        }
    }

    fun setTtsVolume(v: Float) {
        _ttsVolume.value = v
        if (!_isMuted.value) tts.setVolume(v)
    }

    fun generate() {
        viewModelScope.launch {
            _status.value = "Empfange..."
            _isNoise.value = true
            try {
                _text.value = groqRepository.generateBroadcast(webRepository.getPrompt())
            } catch (e: Exception) {
                _text.value = "Übertragungsfehler."
            } finally {
                delay(1500)
                _isNoise.value = false
                _status.value = "Bereit."
            }
        }
    }

    fun toggleMusic() {
        if (_isPlaying.value) {
            // ✅ Stoppe NUR die Musik, NICHT den Live-Stream
            musicPlayer.stop(MusicPlayer.STREAM_TYPE_MUSIC)
            _isPlaying.value = false
        } else {
            // ✅ Starte Musik UNABHÄNGIG vom Live-Stream
            musicPlayer.playUrl(
                webRepository.getMusicUrl(),
                MusicPlayer.STREAM_TYPE_MUSIC
            )
            musicPlayer.setVolume(
                if (_isMuted.value) 0f else _musicVolume.value,
                MusicPlayer.STREAM_TYPE_MUSIC
            )
            _isPlaying.value = true
        }
    }

    private fun refreshNowPlaying() {
        viewModelScope.launch {
            try {
                _nowPlaying.value = webRepository.getNowPlaying().trim()
            } catch (e: Exception) {
                _nowPlaying.value = "Unbekannt"
            }
        }
    }

    fun dismissIntro() {
        _showIntro.value = false
    }

    fun openTextOverlay() {
        _showTextOverlay.value = true
    }

    fun closeTextOverlay() {
        _showTextOverlay.value = false
    }

    override fun onCleared() {
        tts.shutdown()
        super.onCleared()
    }
}