package com.uyscuti.social.call.receivers

import android.Manifest
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.uyscuti.social.call.di.RESULT_KEY
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MyReceiver : BroadcastReceiver() {

    @Inject
    @Named("chat_notification_manager_compat")
    lateinit var notificationManager: NotificationManagerCompat
    @Inject
    @Named("chat_notification_compat_builder")
    lateinit var notificationBuilder: NotificationCompat.Builder

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    override fun onReceive(context: Context?, intent: Intent?) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        if (remoteInput != null) {
            val input = remoteInput.getCharSequence(RESULT_KEY).toString()
            val person = Person.Builder().setName("Me").build()
            val message = NotificationCompat.MessagingStyle.Message(
                input, System.currentTimeMillis(), person
            )
            val notificationStyle = NotificationCompat.MessagingStyle(person).addMessage(message)
            notificationManager.notify(
                1,
                notificationBuilder
                    .setStyle(notificationStyle)
                    .setContentTitle("Sent!")
                    .setStyle(null)
                    .build()
            )
        }
    }
}