package com.uyscuti.social.circuit.di

import android.content.Context
import androidx.room.Room
import com.uyscuti.social.core.common.data.room.dao.DialogDao
import com.uyscuti.social.core.common.data.room.dao.GroupDialogDao
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.repository.DialogRepository
import com.uyscuti.social.core.common.data.room.repository.GroupDialogRepository
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DialogModule {

    @Provides
    fun provideChatDatabase(context: Context): ChatDatabase {
        return ChatDatabase.getInstance(context)
    }


    @Provides
    fun provideDialogDao(chatDatabase: ChatDatabase): DialogDao {
        return chatDatabase.dialogDao()
    }

    @Provides
    fun provideDialogRepository(dialogDao: DialogDao, retrofitInstance: RetrofitInstance, localStorage: LocalStorage): DialogRepository {
        return DialogRepository(dialogDao, retrofitInstance, localStorage)
    }

    @Provides
    fun provideGroupDialogDao(chatDatabase: ChatDatabase): GroupDialogDao {
        return chatDatabase.groupDialogDao()
    }

    @Provides
    fun provideGroupDialogRepository(groupDialogDao: GroupDialogDao, retrofitInstance: RetrofitInstance, localStorage: LocalStorage): GroupDialogRepository {
        return GroupDialogRepository(groupDialogDao, retrofitInstance, localStorage)
    }
}
