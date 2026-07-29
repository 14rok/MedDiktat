package com.meddiktat.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meddiktat.domain.model.Dictation
import com.meddiktat.ui.components.StatusChip
import com.meddiktat.ui.util.formatDate
import com.meddiktat.ui.util.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictationListScreen(
    onRecordClick: () -> Unit,
    onDictationClick: (String) -> Unit,
    viewModel: DictationListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Diktate") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onRecordClick,
                icon = { Icon(Icons.Filled.Mic, contentDescription = null) },
                text = { Text("Neues Diktat") },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.fillMaxSize().padding(padding))
            state.dictations.isEmpty() -> EmptyState(Modifier.fillMaxSize().padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.dictations, key = { it.id }) { dictation ->
                    DictationRow(dictation = dictation, onClick = { onDictationClick(dictation.id) })
                }
            }
        }
    }
}

@Composable
private fun DictationRow(dictation: Dictation, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dictation.displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${formatDate(dictation.recordingDate)} · ${formatDuration(dictation.durationMs)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusChip(status = dictation.status)
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Outlined.GraphicEq,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Noch keine Diktate vorhanden.",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Tippe auf „Neues Diktat“, um deine erste Aufnahme zu starten.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
