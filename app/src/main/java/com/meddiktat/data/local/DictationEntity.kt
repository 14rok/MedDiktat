package com.meddiktat.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meddiktat.domain.model.Dictation
import com.meddiktat.domain.model.DictationPriority
import com.meddiktat.domain.model.DictationStatus
import com.meddiktat.domain.model.ExportState

/**
 * Room-Persistenzmodell. Getrennt vom Domänenmodell [Dictation], damit sich
 * Datenbank-Schema und Fachmodell unabhängig entwickeln können. Enums werden
 * über [Converters] als Text gespeichert (schema-stabil und gut lesbar).
 */
@Entity(tableName = "dictations")
data class DictationEntity(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val updatedAt: Long,
    val filename: String,
    val displayTitle: String,
    val audioPath: String,
    val durationMs: Long,
    val recordingDate: Long,
    val status: DictationStatus,
    val priority: DictationPriority?,
    val dictationType: String?,
    val caseReference: String?,
    val note: String?,
    val transcript: String?,
    val exportState: ExportState,
)

/** Mapping Entity -> Domänenmodell. */
fun DictationEntity.toDomain(): Dictation = Dictation(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    filename = filename,
    displayTitle = displayTitle,
    audioPath = audioPath,
    durationMs = durationMs,
    recordingDate = recordingDate,
    status = status,
    priority = priority,
    dictationType = dictationType,
    caseReference = caseReference,
    note = note,
    transcript = transcript,
    exportState = exportState,
)

/** Mapping Domänenmodell -> Entity. */
fun Dictation.toEntity(): DictationEntity = DictationEntity(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    filename = filename,
    displayTitle = displayTitle,
    audioPath = audioPath,
    durationMs = durationMs,
    recordingDate = recordingDate,
    status = status,
    priority = priority,
    dictationType = dictationType,
    caseReference = caseReference,
    note = note,
    transcript = transcript,
    exportState = exportState,
)
