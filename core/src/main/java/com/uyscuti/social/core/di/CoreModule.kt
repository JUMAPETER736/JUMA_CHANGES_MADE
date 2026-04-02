package com.uyscuti.social.core.di

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.uyscuti.social.core.common.data.room.repository.DialogRepository
import com.uyscuti.social.core.local.utils.SharedStorage
import com.uyscuti.social.core.pushnotifications.PushNotificationHandler
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.CoreChatSocketClient
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Singleton
    @Provides
    fun providePushNotificationHandler(
        localStorage: SharedStorage,
        retrofitInstance: RetrofitInstance,
        dialogRepository: DialogRepository,
        context: Context,
        @Named("chat_notification_manager_compat") chatNotificationManager: NotificationManagerCompat,
        @Named("chat_notification_compat_builder") chatNotificationBuilder: NotificationCompat.Builder,
        coreChatSocketClient: CoreChatSocketClient
    ): PushNotificationHandler {
        return PushNotificationHandler(localStorage, context,dialogRepository,chatNotificationManager, chatNotificationBuilder,coreChatSocketClient,retrofitInstance)
    }
}
