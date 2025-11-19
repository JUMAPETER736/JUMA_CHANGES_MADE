package com.uyscuti.social.core.di

import android.content.Context
import com.uyscuti.social.core.local.utils.CoreStorage
import com.uyscuti.social.core.local.utils.SharedStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SharedStorageModule {
    @Provides
    @Singleton
    fun provideLocalStorage(@ApplicationContext context: Context): SharedStorage {
        return SharedStorage(context)
    }

    @Provides
    @Singleton
    fun provideCoreStorage(@ApplicationContext context: Context): CoreStorage {
        return CoreStorage(context)
    }
}
