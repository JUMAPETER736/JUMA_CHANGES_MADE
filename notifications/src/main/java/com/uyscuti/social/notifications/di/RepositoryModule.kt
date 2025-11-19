package com.uyscuti.social.notifications.di

import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.notifications.reply.ReplyMessageRepository
import com.uyscuti.social.notifications.reply.ReplyMessageRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideReplyMessageRepository(
        retrofitInstance: RetrofitInstance
    ): ReplyMessageRepository {
        return ReplyMessageRepositoryImpl(retrofitInstance)
    }
}

