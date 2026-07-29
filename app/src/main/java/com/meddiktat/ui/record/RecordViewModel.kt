package com.meddiktat.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meddiktat.data.storage.DictationFileManager
import com.meddiktat.domain.model.Dictation
import com.meddiktat.domain.recorder.AudioRecorder
import com.meddiktat.domain.recorder.RecorderState
import com.meddiktat.domain.repository.DictationRepository
import com.meddiktat.ui.util.formatDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

/** Steuert die Aufnahme und speichert das fertige Diktat als Metadatensatz. */
@HiltViewModel
class RecordViewModel @Inject constructor(
    private val recorder: AudioRecorder,
    private val fileManager: DictationFileManager,
    private val repository: DictationRepository,
) : ViewModel() {

    val recorderState: StateFlow<RecorderState> = recorder.state

    /** Wird nach erfolgreichem Speichern gesetzt -> Signal für Navigation zurück. */
    private val _savedDictationId = MutableStateFlow<String?>(null)
    val savedDictationId = _savedDictationId.asStateFlow()

    /** Einmalige Nutzerhinweise (z. B. "Aufnahme zu kurz"). */
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private var currentFile: File? = null

    fun start() {
        val file = fileManager.createNewFile()
        currentFile = file
        recorder.start(file)
    }

    fun pause() = recorder.pause()

    fun resume() = recorder.resume()

    fun cancel() {
        recorder.cancel()
        currentFile = null
    }

    fun stopAndSave() {
        val result = recorder.stop()
        currentFile = null
        if (result == null) {
            _message.value = "Aufnahme war zu kurz und wurde verworfen."
            return
        }
        val now = System.currentTimeMillis()
        val dictation = Dictation(
            id = UUID.randomUUID().toString(),
            createdAt = now,
            updatedAt = now,
            filename = result.file.name,
            displayTitle = "Diktat vom ${formatDateTime(now)}",
            audioPath = result.file.absolutePath,
            durationMs = result.durationMs,
            recordingDate = now,
        )
        viewModelScope.launch {
            repository.upsert(dictation)
            _savedDictationId.value = dictation.id
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Verlässt der Nutzer den Screen während einer laufenden Aufnahme,
        // wird diese verworfen (kein stiller Teil-Datensatz).
        if (recorder.state.value.status != RecorderState.Status.IDLE) {
            recorder.cancel()
        }
    }
}
