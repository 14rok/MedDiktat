package com.meddiktat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room-Datenbank für Metadaten. Enthält bewusst KEINE Audiodaten – die liegen
 * als Dateien im Sandbox-Speicher; hier stehen nur Pfade/Metadaten.
 *
 * Erweiterungshinweis: Für spätere Verschlüsselung kann diese Datenbank mit
 * SQLCipher (SupportFactory) geöffnet werden, ohne DAO/Repository zu ändern.
 */
@Database(
    entities = [DictationEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dictationDao(): DictationDao

    companion object {
        const val NAME = "meddiktat.db"
    }
}
