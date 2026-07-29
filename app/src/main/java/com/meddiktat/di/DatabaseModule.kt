package com.meddiktat.di

import android.content.Context
import androidx.room.Room
import com.meddiktat.data.local.AppDatabase
import com.meddiktat.data.local.DictationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Stellt Room-Datenbank und DAO bereit. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            // Erweiterungshinweis: hier später SupportFactory (SQLCipher) für
            // verschlüsselte Metadaten einhängen.
            .build()

    @Provides
    fun provideDictationDao(db: AppDatabase): DictationDao = db.dictationDao()
}
