package com.meddiktat.data.repository

import com.meddiktat.data.local.DictationDao
import com.meddiktat.data.local.toDomain
import com.meddiktat.data.local.toEntity
import com.meddiktat.domain.model.Dictation
import com.meddiktat.domain.model.DictationStatus
import com.meddiktat.domain.repository.DictationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-basierte Umsetzung des [DictationRepository]. Übersetzt zwischen Entity und
 * Domänenmodell und kapselt so die Persistenz vollständig vor der Fachlogik.
 */
@Singleton
class DictationRepositoryImpl @Inject constructor(
    private val dao: DictationDao,
) : DictationRepository {

    private val clock: () -> Long = { System.currentTimeMillis() }

    override fun observeDictations(): Flow<List<Dictation>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeDictation(id: String): Flow<Dictation?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): Dictation? = dao.getById(id)?.toDomain()

    override suspend fun upsert(dictation: Dictation) {
        dao.upsert(dictation.copy(updatedAt = clock()).toEntity())
    }

    override suspend fun delete(dictation: Dictation) {
        dao.delete(dictation.toEntity())
    }

    override suspend fun updateStatus(id: String, status: DictationStatus) {
        dao.updateStatus(id = id, status = status.name, updatedAt = clock())
    }
}
