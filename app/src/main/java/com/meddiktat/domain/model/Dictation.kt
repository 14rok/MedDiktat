package com.meddiktat.domain.model

/**
 * Reines Domänenmodell eines Diktats – bewusst frei von Room-/Android-Annotationen,
 * damit die Fachlogik unabhängig von der Persistenzschicht bleibt.
 *
 * Datenschutz: Es werden keine Klarnamen o. Ä. erzwungen. [caseReference] ist ein
 * optionales, neutrales Kürzel; der [filename] enthält niemals personenbezogene Daten.
 */
data class Dictation(
    val id: String,
    val createdAt: Long,
    val updatedAt: Long,
    /** Dateiname nach Schema yyyy-MM-dd_HH-mm-ss_dictation_<randomId>.m4a */
    val filename: String,
    val displayTitle: String,
    /** Absoluter Pfad im app-spezifischen Sandbox-Speicher. */
    val audioPath: String,
    val durationMs: Long,
    /** Zeitpunkt der Aufnahme (kann von createdAt abweichen, z. B. bei Import). */
    val recordingDate: Long,
    val status: DictationStatus = DictationStatus.NEW,
    val priority: DictationPriority? = null,
    val dictationType: String? = null,
    val caseReference: String? = null,
    val note: String? = null,
    /** Zunächst leer – Platzhalter für spätere Offline-Transkription. */
    val transcript: String? = null,
    val exportState: ExportState = ExportState.NOT_EXPORTED,
)
