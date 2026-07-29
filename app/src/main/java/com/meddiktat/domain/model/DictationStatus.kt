package com.meddiktat.domain.model

/**
 * Workflow-Status eines Diktats. Bewusst als Enum modelliert, damit spätere
 * Workflow-Schritte (z. B. Übergabe an Schreibkraft) sauber ergänzt werden können.
 */
enum class DictationStatus {
    /** Frisch aufgenommen, noch nicht gesichtet. */
    NEW,

    /** Vom Arzt kontrolliert / gegengehört. */
    REVIEWED,

    /** Bewusst exportiert/geteilt. */
    EXPORTED,

    /** Abgeschlossen und archiviert. */
    ARCHIVED,
}
