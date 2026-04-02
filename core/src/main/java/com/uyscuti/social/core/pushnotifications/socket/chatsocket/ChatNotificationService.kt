package com.uyscuti.social.core.pushnotifications.socket.chatsocket

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.app.TaskStackBuilder
import androidx.core.content.getSystemService
import com.uyscuti.social.core.local.utils.CoreStorage
import com.uyscuti.social.core.local.utils.FileType
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.ChatNotificationServiceActions.*
import com.uyscuti.social.network.api.models.Message
import com.uyscuti.social.notifications.R
import com.uyscuti.social.notifications.di.RESULT_KEY
import com.uyscuti.social.notifications.receiver.MyReceiver
import java.util.Locale
import com.uyscuti.social.core.models.data.Dialog
import com.uyscuti.social.core.models.data.User
import java.util.Date

class ChatNotificationService : Service() {
    private val CHANNEL_ID = "FlashChatNotifications"

    private val activeNotifications = mutableMapOf<String, MutableList<Message>>()

    // Replace `5858` with a unique ID for your foreground service
    private val foregroundNotificationId = 5858

    private val handler = Handler(Looper.getMainLooper())

    private val OPEN_MESSAGE_ACTIVITY = "com.uyscuti.social.circuit.OPEN_MESSAGE_Activity"

    private val OPEN_MAIN_ACTIVITY = "com.uyscuti.social.circuit.OPEN_MAIN_Activity"

    private var isForegroundStarted = false

    private var participants: ArrayList<User> = arrayListOf()

    override fun onBind(p0: Intent?): IBinder? = null

    val TAG = "ChatNotificationService"

    var localStorage: CoreStorage? = null
    override fun onCreate() {
        super.onCreate()

        localStorage = CoreStorage.getInstance(applicationContext)
        createNotificationChannel()

        // 🚨 ALWAYS call startForeground immediately
        startForeground(
            foregroundNotificationId,
            createMinimalServiceNotification()
        )

        isForegroundStarted = true

        // Permission check AFTER foreground start
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }
    }


    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let { cm ->
            val message = cm.getSerializableExtra("message") as Message

            when (cm.action) {
                ON_ONE_ON_ONE_MESSAGE.name -> showNotification(message)
                ON_GROUP_MESSAGE.name -> showGroup(message)
                else -> {}
            }
        }

        return START_NOT_STICKY
    }

    private fun showGroup(message: Message) {
        Log.d(TAG,"Group notification: $message")
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
            notificationContent = message.content.toString()
        }

        val groupKey = "messages_$chatId" // Group by chat/conversation
        val notificationId = message._id.hashCode()

        // Add to active notifications tracking
        activeNotifications.getOrPut(groupKey) { mutableListOf() }.add(message)

        val replyAction = getNotificationAction(chatId, notificationId)
        val openMessagePendingIntent = openMessageActivity(message)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(message.sender.username)
            .setContentText(notificationContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(groupKey)
            .setWhen(currentTimeMillis)
            .addAction(replyAction)
            .setContentIntent(openMessagePendingIntent)
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

    private fun getNotificationAction(chatId: String, notificationId: Int):  NotificationCompat.Action {
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
        replyIntent.putExtra("notificationId", notificationId)

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

    private fun openMessageActivity(message: Message): PendingIntent {

        val user = User(
            message.sender._id,
            message.sender.username,
            message.sender.avatar.url,
            true,
            Date()

        )

        participants.add(user)

        val newMassage = com.uyscuti.social.core.models.data.Message(
            message._id,
            user,
            message.content,
            Date()
        )

        val dialog = Dialog(
            message.chat,
            message.sender.username,
            message.sender.avatar.url,
            participants,
            newMassage,
            0
        )

        val intent = Intent(OPEN_MESSAGE_ACTIVITY).apply {
            putExtra("Dialog_Extra", dialog)
            putExtra("temporally", false)
            putExtra("productReference", "")
            setPackage(packageName)
            // Add flags to control the activity's behavior
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Create a PendingIntent
        val pendingIntent = TaskStackBuilder.create(this).run {
            // Add the main activity as the parent
            val mainIntent = Intent(OPEN_MAIN_ACTIVITY).apply {
                putExtra("fragment","chats")
                putExtra("notification_event", true)
                putExtra("Dialog_Extra", dialog)
                setPackage(packageName)
            }
            addNextIntentWithParentStack(mainIntent)
            // Add the messages activity on top
            addNextIntent(intent)
            // Create the pending intent
            getPendingIntent(
                message._id.hashCode(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        return pendingIntent
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

    override fun onDestroy() {
        // serviceScope.cancel()
        super.onDestroy()
    }


    companion object {
        private var instance: ChatNotificationService? = null

        fun getInstance(context: Context): ChatNotificationService? {
            if (instance == null) {
                instance = (context.applicationContext as? ContextWrapper)?.baseContext?.getSystemService()
            }
            return instance
        }

        fun stopService(context: Context) {
            instance?.stopSelf()
        }
    }

}