package com.uyscuti.social.core.pushnotifications.socket.chatsocket

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.uyscuti.social.core.local.utils.CoreStorage
import com.uyscuti.social.core.local.utils.FileType
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.ChatNotificationServiceActions.*
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.social.FlashNotificationEvent

import com.uyscuti.social.network.api.models.Message
import com.uyscuti.social.notifications.R
import com.uyscuti.social.notifications.di.RESULT_KEY
import com.uyscuti.social.notifications.receiver.MyReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.util.Locale
import androidx.core.content.edit

class ChatNotificationService : Service() {
    private val CHANNEL_ID = "FlashChatNotifications"

    private val activeNotifications = mutableMapOf<String, MutableList<Message>>()

    // Replace `5858` with a unique ID for your foreground service
    private val foregroundNotificationId = 5858

    private val handler = Handler(Looper.getMainLooper())




    private var isForegroundStarted = false

    override fun onBind(p0: Intent?): IBinder? = null

    val TAG = "ChatNotificationService"

    var localStorage: CoreStorage? = null
    override fun onCreate() {
        super.onCreate()
        localStorage = CoreStorage.getInstance(applicationContext)
        createNotificationChannel()
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(!isForegroundStarted) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                stopSelf()
                return START_NOT_STICKY // Can't show notification, permissions need to be requested from Activity
            }

            startForeground(foregroundNotificationId, createMinimalServiceNotification())
            isForegroundStarted = true

            handler.postDelayed({
                stopForeground(true)
                stopSelf()
            }, 30 * 1000)
        }

        intent?.let { cm ->
            val message = cm.getSerializableExtra("message") as Message

            when (cm.action) {
                ON_ONE_ON_ONE_MESSAGE.name -> showNotification(message)
                ON_GROUP_MESSAGE.name -> showGroup()
                else -> {}
            }
        }

        return START_NOT_STICKY
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
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
       // showNotification(message)
    }

    private fun showGroup() {

    }


    @SuppressLint("ObsoleteSdkInt")
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            return // Can't show notification, permissions need to be requested from Activity
        }
        val currentTimeMillis = System.currentTimeMillis()

        val attachments = message.attachments

        val chatId = message.chat

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
            notificationContent = message.content
        }

        val groupKey = "messages_$chatId" // Group by chat/conversation
        val notificationId = message._id.hashCode()

        // Add to active notifications tracking
        activeNotifications.getOrPut(groupKey) { mutableListOf() }.add(message)

        val replyAction = getNotificationAction(chatId)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(message.sender.username)
            .setContentText(notificationContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(groupKey)
            .setWhen(currentTimeMillis)
            .addAction(replyAction)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true) // Automatically remove the notification when clicked
            .setOngoing(false) // Allows swiping to dismiss

        // This is safe - won't interfere with foreground notification
        NotificationManagerCompat.from(this).notify(notificationId, notificationBuilder.build())

        // Create/update group summary if multiple notifications
        activeNotifications[groupKey]?.size?.let {
            if ((it) > 1) {
                showGroupSummary(groupKey)
            }
        }
    }

    private fun createMinimalServiceNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Circuit")
            .setContentText("Waiting for new messages")
            .setSmallIcon(com.uyscuti.social.core.R.drawable.ic_notification) // Use a very small, subtle icon
            .setPriority(NotificationCompat.PRIORITY_MIN) // Even lower than LOW
            .setOngoing(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setDefaults(0)
            .setSilent(true)
            .build()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showGroupSummary(groupKey: String) {
        val messages = activeNotifications[groupKey] ?: return
        val groupId = groupKey.hashCode()

        val inboxStyle = NotificationCompat.InboxStyle()

        // Add individual lines
        messages.takeLast(2).forEach { message ->
            inboxStyle.addLine("${message.sender.username}: ${message.content}")
        }

        // Add summary if needed
        if (messages.size > 2) {
            inboxStyle.setSummaryText("+ ${messages.size - 2} more messages")
        }


        val summaryNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_message)
            .setContentTitle("${messages.size} new messages")
            .setContentText("From ${messages.map { it.sender.username }.distinct().joinToString(", ")}")
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setStyle(inboxStyle)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(groupId, summaryNotification)
    }

    private fun getNotificationAction(chatId: String):  NotificationCompat.Action {
        localStorage?.setChatId(chatId)

        val flag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE
            } else
                PendingIntent.FLAG_UPDATE_CURRENT

        val remoteInput = RemoteInput.Builder(RESULT_KEY)
            .setLabel("Reply")
            .build()

        val replyIntent = Intent(this, MyReceiver::class.java)
        replyIntent.putExtra("chatId", chatId)


        val replyPendingIntent = PendingIntent.getBroadcast(
            this,
            chatId.hashCode(),
            replyIntent,
            flag
        )

        val replyAction = NotificationCompat.Action.Builder(
            0,
            "Reply",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        return replyAction
    }

    private fun handleNotifications(chatId: String, message: String, user: String) {
        localStorage?.setChatId(chatId)

        val currentTimeMillis =
            System.currentTimeMillis() // Get the current system time in milliseconds

//        Log.d(TAG, "ChatId from notification :$chatId")
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




        // Build the notification as before
        val notification = notificationBuilder(chatId)
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

    private fun notificationBuilder(chatId: String): NotificationCompat.Builder {
        val flag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE
            } else
                0

        val remoteInput = RemoteInput.Builder(RESULT_KEY)
            .setLabel("Type here...")
            .build()
        val replyIntent = Intent(this, ChatReceiver::class.java)
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


        return NotificationCompat.Builder(applicationContext, "Chat_Notification")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setStyle(notificationStyle)
            .addAction(replyAction)
    }

    private fun incrementChatNotificationCount() {
        val prefs = getSharedPreferences("notifications", Context.MODE_PRIVATE)
        val currentCount = prefs.getInt("notification_count", 0)
        val newCount = currentCount + 1
        prefs.edit { putInt("notification_count", newCount) }
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses
        return runningProcesses?.any {
            it.processName == packageName &&
                    it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        } == true
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
            Intent(this, ChatNotificationService::class.java)
        )
    }

    override fun onDestroy() {
       // serviceScope.cancel()
        super.onDestroy()
    }


    companion object {
        private var instance: ChatNotificationService? = null

        fun getInstance(context: Context): ChatNotificationService? {
            if (instance == null) {
                instance = (context.applicationContext as? ContextWrapper)?.baseContext?.getSystemService() as? ChatNotificationService
            }
            return instance
        }

        fun stopService(context: Context) {
            instance?.stopSelf()
        }
    }

}