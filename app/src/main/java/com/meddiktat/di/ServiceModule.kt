package com.meddiktat.di

import com.meddiktat.data.speech.NoopSpeechToTextEngine
import com.meddiktat.data.upload.NoopUploadService
import com.meddiktat.domain.speech.SpeechToTextEngine
import com.meddiktat.domain.upload.UploadService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Bindet die Zukunfts-Schnittstellen an ihre MVP-Stubs. Für echte Funktionalität
 * hier lediglich die konkrete Klasse austauschen – der Rest der App bleibt unberührt.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindUploadService(impl: NoopUploadService): UploadService

    @Binds
    @Singleton
    abstract fun bindSpeechToTextEngine(impl: NoopSpeechToTextEngine): SpeechToTextEngine
}
