package com.uyscuti.social.circuit.di


import com.uyscuti.social.core.common.data.room.dao.RecentUserDao
import com.uyscuti.social.core.common.data.room.dao.UserDao
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.repository.RecentUserRepository
import com.uyscuti.social.core.common.data.room.repository.UsersRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object UsersModule {

    @Provides
    fun provideUsersDao(chatDatabase: ChatDatabase): UserDao {
        return chatDatabase.userDao()
    }

    @Provides
    fun provideUsersRepository(userDao: UserDao): UsersRepository {
        return UsersRepository(userDao)
    }

    @Provides
    fun provideRecentUserDao(chatDatabase: ChatDatabase): RecentUserDao {
        return chatDatabase.recentUserDao()
    }

    @Provides
    fun provideRecentUserRepository(recentUserDao: RecentUserDao): RecentUserRepository{
        return RecentUserRepository(recentUserDao)
    }
}