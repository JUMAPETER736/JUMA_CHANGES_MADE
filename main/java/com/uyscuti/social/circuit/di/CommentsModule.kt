package com.uyscuti.social.circuit.di

import com.uyscuti.social.core.common.data.room.dao.ShortCommentsDao
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.repository.ShortCommentsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object CommentsModule {

    @Provides
    fun provideCommentsDao(chatDatabase: ChatDatabase): ShortCommentsDao {
        return chatDatabase.shortCommentsDao()
    }

    @Provides
    fun provideCommentsRepository(commentsDao: ShortCommentsDao): ShortCommentsRepository {
        return ShortCommentsRepository(commentsDao)
    }
}