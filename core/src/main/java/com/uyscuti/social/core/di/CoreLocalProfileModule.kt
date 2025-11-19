package com.uyscuti.social.core.di

import android.content.Context
import com.uyscuti.social.core.local.utils.LocalProfile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreLocalProfileModule {

    @Provides
    @Singleton
    fun provideLocalProfile(@ApplicationContext context: Context): LocalProfile {
        return LocalProfile(context)
    }
}