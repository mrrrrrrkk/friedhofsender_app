package de.friedhofsender.app.audio
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class TtsController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val initDeferred = CompletableDeferred<Boolean>()
    private var tts: TextToSpeech? = null
    private var volume: Float = 1.0f
    private var speakJob: Job? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.GERMAN
                initDeferred.complete(true)
            } else {
                initDeferred.complete(false)
            }
        }
    }
    fun setVolume(v: Float) {
        volume = v.coerceIn(0f, 1f)
    }
    fun setMute(isMuted: Boolean) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                if (isMuted) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
                0
            )
        } else {
            @Suppress("DEPRECATION")
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, isMuted)
        }
    }
    fun stop() {
        speakJob?.cancel()
        tts?.stop()
    }
    private fun cleanText(text: String): String {
        return text
            .replace(Regex("\\[.*?\\]"), "")
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("\\{.*?\\}"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    private fun splitIntoSentences(text: String): List<String> {
        val sentenceRegex = Regex("[.!?;:]")

        return text.split(sentenceRegex)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
    suspend fun speakStreaming(text: String) = withContext(Dispatchers.Default) {
        initDeferred.await()
        stop()
        val cleanedText = cleanText(text)
        val sentences = splitIntoSentences(cleanedText)
        coroutineScope {
            speakJob = launch {
                for (sentence in sentences) {
                    if (!isActive) break
                    if (sentence.contains("[") || sentence.contains("(") || sentence.contains("{")) {
                        continue
                    }
                    val params = Bundle().apply {
                        putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)
                    }
                    tts?.speak(sentence, TextToSpeech.QUEUE_ADD, params, "tts_${System.nanoTime()}")
                    while (isActive && tts?.isSpeaking == true) {
                        delay(100L)
                    }
                    delay(300L)
                }
            }
            speakJob?.join()
        }
    }
    fun shutdown() {
        stop()
        tts?.shutdown()
    }
}