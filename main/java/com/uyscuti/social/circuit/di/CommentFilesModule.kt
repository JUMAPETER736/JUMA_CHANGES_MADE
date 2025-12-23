package com.uyscuti.social.circuit.di


import com.uyscuti.social.core.common.data.room.dao.CommentFilesDao
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.repository.CommentFilesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object CommentFilesModule {

    @Provides
    fun provideCommentFilesDao(chatDatabase: ChatDatabase): CommentFilesDao {
        return chatDatabase.shortCommentFilesDao()
    }

    @Provides
    fun provideCommentFilesRepository(commentFileDao: CommentFilesDao): CommentFilesRepository {
        return CommentFilesRepository(commentFileDao)
    }
}