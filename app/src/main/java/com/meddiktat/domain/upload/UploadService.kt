package com.meddiktat.domain.upload

import com.meddiktat.domain.model.Dictation
import java.io.File

/**
 * Schnittstelle für einen späteren, gesicherten Server-Upload. Im MVP existiert
 * nur eine No-op-Implementierung (data/upload/NoopUploadService), die signalisiert,
 * dass kein Backend konfiguriert ist. Eine echte Implementierung würde hier z. B.
 * TLS-Transport, Authentifizierung und serverseitige Verschlüsselung ergänzen –
 * ohne Änderungen an der übrigen Architektur.
 */
interface UploadService {

    /** Ob ein Upload-Ziel konfiguriert und einsatzbereit ist. */
    val isConfigured: Boolean

    suspend fun upload(dictation: Dictation, file: File): UploadResult
}

sealed interface UploadResult {
    data class Success(val remoteId: String) : UploadResult
    data class Failure(val message: String) : UploadResult
    data object NotConfigured : UploadResult
}
