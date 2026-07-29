package com.meddiktat.data.upload

import com.meddiktat.domain.model.Dictation
import com.meddiktat.domain.upload.UploadResult
import com.meddiktat.domain.upload.UploadService
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MVP-Platzhalter: Es ist bewusst KEIN Backend angebunden. Signalisiert überall
 * dort, wo später ein Upload möglich wäre, dass aktuell kein Ziel konfiguriert ist.
 *
 * Eine echte Implementierung (TLS, Auth, serverseitige Verschlüsselung) kann diese
 * Klasse ersetzen; die DI-Bindung in ServiceModule ist die einzige Änderungsstelle.
 */
@Singleton
class NoopUploadService @Inject constructor() : UploadService {

    override val isConfigured: Boolean = false

    override suspend fun upload(dictation: Dictation, file: File): UploadResult =
        UploadResult.NotConfigured
}
