package com.uyscuti.social.circuit.di


import com.uyscuti.social.core.common.data.room.dao.ShortCommentReplyDao
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.repository.ShortCommentReplyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ShortCommentReply {

    @Provides
    fun provideCommentsReplyDao(chatDatabase: ChatDatabase): ShortCommentReplyDao {
        return chatDatabase.shortCommentReplyDao()
    }

    @Provides
    fun provideCommentsReplyRepository(commentsReplyDao: ShortCommentReplyDao): ShortCommentReplyRepository {
        return ShortCommentReplyRepository(commentsReplyDao)
    }
}