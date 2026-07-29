package com.meddiktat.domain.model

/**
 * Getrennt vom Workflow-[DictationStatus]: dokumentiert rein technisch, ob und
 * wie ein Diktat das Gerät bereits verlassen hat. Vorbereitung für späteren
 * gesicherten Server-Upload (Zustand UPLOADED).
 */
enum class ExportState {
    NOT_EXPORTED,
    SHARED_LOCALLY,
    UPLOADED,
}
