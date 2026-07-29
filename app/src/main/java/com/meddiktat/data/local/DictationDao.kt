package com.meddiktat.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object. Liefert reaktive Streams (Flow), damit die UI automatisch
 * auf Datenänderungen reagiert.
 */
@Dao
interface DictationDao {

    /** Absteigend nach Aufnahmedatum – neueste Diktate zuerst. */
    @Query("SELECT * FROM dictations ORDER BY recordingDate DESC")
    fun observeAll(): Flow<List<DictationEntity>>

    @Query("SELECT * FROM dictations WHERE id = :id")
    fun observeById(id: String): Flow<DictationEntity?>

    @Query("SELECT * FROM dictations WHERE id = :id")
    suspend fun getById(id: String): DictationEntity?

    @Upsert
    suspend fun upsert(entity: DictationEntity)

    @Delete
    suspend fun delete(entity: DictationEntity)

    @Query("UPDATE dictations SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long)
}
