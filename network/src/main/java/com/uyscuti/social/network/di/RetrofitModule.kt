package com.uyscuti.social.network.di

import android.content.Context
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    fun provideRetrofitInstance(localStorage: LocalStorage, context: Context): RetrofitInstance {
        return RetrofitInstance(localStorage, context)
    }
}
