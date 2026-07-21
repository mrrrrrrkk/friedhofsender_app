package de.friedhofsender.app.audio
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Network
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import de.friedhofsender.app.R
import java.util.concurrent.atomic.AtomicBoolean
class MusicService : Service() {
    companion object {
        const val CHANNEL_ID = "friedhofsender_audio"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val ACTION_VOLUME = "VOLUME"
        const val EXTRA_URL = "URL"
        const val EXTRA_VOLUME = "VOLUME"
        const val EXTRA_TITLE = "TITLE"
        const val EXTRA_STREAM_TYPE = "STREAM_TYPE"
        const val STREAM_TYPE_MUSIC = "music"
        const val STREAM_TYPE_LIVE = "live"
    }
    private var musicPlayer: ExoPlayer? = null
    private var liveStreamPlayer: ExoPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private var currentTitle: String = "Friedhofsender"
    private val mainHandler = Handler(Looper.getMainLooper())
    private var retryDelay = 2000L
    private val isForegroundStarted = AtomicBoolean(false)
    private val isServiceDestroyed = AtomicBoolean(false)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createMediaSession()
        registerNetworkCallback()
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (!isForegroundStarted.getAndSet(true)) {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            }
        } catch (e: Exception) {
            isForegroundStarted.set(false)
            return START_STICKY
        }
        if (intent != null) {
            try {
                MediaButtonReceiver.handleIntent(mediaSession, intent)
            } catch (_: Exception) {
            }
            val streamType = intent.getStringExtra(EXTRA_STREAM_TYPE) ?: STREAM_TYPE_MUSIC
            when (intent.action) {
                ACTION_START -> {
                    val url = intent.getStringExtra(EXTRA_URL) ?: return START_STICKY
                    currentTitle = intent.getStringExtra(EXTRA_TITLE) ?: "Friedhofsender"
                    if (streamType == STREAM_TYPE_LIVE) {
                        startLiveStream(url)
                    } else {
                        startPlayer(url)
                    }
                    updatePlaybackState()
                    updateNotification()
                }
                ACTION_STOP -> {
                    if (streamType == STREAM_TYPE_LIVE) {
                        stopLiveStream()
                    } else {
                        stopPlayer()
                    }
                    updatePlaybackState()
                    updateNotification()
                    if (musicPlayer == null && liveStreamPlayer == null) {
                        try {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                        } catch (_: Exception) {
                        }
                        isForegroundStarted.set(false)
                        stopSelf()
                    }
                }
                ACTION_VOLUME -> {
                    val vol = intent.getFloatExtra(EXTRA_VOLUME, 1f)
                    if (streamType == STREAM_TYPE_LIVE) {
                        liveStreamPlayer?.volume = vol
                    } else {
                        musicPlayer?.volume = vol
                    }
                }
            }
        }
        return START_STICKY
    }
    private fun startPlayer(url: String) {
        if (musicPlayer == null) {
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    5000,
                    33000,
                    1500,
                    3000
                )
                .build()
            musicPlayer = ExoPlayer.Builder(this)
                .setLoadControl(loadControl)
                .build()
            musicPlayer!!.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_NONE)
                    .build(),
                true
            )
            mediaSession.isActive = true
            musicPlayer!!.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlaybackState()
                    updateNotification()
                }
                override fun onPlayerError(error: PlaybackException) {
                    android.util.Log.e("MusicService", "Music playback error: ${error.message}")
                    musicPlayer?.playWhenReady = false
                    val cause = error.cause
                    if (cause !is androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException) {
                        mainHandler.postDelayed({
                            try {
                                val p = musicPlayer ?: return@postDelayed
                                if (!isServiceDestroyed.get()) {
                                    p.prepare()
                                    p.playWhenReady = true
                                    retryDelay = 2000L
                                }
                            } catch (_: Exception) {
                                retryDelay = (retryDelay * 1.5).toLong().coerceAtMost(15000L)
                            }
                        }, retryDelay)
                    }
                }
            })
        }
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType("application/x-mpegURL")
            .build()
        musicPlayer!!.apply {
            stop()
            clearMediaItems()
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
        updateMetadata()
        updatePlaybackState()
        updateNotification()
    }
    private fun stopPlayer() {
        try {
            musicPlayer?.stop()
            musicPlayer?.release()
        } catch (_: Exception) {
        }
        musicPlayer = null
        try {
            mediaSession.isActive = false
        } catch (_: Exception) {
        }
    }
    private fun startLiveStream(url: String) {
        if (liveStreamPlayer == null) {
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    2000,
                    8000,
                    1000,
                    2000
                )
                .build()
            liveStreamPlayer = ExoPlayer.Builder(this)
                .setLoadControl(loadControl)
                .build()
            liveStreamPlayer!!.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_ASSISTANT)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_NONE)
                    .build(),
                false
            )
            liveStreamPlayer!!.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    android.util.Log.e("MusicService", "Live stream playback error: ${error.message}")
                    liveStreamPlayer?.playWhenReady = false
                    val cause = error.cause
                    val shouldRetry = cause is androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException ||
                            cause is java.io.IOException ||
                            cause is java.net.SocketTimeoutException
                    if (shouldRetry) {
                        mainHandler.postDelayed({
                            try {
                                val p = liveStreamPlayer ?: return@postDelayed
                                if (!isServiceDestroyed.get() && p.playWhenReady) {
                                    android.util.Log.d("MusicService", "Retrying live stream connection...")
                                    p.prepare()
                                    p.playWhenReady = true
                                }
                            } catch (_: Exception) {
                            }
                        }, 5000)
                    }
                }
            })
        }
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType("application/x-mpegURL")
            .build()
        liveStreamPlayer!!.apply {
            stop()
            clearMediaItems()
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
        android.util.Log.d("MusicService", "Starting live stream: $url")
    }
    private fun stopLiveStream() {
        try {
            liveStreamPlayer?.apply {
                playWhenReady = false
                stop()
                release()
            }
        } catch (_: Exception) {
        }
        liveStreamPlayer = null
    }
    override fun onDestroy() {
        isServiceDestroyed.set(true)
        stopPlayer()
        stopLiveStream()
        try {
            mediaSession.release()
        } catch (_: Exception) {
        }
        super.onDestroy()
    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        isServiceDestroyed.set(true)
        stopPlayer()
        stopLiveStream()
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
        }
        isForegroundStarted.set(false)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }
    private fun createMediaSession() {
        mediaSession = MediaSessionCompat(this, "FriedhofsenderSession")
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                try {
                    musicPlayer?.playWhenReady = true
                    updatePlaybackState()
                    updateNotification()
                } catch (_: Exception) {
                }
            }
            override fun onPause() {
                try {
                    musicPlayer?.playWhenReady = false
                    updatePlaybackState()
                    updateNotification()
                } catch (_: Exception) {
                }
            }
            override fun onStop() {
                try {
                    stopPlayer()
                    stopSelf()
                } catch (_: Exception) {
                }
            }
        })
    }
    private fun updateMetadata() {
        try {
            val coverBitmap = BitmapFactory.decodeResource(
                resources,
                R.drawable.hintergrundfriedhofsender
            )
            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Friedhofsender")
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, coverBitmap)
                .build()

            mediaSession.setMetadata(metadata)
        } catch (_: Exception) {
        }
    }
    private fun buildPlaybackState(): PlaybackStateCompat {
        val isPlaying = musicPlayer?.isPlaying == true
        return PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_STOP
            )
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING
                else PlaybackStateCompat.STATE_PAUSED,
                musicPlayer?.currentPosition ?: 0,
                1f
            )
            .build()
    }
    private fun updatePlaybackState() {
        try {
            mediaSession.setPlaybackState(buildPlaybackState())
        } catch (_: Exception) {
        }
    }
    private fun buildNotification(): Notification {
        val isPlaying = musicPlayer?.isPlaying == true
        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "Pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PAUSE
                )
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                "Play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PLAY
                )
            )
        }
        val coverBitmap = BitmapFactory.decodeResource(
            resources,
            R.drawable.hintergrundfriedhofsender
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Friedhofsender")
            .setContentText(currentTitle.ifBlank { "NOW PLAYING: –" })
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setLargeIcon(coverBitmap)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .addAction(playPauseAction)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0)
            )
            .build()
    }
    private fun updateNotification() {
        try {
            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(NOTIFICATION_ID, buildNotification())
        } catch (_: Exception) {
        }
    }
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Friedhofsender Audio",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            enableVibration(false)
            setSound(null, null)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
    private fun registerNetworkCallback() {
        try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onLost(network: Network) {
                    mainHandler.post {
                        musicPlayer?.playWhenReady = false
                        liveStreamPlayer?.playWhenReady = false
                    }
                }
                override fun onAvailable(network: Network) {
                    mainHandler.post {
                        if (isServiceDestroyed.get()) return@post
                        try {
                            musicPlayer?.let { p ->
                                if (p.playbackState == Player.STATE_IDLE ||
                                    p.playbackState == Player.STATE_BUFFERING
                                ) {
                                    mainHandler.postDelayed({
                                        try {
                                            if (!isServiceDestroyed.get()) {
                                                p.prepare()
                                                p.playWhenReady = true
                                            }
                                        } catch (_: Exception) {}
                                    }, 1500)
                                }
                            }
                            liveStreamPlayer?.let { p ->
                                if (p.playbackState == Player.STATE_IDLE ||
                                    p.playbackState == Player.STATE_BUFFERING
                                ) {
                                    mainHandler.postDelayed({
                                        try {
                                            if (!isServiceDestroyed.get()) {
                                                p.prepare()
                                                p.playWhenReady = true
                                            }
                                        } catch (_: Exception) {}
                                    }, 1500)
                                }
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            })
        } catch (_: Exception) {
        }
    }
}