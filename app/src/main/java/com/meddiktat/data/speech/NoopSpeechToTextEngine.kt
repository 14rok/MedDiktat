package com.meddiktat.data.speech

import com.meddiktat.domain.speech.SpeechToTextEngine
import com.meddiktat.domain.speech.TranscriptionResult
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MVP-Stub der Transkriptions-Schnittstelle. [isAvailable] = false dient als
 * Feature-Flag: die UI blendet Transkriptionsfunktionen aus, solange keine echte
 * Offline-Engine eingebunden ist.
 */
@Singleton
class NoopSpeechToTextEngine @Inject constructor() : SpeechToTextEngine {

    override val isAvailable: Boolean = false

    override suspend fun transcribe(file: File, languageTag: String): TranscriptionResult =
        TranscriptionResult.Unavailable
}
