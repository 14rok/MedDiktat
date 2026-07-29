package com.meddiktat.di

import com.meddiktat.domain.playback.AudioPlayer
import com.meddiktat.domain.recorder.AudioRecorder
import com.meddiktat.playback.MediaPlayerAudioPlayer
import com.meddiktat.recorder.MediaRecorderAudioRecorder
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Bindet die Audio-Abstraktionen an ihre Android-Implementierungen. */
@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {

    @Binds
    @Singleton
    abstract fun bindAudioRecorder(impl: MediaRecorderAudioRecorder): AudioRecorder

    @Binds
    @Singleton
    abstract fun bindAudioPlayer(impl: MediaPlayerAudioPlayer): AudioPlayer
}
