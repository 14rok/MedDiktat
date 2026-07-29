package com.meddiktat.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.meddiktat.domain.model.Dictation
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Baut den Teilen-Intent für den kontrollierten Export. Der Export ist immer eine
 * bewusste Nutzeraktion: Dieser Manager erzeugt nur den Intent, das Auslösen
 * erfolgt in der UI – und erst nach Bestätigung des Datenschutz-Warnhinweises.
 *
 * Über den FileProvider wird eine content:// URI mit temporärem, widerrufbarem
 * Lesezugriff erzeugt; der reale Dateipfad im Sandbox verlässt die App nie.
 */
@Singleton
class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun buildShareIntent(dictation: Dictation): Intent {
        val file = File(dictation.audioPath)
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, dictation.displayTitle)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        // Chooser erzwingt eine bewusste Zielauswahl durch die ärztliche Person.
        return Intent.createChooser(sendIntent, "Diktat exportieren")
    }

    companion object {
        private const val MIME_TYPE = "audio/mp4"
    }
}
