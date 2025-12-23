package com.uyscuti.social.circuit.di

import com.uyscuti.social.core.common.data.room.dao.FollowListDao
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.repository.ShortsFollowListRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object ShortsFollowModule {

    @Provides
    fun provideUsersDao(chatDatabase: ChatDatabase): FollowListDao {
        return chatDatabase.shortsEntityFollowListDao()
    }

    @Provides
    fun provideUsersRepository(followDao: FollowListDao): ShortsFollowListRepository {
        return ShortsFollowListRepository(followDao)
    }
}