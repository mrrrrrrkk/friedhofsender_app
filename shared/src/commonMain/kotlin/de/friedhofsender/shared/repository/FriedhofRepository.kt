package de.friedhofsender.shared.repository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
@Serializable
data class BroadcastRequest(val prompt: String)
@Serializable
data class BroadcastResponse(
    val text: String? = null,
    val error: String? = null
)
@Serializable
data class NowPlayingResponse(val current: String? = null)
class FriedhofRepository {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(jsonParser)
        }
        expectSuccess = false
    }
    suspend fun getNowPlaying(): String {
        return try {
            val response = client.get("https://www.friedhofsender.de/live/nowplaying.json") {
                header(HttpHeaders.UserAgent, "FriedhofsenderDesktop/1.0")
            }
            if (response.status.isSuccess()) {
                val rawJson = response.bodyAsText()
                val data = jsonParser.decodeFromString<NowPlayingResponse>(rawJson)
                data.current?.trim()?.ifBlank { "Unbekannt" } ?: "Unbekannt"
            } else {
                "Fehler beim Laden (${response.status.value})"
            }
        } catch (e: Exception) {
            "Unbekannt"
        }
    }
    fun getPrompt(additionalTopic: String = ""): String {
        val basePrompt = "Erzeuge eine neue Friedhofsdurchsage im nüchternen, leicht unheimlichen Stil. " +
                "Behandle die Stadt als einen festen Ort mit einer fortlaufenden Geschichte. " +
                "Die Stadtbewohner, Vorkommnisse, Ungewöhnliches, Der Friedhof, Bekannte Orte, Nachrichten. " +
                "Es ist gut und erwünscht, wenn bereits bekannte Personen oder Orte der Stadt erneut auftauchen, " +
                "aber erzähle ihre Geschichte weiter oder füge ein neues, sinnvolles Detail hinzu. " +
                "Alles muss Teil eines großen, zusammenhängenden Ganzen sein. " +
                "Die Worte Night Vale und Stil dürfen nicht vorkommen. " +
                "Zuhörer werden begrüßt und verabschiedet wie in einer seltsamen Radiosendung. " +
                "Am Ende der Nachricht folgt eine kurze Geschichte über einen Stadtbewohner oder ein Ereignis. " +
                "Schreibe mindestens 3-4 Absätze für eine gute Durchsage."
        return if (additionalTopic.isNotBlank()) {
            "$basePrompt Zusätzlicher Fokus/Themenwunsch für diese Ausgabe, der unbedingt integriert werden muss: \"$additionalTopic\"."
        } else {
            basePrompt
        }
    }
    suspend fun generateBroadcast(topic: String): String {
        val prompt = getPrompt(topic)
        return try {
            val response = client.post("https://api.friedhofsender.de/generate") {
                contentType(ContentType.Application.Json)
                setBody(BroadcastRequest(prompt = prompt))
            }
            if (response.status.isSuccess()) {
                val rawJson = response.bodyAsText()
                val broadcastResponse = jsonParser.decodeFromString<BroadcastResponse>(rawJson)
                broadcastResponse.text ?: broadcastResponse.error ?: "Keine Antwort erhalten."
            } else {
                val errorText = response.bodyAsText()
                "[SERVER-FEHLER ${response.status.value}] $errorText"
            }
        } catch (e: Exception) {
            "[NETZWERK-FEHLER] Konnte api.friedhofsender.de nicht erreichen: ${e.message}"
        }
    }
    fun getMusicUrl(): String = "https://www.friedhofsender.de/live/stream.m3u8"
}