package com.uyscuti.social.network.chatsocket

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.uyscuti.social.network.api.models.Avatar
import com.uyscuti.social.network.api.models.Message
import com.uyscuti.social.network.api.models.User
import com.uyscuti.social.network.chatsocket.ChatNotificationServiceActions.*
import com.uyscuti.social.network.R
import java.util.Date

class ChatNotificationService : Service() {
    private val CHANNEL_ID = "FlashChatNotifications"

    override fun onBind(p0: Intent?): IBinder? = null


    @RequiresApi(Build.VERSION_CODES.ECLAIR)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { cm ->
            when (cm.action) {
                ON_ONE_ON_ONE_MESSAGE.name -> showOne()
                ON_GROUP_MESSAGE.name -> showGroup()
                else -> {}
            }
        }

        return START_STICKY
    }

    private fun showOne() {
        val avatar = Avatar(
            _id = "1",
            localPath = "local",
            url = "url"
        )

        val lastseen = Date()

        val sender = User(
            _id = "1",
            avatar = avatar,
            email = "email",
            isEmailVerified = true,
            role = "USER",
            username = "username",
            lastseen = lastseen
        )



        val message = Message(
            _id = "1",
            sender = sender,
            content = "message",
            chat = "1",
            attachments = null,
            createdAt = "createdAt",
            updatedAt = "updatedAt",
        )
        createNotificationChannel()
        showNotification(message)
    }

    private fun showGroup() {

    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Chat Notifications Channel"
            val description = "Push Notifications for Chat"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(message: Message) {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_message_24)
            .setContentTitle("New Message")
            .setContentText("${message.sender.username}: ${message.content}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true) // Automatically remove the notification when clicked

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(message._id.hashCode(), notificationBuilder.build())
    }

}