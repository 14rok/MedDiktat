package com.meddiktat.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.GERMANY)

/** z. B. "28.07.2026" */
fun formatDate(epochMillis: Long): String = dateFormat.format(Date(epochMillis))

/** z. B. "28.07.2026, 16:24" */
fun formatDateTime(epochMillis: Long): String = dateTimeFormat.format(Date(epochMillis))

/** z. B. "03:47" oder "1:02:15" für Dauer/Position. */
fun formatDuration(millis: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis.coerceAtLeast(0))
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.GERMANY, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.GERMANY, "%02d:%02d", minutes, seconds)
    }
}
