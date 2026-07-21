package de.friedhofsender.app.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroqRepository @Inject constructor(
    private val api: GroqApiService
) {

    suspend fun generateBroadcast(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d("GROQ", "Sende Prompt: $prompt")

            val request = GroqChatRequest(
                model = "llama-3.1-8b-instant",
                messages = listOf(GroqMessage("user", prompt))
            )

            val response = api.createChatCompletion(request)

            val result = response.choices.firstOrNull()?.message?.content
            Log.d("GROQ", "Antwort: $result")

            result ?: "Keine Antwort von der KI erhalten."
        } catch (e: Exception) {
            Log.e("GROQ", "Fehler bei API-Aufruf", e)
            "Fehler bei der Generierung der Durchsage."
        }
    }
}
