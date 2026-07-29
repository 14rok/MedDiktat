package com.meddiktat.ui.record

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meddiktat.domain.recorder.RecorderState
import com.meddiktat.ui.util.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    onFinished: () -> Unit,
    viewModel: RecordViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val recorderState by viewModel.recorderState.collectAsStateWithLifecycle()
    val savedId by viewModel.savedDictationId.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    // Nach erfolgreichem Speichern zurück zur Liste.
    LaunchedEffect(savedId) { if (savedId != null) onFinished() }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Neues Diktat") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.cancel(); onFinished() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (!hasPermission) {
                PermissionRequest(onRequest = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                })
            } else {
                RecorderControls(
                    state = recorderState,
                    onStart = viewModel::start,
                    onPause = viewModel::pause,
                    onResume = viewModel::resume,
                    onStop = viewModel::stopAndSave,
                    onDiscard = { viewModel.cancel(); onFinished() },
                )
            }
        }
    }
}

@Composable
private fun PermissionRequest(onRequest: () -> Unit) {
    Text(
        text = "Für Aufnahmen wird der Zugriff auf das Mikrofon benötigt.",
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(16.dp))
    Button(onClick = onRequest) { Text("Mikrofonzugriff erlauben") }
}

@Composable
private fun RecorderControls(
    state: RecorderState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onDiscard: () -> Unit,
) {
    val statusText = when (state.status) {
        RecorderState.Status.IDLE -> "Bereit"
        RecorderState.Status.RECORDING -> "Nimmt auf …"
        RecorderState.Status.PAUSED -> "Pausiert"
    }

    Text(text = statusText, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))
    Text(
        text = formatDuration(state.elapsedMs),
        style = MaterialTheme.typography.displayMedium,
    )
    Spacer(Modifier.height(12.dp))

    // Einfaches Pegel-Feedback während der Aufnahme.
    if (state.status == RecorderState.Status.RECORDING) {
        LinearProgressIndicator(
            progress = { state.amplitude },
            modifier = Modifier.fillMaxWidth(0.7f),
        )
    }

    Spacer(Modifier.height(40.dp))

    when (state.status) {
        RecorderState.Status.IDLE -> {
            FilledIconButton(
                onClick = onStart,
                modifier = Modifier.size(96.dp),
            ) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = "Aufnahme starten",
                    modifier = Modifier.size(40.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("Aufnahme starten", style = MaterialTheme.typography.labelLarge)
        }

        RecorderState.Status.RECORDING, RecorderState.Status.PAUSED -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = onDiscard,
                    modifier = Modifier.size(64.dp),
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Verwerfen")
                }

                FilledIconButton(
                    onClick = onStop,
                    modifier = Modifier.size(96.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Icon(
                        Icons.Filled.Stop,
                        contentDescription = "Stoppen & speichern",
                        modifier = Modifier.size(40.dp),
                    )
                }

                if (state.status == RecorderState.Status.RECORDING) {
                    FilledTonalIconButton(onClick = onPause, modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Filled.Pause, contentDescription = "Pause")
                    }
                } else {
                    FilledTonalIconButton(onClick = onResume, modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Fortsetzen")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Stopp speichert das Diktat", style = MaterialTheme.typography.labelMedium)
        }
    }
}
