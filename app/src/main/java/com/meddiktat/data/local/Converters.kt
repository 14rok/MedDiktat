package com.meddiktat.data.local

import androidx.room.TypeConverter
import com.meddiktat.domain.model.DictationPriority
import com.meddiktat.domain.model.DictationStatus
import com.meddiktat.domain.model.ExportState

/**
 * Room-TypeConverter für die Enums. Speicherung als Name-String hält das Schema
 * robust gegenüber Umsortierungen der Enum-Konstanten.
 */
class Converters {

    @TypeConverter
    fun statusToString(value: DictationStatus): String = value.name

    @TypeConverter
    fun stringToStatus(value: String): DictationStatus = DictationStatus.valueOf(value)

    @TypeConverter
    fun priorityToString(value: DictationPriority?): String? = value?.name

    @TypeConverter
    fun stringToPriority(value: String?): DictationPriority? =
        value?.let { DictationPriority.valueOf(it) }

    @TypeConverter
    fun exportStateToString(value: ExportState): String = value.name

    @TypeConverter
    fun stringToExportState(value: String): ExportState = ExportState.valueOf(value)
}
