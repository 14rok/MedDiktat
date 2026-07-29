package com.meddiktat.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import com.meddiktat.domain.recorder.AudioRecorder
import com.meddiktat.domain.recorder.RecorderState
import com.meddiktat.domain.recorder.RecordingResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MediaRecorder-Implementierung der [AudioRecorder]-Abstraktion.
 *
 * Audioparameter sind auf gute Sprachverständlichkeit bei kompakter Dateigröße
 * ausgelegt: MPEG-4-Container (.m4a), AAC-Codec, mono, 44,1 kHz, 96 kbit/s.
 *
 * Die verstrichene Zeit wird über die monotone Uhr (elapsedRealtime) gemessen
 * und Pausen sauber herausgerechnet.
 */
@Singleton
class MediaRecorderAudioRecorder @Inject constructor(
    @ApplicationContext private val context: Context,
) : AudioRecorder {

    private val _state = MutableStateFlow(RecorderState())
    override val state = _state.asStateFlow()

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    private var segmentStartedAt = 0L
    private var accumulatedMs = 0L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickJob: Job? = null

    override fun start(output: File) {
        cancel() // eventuelle Altzustände sicher aufräumen
        val rec = createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            // Kennzeichnet die Aufnahme als datenschutzsensibel (Concurrent-Capture-Schutz).
            // Muss unmittelbar nach setAudioSource (Zustand INITIALIZED) gesetzt werden.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                isPrivacySensitive = true
            }
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioChannels(1)
            setAudioSamplingRate(44_100)
            setAudioEncodingBitRate(96_000)
            setOutputFile(output.absolutePath)
            prepare()
            start()
        }
        recorder = rec
        outputFile = output
        accumulatedMs = 0L
        segmentStartedAt = SystemClock.elapsedRealtime()
        _state.value = RecorderState(status = RecorderState.Status.RECORDING)
        startTicker()
    }

    override fun pause() {
        val rec = recorder ?: return
        if (_state.value.status != RecorderState.Status.RECORDING) return
        rec.pause()
        accumulatedMs += SystemClock.elapsedRealtime() - segmentStartedAt
        tickJob?.cancel()
        _state.update {
            it.copy(status = RecorderState.Status.PAUSED, elapsedMs = accumulatedMs, amplitude = 0f)
        }
    }

    override fun resume() {
        val rec = recorder ?: return
        if (_state.value.status != RecorderState.Status.PAUSED) return
        rec.resume()
        segmentStartedAt = SystemClock.elapsedRealtime()
        _state.update { it.copy(status = RecorderState.Status.RECORDING) }
        startTicker()
    }

    override fun stop(): RecordingResult? {
        val rec = recorder ?: return null
        val file = outputFile ?: return null
        val duration = currentElapsedMs()
        return try {
            rec.stop()
            RecordingResult(file = file, durationMs = duration)
        } catch (e: RuntimeException) {
            // stop() wirft, wenn keine gültigen Audiodaten vorliegen (zu kurz).
            file.delete()
            null
        } finally {
            teardown()
        }
    }

    override fun cancel() {
        recorder?.let { rec ->
            runCatching { rec.stop() }
            rec.release()
        }
        outputFile?.delete()
        teardown()
    }

    override fun release() = cancel()

    private fun teardown() {
        recorder = null
        outputFile = null
        accumulatedMs = 0L
        tickJob?.cancel()
        tickJob = null
        _state.value = RecorderState()
    }

    private fun currentElapsedMs(): Long =
        if (_state.value.status == RecorderState.Status.RECORDING) {
            accumulatedMs + (SystemClock.elapsedRealtime() - segmentStartedAt)
        } else {
            accumulatedMs
        }

    private fun startTicker() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (isActive) {
                val amp = recorder?.let { runCatching { it.maxAmplitude }.getOrDefault(0) } ?: 0
                _state.update {
                    it.copy(
                        elapsedMs = currentElapsedMs(),
                        amplitude = (amp / MAX_AMPLITUDE).coerceIn(0f, 1f),
                    )
                }
                delay(TICK_MS)
            }
        }
    }

    private fun createRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

    companion object {
        private const val TICK_MS = 100L
        private const val MAX_AMPLITUDE = 32_767f
    }
}
