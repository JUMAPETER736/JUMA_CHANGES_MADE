package com.uyscuti.social.core.di

import android.content.Context
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.CoreChatSocketClient
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatSocketModule {

    @Provides
    @Named("Default")
    @Singleton
    fun provideChatSocketClient(localStorage: LocalStorage, retrofitInstance: RetrofitInstance, @ApplicationContext context: Context): CoreChatSocketClient {
        return CoreChatSocketClient(localStorage,retrofitInstance,context)
    }
}
