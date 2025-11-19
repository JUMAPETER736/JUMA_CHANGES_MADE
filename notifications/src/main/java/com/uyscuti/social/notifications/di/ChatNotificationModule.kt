package com.uyscuti.social.notifications.di

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.uyscuti.social.notifications.R
import com.uyscuti.social.notifications.receiver.MyReceiver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

const val RESULT_KEY = "RESULT_KEY"

@Module
@InstallIn(SingletonComponent::class)
object ChatNotificationModule {
    @Singleton
    @Provides
    @Named("chat_notification_compat_builder")
    fun provideNotificationBuilder(
        @ApplicationContext context: Context
    ): NotificationCompat.Builder {
        val flag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE
            } else
                0
        val remoteInput = RemoteInput.Builder(RESULT_KEY)
            .setLabel("Type here...")
            .build()
        val replyIntent = Intent(context, MyReceiver::class.java)

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            replyIntent,
            flag
        )
        val replyAction = NotificationCompat.Action.Builder(
            0,
            "Reply",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()


        return NotificationCompat.Builder(context, "Chat_Notification")
            .setSmallIcon(R.drawable.ic_message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setStyle(notificationStyle)
            .addAction(replyAction)
    }

    @Singleton
    @Provides
    @Named("chat_notification_manager_compat")
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManagerCompat {
        val notificationManager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "Chat_Notification",
                "Chat_Notification_Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        return notificationManager
    }

}