package com.meddiktat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.meddiktat.domain.model.DictationPriority
import com.meddiktat.domain.model.DictationStatus

/** Deutsches Label für den Workflow-Status. */
fun DictationStatus.label(): String = when (this) {
    DictationStatus.NEW -> "Neu"
    DictationStatus.REVIEWED -> "Geprüft"
    DictationStatus.EXPORTED -> "Exportiert"
    DictationStatus.ARCHIVED -> "Archiviert"
}

/** Deutsches Label für die Priorität. */
fun DictationPriority.label(): String = when (this) {
    DictationPriority.LOW -> "Niedrig"
    DictationPriority.NORMAL -> "Normal"
    DictationPriority.HIGH -> "Hoch"
}

@Composable
fun StatusChip(
    status: DictationStatus,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val container: Color = when (status) {
        DictationStatus.NEW -> scheme.primaryContainer
        DictationStatus.REVIEWED -> scheme.secondaryContainer
        DictationStatus.EXPORTED -> scheme.tertiaryContainer
        DictationStatus.ARCHIVED -> scheme.surfaceVariant
    }
    val content: Color = when (status) {
        DictationStatus.NEW -> scheme.onPrimaryContainer
        DictationStatus.REVIEWED -> scheme.onSecondaryContainer
        DictationStatus.EXPORTED -> scheme.onTertiaryContainer
        DictationStatus.ARCHIVED -> scheme.onSurfaceVariant
    }
    Text(
        text = status.label(),
        style = MaterialTheme.typography.labelSmall,
        color = content,
        modifier = modifier
            .background(container, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
