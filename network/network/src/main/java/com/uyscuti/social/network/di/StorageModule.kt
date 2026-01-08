package com.uyscuti.social.network.di

import android.content.Context
import com.uyscuti.social.network.utils.LocalStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    @Provides
    @Singleton
    fun provideLocalStorage(@ApplicationContext context: Context): LocalStorage {
        return LocalStorage(context)
    }
}
