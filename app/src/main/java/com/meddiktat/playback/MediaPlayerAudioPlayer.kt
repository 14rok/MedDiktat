package com.meddiktat.playback

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.meddiktat.domain.playback.AudioPlayer
import com.meddiktat.domain.playback.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MediaPlayer-Implementierung der [AudioPlayer]-Abstraktion. Spielt Diktate
 * ausschließlich in-App ab; es wird kein externer Player-Intent ausgelöst.
 */
@Singleton
class MediaPlayerAudioPlayer @Inject constructor() : AudioPlayer {

    private val _state = MutableStateFlow(PlayerState())
    override val state = _state.asStateFlow()

    private var player: MediaPlayer? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var tickJob: Job? = null

    override fun prepare(file: File) {
        release()
        val mp = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            setDataSource(file.absolutePath)
            setOnCompletionListener {
                tickJob?.cancel()
                _state.update {
                    it.copy(status = PlayerState.Status.COMPLETED, positionMs = it.durationMs)
                }
            }
            prepare() // Datei ist lokal & klein -> synchrones prepare ist unkritisch.
        }
        player = mp
        _state.value = PlayerState(
            status = PlayerState.Status.PREPARED,
            positionMs = 0L,
            durationMs = mp.duration.toLong().coerceAtLeast(0L),
        )
    }

    override fun play() {
        val mp = player ?: return
        // Nach Abschluss von vorne beginnen.
        if (_state.value.status == PlayerState.Status.COMPLETED) mp.seekTo(0)
        mp.start()
        _state.update { it.copy(status = PlayerState.Status.PLAYING) }
        startTicker()
    }

    override fun pause() {
        val mp = player ?: return
        if (!mp.isPlaying) return
        mp.pause()
        tickJob?.cancel()
        _state.update { it.copy(status = PlayerState.Status.PAUSED, positionMs = mp.currentPosition.toLong()) }
    }

    override fun seekTo(positionMs: Long) {
        val mp = player ?: return
        mp.seekTo(positionMs.toInt())
        _state.update { it.copy(positionMs = positionMs) }
    }

    override fun release() {
        tickJob?.cancel()
        tickJob = null
        player?.release()
        player = null
        _state.value = PlayerState()
    }

    private fun startTicker() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (isActive) {
                player?.let { mp ->
                    if (mp.isPlaying) {
                        _state.update { it.copy(positionMs = mp.currentPosition.toLong()) }
                    }
                }
                delay(TICK_MS)
            }
        }
    }

    companion object {
        private const val TICK_MS = 200L
    }
}
