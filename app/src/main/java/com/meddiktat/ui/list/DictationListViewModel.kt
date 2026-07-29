package com.meddiktat.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meddiktat.data.storage.DictationFileManager
import com.meddiktat.domain.model.Dictation
import com.meddiktat.domain.repository.DictationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListUiState(
    val dictations: List<Dictation> = emptyList(),
    val isLoading: Boolean = true,
)

/** Liefert die reaktive Diktatliste und kapselt das (datei- + metadaten-)Löschen. */
@HiltViewModel
class DictationListViewModel @Inject constructor(
    private val repository: DictationRepository,
    private val fileManager: DictationFileManager,
) : ViewModel() {

    val uiState: StateFlow<ListUiState> = repository.observeDictations()
        .map { ListUiState(dictations = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListUiState(isLoading = true),
        )

    fun delete(dictation: Dictation) {
        viewModelScope.launch {
            repository.delete(dictation)
            fileManager.delete(dictation.filename)
        }
    }
}
