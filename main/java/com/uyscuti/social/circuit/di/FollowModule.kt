package com.uyscuti.social.circuit.di

import com.uyscuti.social.core.common.data.room.dao.FollowUnFollowDao
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.repository.FollowUnFollowRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object FollowModule {
    @Provides
    fun provideFollowDao(chatDatabase: ChatDatabase): FollowUnFollowDao {
        return chatDatabase.followUnFollowDao()
    }

    @Provides
    fun provideUsersRepository(followDao: FollowUnFollowDao): FollowUnFollowRepository {
        return FollowUnFollowRepository(followDao)
    }
}