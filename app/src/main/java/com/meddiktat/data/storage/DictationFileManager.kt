package com.meddiktat.data.storage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Zentrale, einzige Stelle für Dateipfade der Audiodiktate.
 *
 * Datenschutz:
 *  - Speicherort ist ausschließlich context.filesDir/dictations (App-Sandbox,
 *    für andere Apps nicht lesbar). Keine öffentlichen Download-/Media-Ordner.
 *  - Dateinamen enthalten niemals Klarnamen: Zeitstempel + zufällige Kurz-ID.
 */
@Singleton
class DictationFileManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val directory: File
        get() = File(context.filesDir, DIR_NAME).apply { if (!exists()) mkdirs() }

    /** Erzeugt einen datenschutzkonformen Dateinamen (ohne Datei anzulegen). */
    fun newFileName(now: Date = Date()): String {
        val timestamp = SimpleDateFormat(TIMESTAMP_PATTERN, Locale.GERMANY).format(now)
        val randomId = UUID.randomUUID().toString().substring(0, 8)
        return "${timestamp}_dictation_$randomId.m4a"
    }

    /** Neue, leere Zieldatei im Sandbox-Ordner. */
    fun createNewFile(): File = File(directory, newFileName())

    fun fileFor(fileName: String): File = File(directory, fileName)

    fun exists(fileName: String): Boolean = fileFor(fileName).exists()

    /** Löscht die Audiodatei zu einem Diktat (idempotent). */
    fun delete(fileName: String): Boolean {
        val file = fileFor(fileName)
        return if (file.exists()) file.delete() else true
    }

    companion object {
        private const val DIR_NAME = "dictations"
        private const val TIMESTAMP_PATTERN = "yyyy-MM-dd_HH-mm-ss"
    }
}
