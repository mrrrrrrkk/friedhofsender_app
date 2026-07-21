package de.friedhofsender.app.data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class WebRepository @Inject constructor() {
    private val jsonUrl = "https://www.friedhofsender.de/live/nowplaying.json"
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