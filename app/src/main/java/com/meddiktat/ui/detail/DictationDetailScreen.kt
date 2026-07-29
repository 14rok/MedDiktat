package com.meddiktat.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meddiktat.domain.model.Dictation
import com.meddiktat.domain.model.DictationPriority
import com.meddiktat.domain.model.DictationStatus
import com.meddiktat.domain.playback.PlayerState
import com.meddiktat.ui.components.label
import com.meddiktat.ui.util.formatDateTime
import com.meddiktat.ui.util.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictationDetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val dictation by viewModel.dictation.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()

    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showExportWarning by remember { mutableStateOf(false) }

    LaunchedEffectOnDelete(deleted, onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        dictation?.displayTitle ?: "Diktat",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { showEdit = true }, enabled = dictation != null) {
                        Icon(Icons.Filled.Edit, contentDescription = "Bearbeiten")
                    }
                    IconButton(onClick = { showExportWarning = true }, enabled = dictation != null) {
                        Icon(Icons.Filled.Share, contentDescription = "Exportieren")
                    }
                    IconButton(onClick = { showDelete = true }, enabled = dictation != null) {
                        Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                    }
                },
            )
        },
    ) { padding ->
        val current = dictation
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                PlaybackSection(
                    state = playerState,
                    totalDurationMs = current.durationMs,
                    onTogglePlayback = viewModel::togglePlayback,
                    onSeek = viewModel::seekTo,
                )
                StatusSelector(
                    current = current.status,
                    onSelect = viewModel::setStatus,
                )
                MetadataSection(dictation = current)
                TranscriptSection(transcript = current.transcript)
            }
        }
    }

    if (showEdit && dictation != null) {
        EditDialog(
            dictation = dictation!!,
            onDismiss = { showEdit = false },
            onSave = { title, note, caseRef, type, priority ->
                viewModel.saveEdits(title, note, caseRef, type, priority)
                showEdit = false
            },
        )
    }

    if (showDelete) {
        DeleteConfirmDialog(
            onConfirm = { showDelete = false; viewModel.delete() },
            onDismiss = { showDelete = false },
        )
    }

    if (showExportWarning) {
        ExportWarningDialog(
            onConfirm = {
                showExportWarning = false
                viewModel.buildShareIntent()?.let { intent ->
                    context.startActivity(intent)
                    viewModel.markExported()
                }
            },
            onDismiss = { showExportWarning = false },
        )
    }
}

@Composable
private fun LaunchedEffectOnDelete(deleted: Boolean, onBack: () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(deleted) { if (deleted) onBack() }
}

// --- Wiedergabe -----------------------------------------------------------

@Composable
private fun PlaybackSection(
    state: PlayerState,
    totalDurationMs: Long,
    onTogglePlayback: () -> Unit,
    onSeek: (Long) -> Unit,
) {
    // Die vorbereitete Dauer kann 0 sein, solange nicht abgespielt wurde ->
    // dann auf die gespeicherte Diktatdauer zurückfallen.
    val duration = if (state.durationMs > 0) state.durationMs else totalDurationMs
    val position = state.positionMs.coerceIn(0L, duration.coerceAtLeast(0L))
    val isPlaying = state.status == PlayerState.Status.PLAYING

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Slider(
                value = position.toFloat(),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(position), style = MaterialTheme.typography.labelMedium)
                Text(formatDuration(duration), style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                FilledIconButton(onClick = onTogglePlayback, modifier = Modifier.size(72.dp)) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Abspielen",
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
        }
    }
}

// --- Status ---------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusSelector(current: DictationStatus, onSelect: (DictationStatus) -> Unit) {
    Column {
        Text("Status", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DictationStatus.entries.forEach { status ->
                FilterChip(
                    selected = status == current,
                    onClick = { onSelect(status) },
                    label = { Text(status.label()) },
                )
            }
        }
    }
}

// --- Metadaten ------------------------------------------------------------

@Composable
private fun MetadataSection(dictation: Dictation) {
    Column {
        Text("Details", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        MetaRow("Aufnahme", formatDateTime(dictation.recordingDate))
        MetaRow("Dauer", formatDuration(dictation.durationMs))
        dictation.priority?.let { MetaRow("Priorität", it.label()) }
        dictation.dictationType?.let { MetaRow("Art", it) }
        dictation.caseReference?.let { MetaRow("Fallkürzel", it) }
        dictation.note?.let { MetaRow("Notiz", it) }
        HorizontalDivider(Modifier.padding(top = 8.dp))
        MetaRow("Datei", dictation.filename)
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TranscriptSection(transcript: String?) {
    Column {
        Text("Transkript", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            text = transcript?.takeIf { it.isNotBlank() }
                ?: "Noch keine Transkription. Offline-Spracherkennung ist vorbereitet, aber im MVP nicht aktiv.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// --- Dialoge --------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditDialog(
    dictation: Dictation,
    onDismiss: () -> Unit,
    onSave: (String, String?, String?, String?, DictationPriority?) -> Unit,
) {
    var title by remember(dictation.id) { mutableStateOf(dictation.displayTitle) }
    var caseRef by remember(dictation.id) { mutableStateOf(dictation.caseReference.orEmpty()) }
    var type by remember(dictation.id) { mutableStateOf(dictation.dictationType.orEmpty()) }
    var note by remember(dictation.id) { mutableStateOf(dictation.note.orEmpty()) }
    var priority by remember(dictation.id) { mutableStateOf(dictation.priority) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(title, note, caseRef, type, priority) }) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
        title = { Text("Diktat bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = caseRef,
                    onValueChange = { caseRef = it },
                    label = { Text("Fallkürzel (neutral, optional)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Art (optional)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notiz (optional)") },
                )
                Text("Priorität", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = priority == null,
                        onClick = { priority = null },
                        label = { Text("Keine") },
                    )
                    DictationPriority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.label()) },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun DeleteConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onConfirm) { Text("Löschen") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
        title = { Text("Diktat löschen?") },
        text = { Text("Die Audiodatei und alle Metadaten werden unwiderruflich vom Gerät gelöscht.") },
    )
}

@Composable
private fun ExportWarningDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onConfirm) { Text("Trotzdem exportieren") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
        title = { Text("Datenschutzhinweis") },
        text = {
            Text(
                "Sobald das Diktat die App verlässt, liegt es außerhalb des geschützten " +
                    "App-Speichers. Stelle sicher, dass der Empfänger und der Übertragungsweg " +
                    "für medizinische Daten geeignet sind.",
            )
        },
    )
}
