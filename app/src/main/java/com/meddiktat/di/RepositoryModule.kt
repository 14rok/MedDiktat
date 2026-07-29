package com.meddiktat.di

import com.meddiktat.data.repository.DictationRepositoryImpl
import com.meddiktat.domain.repository.DictationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Bindet die Repository-Schnittstelle an ihre Room-Implementierung. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDictationRepository(impl: DictationRepositoryImpl): DictationRepository
}
