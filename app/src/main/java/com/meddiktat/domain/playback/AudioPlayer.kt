package com.meddiktat.domain.playback

import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Abstraktion der In-App-Wiedergabe. Wiedergabe erfolgt ausschließlich innerhalb
 * der App (kein Intent an externe Player), um Datenabfluss zu vermeiden.
 */
interface AudioPlayer {

    val state: StateFlow<PlayerState>

    /** Lädt und bereitet die Datei vor (ohne automatisch abzuspielen). */
    fun prepare(file: File)

    fun play()

    fun pause()

    fun seekTo(positionMs: Long)

    /** Stoppt Wiedergabe und gibt den MediaPlayer frei. */
    fun release()
}

data class PlayerState(
    val status: Status = Status.IDLE,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
) {
    enum class Status { IDLE, PREPARED, PLAYING, PAUSED, COMPLETED }
}
