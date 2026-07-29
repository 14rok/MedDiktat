package com.meddiktat.domain.recorder

import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Abstraktion der Audioaufnahme. Die konkrete Implementierung (MediaRecorder)
 * liegt im recorder-Package. So bleibt die ViewModel-Schicht testbar und ein
 * späterer Wechsel (z. B. AudioRecord für PCM/Transkription) ist möglich.
 */
interface AudioRecorder {

    /** Beobachtbarer Aufnahmezustand inkl. laufender Dauer. */
    val state: StateFlow<RecorderState>

    /** Startet eine neue Aufnahme in die angegebene Zieldatei (.m4a/AAC). */
    fun start(output: File)

    /** Pausiert die laufende Aufnahme (API 24+). */
    fun pause()

    /** Setzt eine pausierte Aufnahme fort. */
    fun resume()

    /**
     * Beendet die Aufnahme und liefert das Ergebnis, oder null bei Fehler/keiner
     * aktiven Aufnahme.
     */
    fun stop(): RecordingResult?

    /** Bricht die Aufnahme ab und löscht die (unvollständige) Datei. */
    fun cancel()

    /** Gibt Ressourcen frei (spätestens in onCleared/onStop aufrufen). */
    fun release()
}

/** Momentaufnahme des Recorders für die UI. */
data class RecorderState(
    val status: Status = Status.IDLE,
    val elapsedMs: Long = 0L,
    /** Aktuelle Aussteuerung 0f..1f für ein einfaches Pegel-Feedback. */
    val amplitude: Float = 0f,
) {
    enum class Status { IDLE, RECORDING, PAUSED }
}

/** Ergebnis einer abgeschlossenen Aufnahme. */
data class RecordingResult(
    val file: File,
    val durationMs: Long,
)
