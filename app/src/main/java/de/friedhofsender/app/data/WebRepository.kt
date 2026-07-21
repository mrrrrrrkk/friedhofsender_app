package de.friedhofsender.app.data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class WebRepository @Inject constructor() {
    private val jsonUrl = "https://www.friedhofsender.de/live/nowplaying.json"
    private val apiGenerateUrl = "https://api.friedhofsender.de/generate"
    suspend fun getNowPlaying(): String = withContext(Dispatchers.IO) {
        try {
            val jsonString = URL(jsonUrl).readText()
            val jsonObject = JSONObject(jsonString)
            val currentTrack = jsonObject.optString("current", "Unbekannt")
            currentTrack.trim().takeUnless { it.isBlank() } ?: "Unbekannt"
        } catch (e: Exception) {
            "Fehler beim Laden"
        }
    }
    suspend fun generateBroadcast(topic: String): String = withContext(Dispatchers.IO) {
        try {
            val basePrompt = getPrompt()
            val prompt = if (topic.isNotBlank()) {
                "$basePrompt Zusätzlicher Fokus/Themenwunsch für diese Ausgabe, der unbedingt integriert werden muss: \"$topic\"."
            } else {
                basePrompt
            }
            val url = URL(apiGenerateUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            val jsonBody = JSONObject().apply {
                put("prompt", prompt)
            }.toString()
            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(jsonBody)
                writer.flush()
            }
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                val responseObj = JSONObject(responseString)
                responseObj.optString("text", "").takeIf { it.isNotBlank() } 
                    ?: responseObj.optString("error", "Keine Antwort erhalten.")
            } else {
                val errorString = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                } catch (_: Exception) { "" }
                "[SERVER-FEHLER $responseCode] $errorString"
            }
        } catch (e: Exception) {
            "Übertragungsfehler: ${e.message}"
        }
    }
    suspend fun getPrompt(): String = withContext(Dispatchers.IO) {
        "Erzeuge eine neue Friedhofsdurchsage im nüchternen, leicht unheimlichen Stil. " +
                "Behandle die Stadt als einen festen Ort mit einer fortlaufenden Geschichte. " +
                "Die Stadtbewohner, Vorkommnisse, Ungewöhnliches, Der Friedhof, Bekannte Orte, Nachrichten, " +
                "Es ist gut und erwünscht, wenn bereits bekannte Personen oder Orte der Stadt erneut auftauchen, " +
                "aber erzähle ihre Geschichte weiter oder füge ein neues, sinnvolles Detail hinzu. " +
                "Alles muss Teil eines großen, zusammenhängenden Ganzen sein. " +
                "Die Worte Night Vale und Stil dürfen nicht vorkommen. " +
                "Zuhörer werden begrüßt und verabschiedet wie in einer seltsamen Radiosendung. " +
                "Am Ende der Nachricht folgt eine kurze Geschichte über einen Stadtbewohner oder ein Ereignis. " +
                "Schreibe mindestens 3-4 Absätze für eine gute Durchsage."
    }
    fun getMusicUrl(): String = "https://www.friedhofsender.de/live/stream.m3u8"
    fun getLiveStreamUrl(): String = ""
}