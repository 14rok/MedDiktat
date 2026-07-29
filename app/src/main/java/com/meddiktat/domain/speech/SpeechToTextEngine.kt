package com.meddiktat.domain.speech

import java.io.File

/**
 * Schnittstelle für spätere Offline-Transkription. Im MVP existiert nur ein
 * Stub (siehe data/speech/NoopSpeechToTextEngine). Ein reales Modul (z. B.
 * Vosk/Whisper-on-device) kann diese Schnittstelle implementieren, ohne dass
 * UI oder Repository angepasst werden müssen.
 */
interface SpeechToTextEngine {

    /** Ob eine echte Transkriptions-Engine verfügbar ist (Feature-Flag). */
    val isAvailable: Boolean

    /** Transkribiert die Audiodatei; standardmäßig deutschsprachig. */
    suspend fun transcribe(file: File, languageTag: String = "de-DE"): TranscriptionResult
}

sealed interface TranscriptionResult {
    data class Success(val text: String, val confidence: Float? = null) : TranscriptionResult
    data class Error(val message: String) : TranscriptionResult
    data object Unavailable : TranscriptionResult
}
