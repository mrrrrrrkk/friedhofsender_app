package de.friedhofsender.app.audio
import android.content.Context
import android.content.Intent
class MusicPlayer(private val context: Context) {
    companion object {
        const val STREAM_TYPE_MUSIC = "music"
        const val STREAM_TYPE_LIVE = "live"
        const val ACTION_PAUSE_MUSIC = "PAUSE_MUSIC"
        const val ACTION_RESUME_MUSIC = "RESUME_MUSIC"
    }
    fun playUrl(url: String, streamType: String = STREAM_TYPE_MUSIC) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_START
            putExtra(MusicService.EXTRA_URL, url)
            putExtra(MusicService.EXTRA_STREAM_TYPE, streamType)
        }
        context.startForegroundService(intent)
    }
    fun stop(streamType: String = STREAM_TYPE_MUSIC) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_STOP
            putExtra(MusicService.EXTRA_STREAM_TYPE, streamType)
        }
        context.startService(intent)
    }
    fun pauseMusic() {
        val intent = Intent(context, MusicService::class.java).apply {
            action = ACTION_PAUSE_MUSIC
        }
        context.startService(intent)
    }
    fun resumeMusic() {
        val intent = Intent(context, MusicService::class.java).apply {
            action = ACTION_RESUME_MUSIC
        }
        context.startService(intent)
    }
    fun setVolume(v: Float, streamType: String = STREAM_TYPE_MUSIC) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_VOLUME
            putExtra(MusicService.EXTRA_VOLUME, v)
            putExtra(MusicService.EXTRA_STREAM_TYPE, streamType)
        }
        context.startService(intent)
    }
}