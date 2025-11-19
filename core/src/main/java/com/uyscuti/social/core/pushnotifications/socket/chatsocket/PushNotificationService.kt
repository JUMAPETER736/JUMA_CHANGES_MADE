package com.uyscuti.social.core.pushnotifications.socket.chatsocket


import android.Manifest
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.uyscuti.social.core.local.utils.CoreStorage
import com.uyscuti.social.core.local.utils.FileType
import com.uyscuti.social.network.api.models.Message
import com.uyscuti.social.notifications.R
import com.uyscuti.social.notifications.di.RESULT_KEY
import com.uyscuti.social.notifications.receiver.MyReceiver

import java.util.Locale


class PushNotificationService : IntentService("NotificationService") {
    private val CHANNEL_ID = "FlashChatNotifications"
    private val TAG = "PushNotificationService"


    var localStorage: CoreStorage? = null

    override fun onCreate() {
        super.onCreate()
        localStorage = CoreStorage.getInstance(applicationContext)
    }

    override fun onHandleIntent(intent: Intent?) {

        Log.d("PushNotificationService", "onHandleIntent")
        if (intent != null) {
            val message = intent.getSerializableExtra("message") as Message

            val chatId = message.chat
            val text = message.content
            val sender = message.sender.username
            val attachments = message.attachments
            var notificationContent = ""

            if (!attachments.isNullOrEmpty()) {
                for (attachment in attachments) {
                    when (getFileType(attachment.url)) {
                        FileType.IMAGE -> {
                            notificationContent = "📷 Image"
                        }

                        FileType.AUDIO -> {
                            notificationContent = "🎵 Audio"
                        }

                        FileType.VIDEO -> {
                            notificationContent = "🎬 Video"
                        }

                        FileType.DOCUMENT -> {
                            notificationContent = "📄 Document"
                        }

                        FileType.OTHER -> {
                            // Handle other types, if needed
                            notificationContent = "📎 Attachment"
                        }
                    }
                }
            } else {
                notificationContent = text
            }
            handleNotifications(chatId, notificationContent, sender)

//            createNotificationChannel()
//            showNotification(message)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Chat Notifications"
            val description = "Push Notifications for Chat"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getFileType(url: String): FileType {

        return when (url.substringAfterLast(".").toLowerCase(Locale.ROOT)) {
            "jpg", "jpeg", "png", "gif" -> FileType.IMAGE
            "mp3", "wav", "ogg", "m4a" -> FileType.AUDIO
            "mp4", "avi", "mkv" -> FileType.VIDEO
            "pdf", "doc", "docx", "txt" -> FileType.DOCUMENT
            else -> FileType.OTHER
        }
    }

    private fun handleNotifications(chatId: String, message: String, user: String) {
        localStorage?.setChatId(chatId)

        val currentTimeMillis =
            System.currentTimeMillis() // Get the current system time in milliseconds

        Log.d(TAG, "ChatId from notification :$chatId")
        val flag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE
            } else
                0
        val remoteInput = RemoteInput.Builder(RESULT_KEY)
            .setLabel("Reply")
            .build()
        val replyIntent = Intent(this, MyReceiver::class.java)
        replyIntent.putExtra("chatId", chatId)
        val replyPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            replyIntent,
            flag
        )
        val replyAction = NotificationCompat.Action.Builder(
            0,
            "Reply",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

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
//        notificationManage().notify(
//            5858, notificationBuilder()
//                .setContentTitle(user)
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setAutoCancel(true)
//                .setWhen(currentTimeMillis)
////            .addAction(replyAction)
//                .build())

        // Replace `5858` with a unique ID for your foreground service
        val foregroundNotificationId = 5858


        // Build the notification as before
        val notification = notificationBuilder()
            .setContentTitle(user)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setWhen(currentTimeMillis)
//            .addAction(replyAction)
            .build()


        // Start the service as a foreground service with the notification
        startForeground(foregroundNotificationId, notification)
    }

    private fun notificationBuilder(): NotificationCompat.Builder {
        val flag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE
            } else
                0
        val remoteInput = RemoteInput.Builder(RESULT_KEY)
            .setLabel("Type here...")
            .build()
        val replyIntent = Intent(this, MyReceiver::class.java)

        val replyPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            replyIntent,
            flag
        )
        val replyAction = NotificationCompat.Action.Builder(
            0,
            "Reply",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()


        return NotificationCompat.Builder(this, "Chat_Notification")
            .setSmallIcon(com.uyscuti.social.notifications.R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setStyle(notificationStyle)
            .addAction(replyAction)
    }

    private fun notificationManage(): NotificationManagerCompat {
        val notificationManager = NotificationManagerCompat.from(this)
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

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(message: Message) {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(message.sender.username)
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
