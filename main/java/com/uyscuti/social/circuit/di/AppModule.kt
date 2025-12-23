package com.uyscuti.social.circuit.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.uyscuti.social.circuit.MyViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context.applicationContext
    }

    @Provides
    @Singleton
    fun provideViewModelFactory(application: Application): ViewModelProvider.Factory {
        return ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    @Provides
    @Singleton
    fun provideViewModelStoreOwner(): ViewModelStoreOwner {
        return MyViewModelStoreOwner()
    }
}

