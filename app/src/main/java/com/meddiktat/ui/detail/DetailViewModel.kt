package com.meddiktat.ui.detail

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meddiktat.data.storage.DictationFileManager
import com.meddiktat.domain.model.Dictation
import com.meddiktat.domain.model.DictationPriority
import com.meddiktat.domain.model.DictationStatus
import com.meddiktat.domain.model.ExportState
import com.meddiktat.domain.playback.AudioPlayer
import com.meddiktat.domain.playback.PlayerState
import com.meddiktat.domain.repository.DictationRepository
import com.meddiktat.export.ExportManager
import com.meddiktat.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Detailsteuerung: Wiedergabe, Metadaten-Bearbeitung, Status, Löschen und Export.
 * Die Diktat-ID kommt über die Navigationsroute in den SavedStateHandle.
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DictationRepository,
    private val player: AudioPlayer,
    private val fileManager: DictationFileManager,
    private val exportManager: ExportManager,
) : ViewModel() {

    private val dictationId: String = checkNotNull(savedStateHandle[Routes.ARG_DICTATION_ID])

    val dictation: StateFlow<Dictation?> = repository.observeDictation(dictationId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val playerState: StateFlow<PlayerState> = player.state

    private val _deleted = MutableStateFlow(false)
    val deleted = _deleted.asStateFlow()

    private var preparedPath: String? = null

    // --- Wiedergabe -------------------------------------------------------

    fun togglePlayback() {
        val current = dictation.value ?: return
        when (player.state.value.status) {
            PlayerState.Status.PLAYING -> player.pause()
            PlayerState.Status.IDLE -> {
                preparePlayer(current)
                player.play()
            }
            else -> player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        if (player.state.value.status == PlayerState.Status.IDLE) {
            dictation.value?.let { preparePlayer(it) }
        }
        player.seekTo(positionMs)
    }

    private fun preparePlayer(d: Dictation) {
        if (preparedPath == d.audioPath &&
            player.state.value.status != PlayerState.Status.IDLE
        ) {
            return
        }
        player.prepare(File(d.audioPath))
        preparedPath = d.audioPath
    }

    // --- Metadaten --------------------------------------------------------

    fun saveEdits(
        title: String,
        note: String?,
        caseReference: String?,
        dictationType: String?,
        priority: DictationPriority?,
    ) {
        val current = dictation.value ?: return
        viewModelScope.launch {
            repository.upsert(
                current.copy(
                    displayTitle = title.ifBlank { current.displayTitle },
                    note = note?.ifBlank { null },
                    caseReference = caseReference?.ifBlank { null },
                    dictationType = dictationType?.ifBlank { null },
                    priority = priority,
                ),
            )
        }
    }

    fun setStatus(status: DictationStatus) {
        viewModelScope.launch { repository.updateStatus(dictationId, status) }
    }

    // --- Löschen ----------------------------------------------------------

    fun delete() {
        val current = dictation.value ?: return
        player.release()
        viewModelScope.launch {
            repository.delete(current)
            fileManager.delete(current.filename)
            _deleted.value = true
        }
    }

    // --- Export -----------------------------------------------------------

    /** Baut den Teilen-Intent. Aufrufreihenfolge in der UI: Warnhinweis -> Intent. */
    fun buildShareIntent(): Intent? = dictation.value?.let { exportManager.buildShareIntent(it) }

    /** Nach ausgelöstem Export den Zustand dokumentieren. */
    fun markExported() {
        val current = dictation.value ?: return
        viewModelScope.launch {
            repository.upsert(current.copy(exportState = ExportState.SHARED_LOCALLY))
            if (current.status == DictationStatus.NEW || current.status == DictationStatus.REVIEWED) {
                repository.updateStatus(dictationId, DictationStatus.EXPORTED)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
