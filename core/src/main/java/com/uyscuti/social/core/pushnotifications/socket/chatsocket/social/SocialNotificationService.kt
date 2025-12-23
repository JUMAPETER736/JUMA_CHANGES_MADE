package com.uyscuti.social.core.pushnotifications.socket.chatsocket.social

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import androidx.core.content.getSystemService
import com.uyscuti.social.core.R
import com.uyscuti.social.core.local.utils.CoreStorage
import com.uyscuti.social.core.local.utils.FileType
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.ChatNotificationServiceActions.ON_GROUP_MESSAGE
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.ChatNotificationServiceActions.ON_ONE_ON_ONE_MESSAGE
import com.uyscuti.social.network.api.models.Message
import com.uyscuti.social.network.api.models.Notification
import org.greenrobot.eventbus.EventBus

import java.util.Locale


class SocialNotificationService : Service() {
    private val CHANNEL_ID = "FlashChatNotifications"
    override fun onBind(p0: Intent?): IBinder? = null

    var localStorage: CoreStorage? = null
    override fun onCreate() {
        super.onCreate()
        localStorage = CoreStorage.getInstance(applicationContext)
    }

    @RequiresApi(Build.VERSION_CODES.ECLAIR)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { cm ->
            val message = cm.getSerializableExtra("notification") as Notification
            when (cm.action) {
                ON_ONE_ON_ONE_MESSAGE.name -> handleNotification(message._id,message.message,message.sender.username,message)
                ON_GROUP_MESSAGE.name -> showGroup()
                else -> {}
            }
        }

        return START_NOT_STICKY
    }

    private fun showOne(message: Message) {
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

//        val avatar = Avatar(
//            _id = "1",
//            localPath = "local",
//            url = "url"
//        )
//
//        val lastseen = Date()

//        val sender = User(
//            _id = "1",
//            avatar = avatar,
//            email = "email",
//            isEmailVerified = true,
//            role = "USER",
//            username = "username",
//            lastseen = lastseen
//        )

//        val message = Message(
//            _id = "1",
//            sender = sender,
//            content = "message",
//            chat = "1",
//            attachments = null,
//            createdAt = "createdAt",
//            updatedAt = "updatedAt",
//        )
//        createNotificationChannel()
//        showNotification(message)
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

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(message: Message) {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(com.uyscuti.social.notifications.R.drawable.ic_launcher_foreground)
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
    private fun handleNotifications(chatId: String, message: String, user: String, ) {
        localStorage?.setChatId(chatId)
        val currentTimeMillis = System.currentTimeMillis()


        // Get the current system time in milliseconds

//        Log.d(TAG, "ChatId from notification :$chatId")
//        val flag =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                PendingIntent.FLAG_MUTABLE
//            } else
//                0
//        val remoteInput = RemoteInput.Builder(RESULT_KEY)
//            .setLabel("Reply")
//            .build()
//        val replyIntent = Intent(this, MyReceiver::class.java)
//        replyIntent.putExtra("chatId", chatId)
//        val replyPendingIntent = PendingIntent.getBroadcast(
//            this,
//            1,
//            replyIntent,
//            flag
//        )
//        val replyAction = NotificationCompat.Action.Builder(
//            0,
//            "Reply",
//            replyPendingIntent
//        ).addRemoteInput(remoteInput).build()

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

        val vibratePattern = longArrayOf(0, 1000, 500, 1000)
        // Build the notification as before
        val notification = notificationBuilder(chatId)
            .setContentTitle(user)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setWhen(currentTimeMillis)
//            .setVibrate(vibratePattern)
//            .addAction(replyAction)
            .build()
// added but event
        Log.d("SocialNotificationService"," notification created")
        EventBus.getDefault().post(FlashNotificationEvent(true))
//        EventBus.getDefault().post(FlashNotificationsEvents())

        // Start the service as a foreground service with the notification
        startForeground(foregroundNotificationId, notification)
        stopForeground(false)
    }


    private fun handleNotification(chatId: String, message: String, user: String, note: Notification) {
        localStorage?.setChatId(chatId)
        val currentTimeMillis = System.currentTimeMillis()


        // Get the current system time in milliseconds

//        Log.d(TAG, "ChatId from notification :$chatId")
//        val flag =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                PendingIntent.FLAG_MUTABLE
//            } else
//                0
//        val remoteInput = RemoteInput.Builder(RESULT_KEY)
//            .setLabel("Reply")
//            .build()
//        val replyIntent = Intent(this, MyReceiver::class.java)
//        replyIntent.putExtra("chatId", chatId)
//        val replyPendingIntent = PendingIntent.getBroadcast(
//            this,
//            1,
//            replyIntent,
//            flag
//        )
//        val replyAction = NotificationCompat.Action.Builder(
//            0,
//            "Reply",
//            replyPendingIntent
//        ).addRemoteInput(remoteInput).build()

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

        val vibratePattern = longArrayOf(0, 1000, 500, 1000)
        // Build the notification as before
        val notification = notificationBuilder(chatId)
            .setContentTitle(user)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setWhen(currentTimeMillis)
//            .setVibrate(vibratePattern)
//            .addAction(replyAction)
            .build()
// added but event
        Log.d("SocialNotificationService"," notification created")
        EventBus.getDefault().post(FlashNotificationEvent(true))

        // Start the service as a foreground service with the notification
        startForeground(foregroundNotificationId, notification)
        stopForeground(false)
    }


    private fun notificationBuilder(chatId: String): NotificationCompat.Builder {
//        val flag =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                PendingIntent.FLAG_MUTABLE
//            } else
//                0
//        val remoteInput = RemoteInput.Builder(RESULT_KEY)
//            .setLabel("Type here...")
//            .build()
//        val replyIntent = Intent(this, ChatReceiver::class.java)
//        replyIntent.putExtra("chatId", chatId)
//
//        val replyPendingIntent = PendingIntent.getBroadcast(
//            this,
//            1,
//            replyIntent,
//            flag
//        )
//        val replyAction = NotificationCompat.Action.Builder(
//            0,
//            "Reply",
//            replyPendingIntent
//        ).addRemoteInput(remoteInput).build()
//

        return NotificationCompat.Builder(this, "Chat_Notification")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
//            .setStyle(notificationStyle)
//            .addAction(replyAction)

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


    fun Context.stopNotificationService() {
        (applicationContext as? ContextWrapper)?.baseContext?.stopService(
            Intent(this, SocialNotificationService::class.java)
        )
    }


    companion object {
        private var instance: SocialNotificationService? = null

        fun getInstance(context: Context): SocialNotificationService? {
            if (instance == null) {
                instance = (context.applicationContext as? ContextWrapper)?.baseContext?.getSystemService() as? SocialNotificationService
            }
            return instance
        }

        fun stopService(context: Context) {
            instance?.stopSelf()
        }
    }

}