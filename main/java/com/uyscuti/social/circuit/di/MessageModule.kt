package com.uyscuti.social.circuit.di

import android.content.Context
import com.uyscuti.social.core.common.data.room.dao.MessageDao
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.repository.MessageRepository
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object MessageModule {

    @Provides
    fun provideMessageDao(chatDatabase: ChatDatabase): MessageDao {
        return chatDatabase.messageDao()
    }

    @Provides
    fun provideMessageRepository(context: Context,dialogDao: MessageDao, retrofitInstance: RetrofitInstance): MessageRepository {
        return MessageRepository(context,dialogDao, retrofitInstance)
    }
}