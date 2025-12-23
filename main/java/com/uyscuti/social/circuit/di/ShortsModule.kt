package com.uyscuti.social.circuit.di


import com.uyscuti.social.core.common.data.room.dao.ShortsDao
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.repository.ShortsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ShortsModule {

    @Provides
    fun provideUsersDao(chatDatabase: ChatDatabase): ShortsDao {
        return chatDatabase.shortsDao()
    }

    @Provides
    fun provideUsersRepository(shortsDao: ShortsDao): ShortsRepository {
        return ShortsRepository(shortsDao)
    }

}