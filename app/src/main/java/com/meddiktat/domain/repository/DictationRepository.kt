package com.meddiktat.domain.repository

import com.meddiktat.domain.model.Dictation
import com.meddiktat.domain.model.DictationStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository-Abstraktion (Repository Pattern). Die UI/ViewModels kennen nur dieses
 * Interface, nicht Room. Das erlaubt späteren Austausch/Erweiterung (z. B.
 * verschlüsselte DB, zusätzlicher Remote-Layer) ohne Änderung der Fachlogik.
 */
interface DictationRepository {

    /** Reaktive, nach Aufnahmedatum absteigend sortierte Liste aller Diktate. */
    fun observeDictations(): Flow<List<Dictation>>

    fun observeDictation(id: String): Flow<Dictation?>

    suspend fun getById(id: String): Dictation?

    suspend fun upsert(dictation: Dictation)

    suspend fun delete(dictation: Dictation)

    suspend fun updateStatus(id: String, status: DictationStatus)
}
