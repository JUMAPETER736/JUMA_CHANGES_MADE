package com.uyscuti.social.core.pushnotifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.core.common.data.room.repository.DialogRepository
import com.uyscuti.social.core.common.data.room.repository.MessageRepository
import com.uyscuti.social.core.local.utils.FileType
import com.uyscuti.social.core.local.utils.SharedStorage
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.CoreChatSocketClient
import com.uyscuti.social.network.api.models.Message
import com.uyscuti.social.network.api.models.Notification
import com.uyscuti.social.network.api.models.User
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.notifications.di.RESULT_KEY
import com.uyscuti.social.notifications.receiver.MyReceiver


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random


@Singleton
class PushNotificationHandler @Inject constructor(
    private val localStorage: SharedStorage,
    private val context: Context,
    private var dialogRepository: DialogRepository,
    private var chatNotificationManager: NotificationManagerCompat,
    private var chatNotificationBuilder: NotificationCompat.Builder,
    private var coreChatSocketClient: CoreChatSocketClient,
    private var retrofitInstance: RetrofitInstance

) : CoreChatSocketClient.ChatSocketEvents {

    private val TAG = "PushNotificationHandler"


//    private var dialogRepository: DialogRepository = DialogRepository(ChatDatabase.getInstance(context).dialogDao(),retrofitInstance, localStorage)


    private var messageRepository: MessageRepository =
        MessageRepository(context, ChatDatabase.getInstance(context).messageDao(), retrofitInstance)


    init {
//        coreChatSocketClient.chatListener = this
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun handleNotifications(chatId: String, message: String, user: String) {
        localStorage.setChatId(chatId)


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
        val replyIntent = Intent(context, MyReceiver::class.java)
        replyIntent.putExtra("chatId", chatId)
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

        if (ActivityCompat.checkSelfPermission(
                context,
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
        chatNotificationManager.notify(
            5858, chatNotificationBuilder
                .setContentTitle(user)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setWhen(currentTimeMillis)
//            .addAction(replyAction)
                .build()
        )
    }

    override fun onSocketConnect() {
        Log.d(TAG, "onSocketConnect: In PushNotificationHandler")
    }

    private fun getFileType(url: String): FileType {

        return when (url.substringAfterLast(".").toLowerCase()) {
            "jpg", "jpeg", "png", "gif" -> FileType.IMAGE
            "mp3", "wav", "ogg", "m4a" -> FileType.AUDIO
            "mp4", "avi", "mkv" -> FileType.VIDEO
            "pdf", "doc", "docx", "txt" -> FileType.DOCUMENT
            else -> FileType.OTHER
        }
    }

    override fun onNewMessage(message: Message) {

        Log.d(TAG, "onSocketConnect: New Message In PushNotificationHandler")

        val chatId = message.chat
        val text = message.content
        val sender = message.sender.username
//        handleNotifications(chatId, text, sender)
        updateDB(message)
    }

    override fun onDeliveryReport() {
        Log.d("Message", "Delivered")
    }

    override fun onNotification(notification: Notification) {

    }

    override fun onMessageOpenedReport() {
        Log.d("Message", "Opened")
    }

    private fun convertIso8601ToUnixTimestamp(iso8601Date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")

        val date = sdf.parse(iso8601Date)
        return date?.time ?: 0
    }


    private fun com.uyscuti.social.network.api.models.Message.toMessageEntity(): MessageEntity {

        val createdAt = convertIso8601ToUnixTimestamp(createdAt)


        // Initialize URLs as null
        var imageUrl: String? = null
        var audioUrl: String? = null
        var videoUrl: String? = null
        var docUrl: String? = null
        var size: Long = 0

        // Handle attachments and assign URLs
        if (attachments != null && attachments?.isNotEmpty() == true) {
            val attachments = attachments
            if (attachments != null) {
                for (attachment in attachments) {
                    when (getFileType(attachment.url)) {
                        FileType.IMAGE -> {
                            imageUrl = attachment.url
//                            size = getFileSize(attachment.url)

                            Log.d(
                                "Received Attachment ",
                                "Image To Save, Path Of Image Received: $imageUrl"
                            )
                        }

                        FileType.AUDIO -> {
                            audioUrl = attachment.url
                            Log.d(
                                "Received Attachment",
                                "Audio To Save, Path Of Audio Received: $audioUrl"
                            )

                        }

                        FileType.VIDEO -> {
                            videoUrl = attachment.url
                            Log.d(
                                "Received Attachment",
                                "Video To Save, Path Of Video Received: $videoUrl"
                            )

                        }

                        FileType.DOCUMENT -> {
                            docUrl = attachment.url
                            Log.d(
                                "Received Attachment",
                                "Document To Save, Path Of Document Received: $docUrl"
                            )

                        }

                        FileType.OTHER -> {
                            // Handle other types, if needed
                        }
                    }
                }
            }
        }


        return MessageEntity(
            id = _id,
            chatId = chat,
            userName = sender.username,
            user = sender.toUserEntity(),
            text = content,
            createdAt = createdAt,
            imageUrl = imageUrl,
            voiceUrl = null,
            voiceDuration = 0,
            userId = sender._id,
            status = "Received",
            videoUrl = videoUrl,
            audioUrl = audioUrl,
            docUrl = docUrl,
            fileSize = size
        )
    }


    private fun updateDB(message: com.uyscuti.social.network.api.models.Message) {
        CoroutineScope(Dispatchers.IO).launch {

            delay(50)
            val dialogToUpdate = message.chat

            val notId = Random.nextInt()

            handleNotifications(message.chat, message.content, message.sender.username)

            val messageEnt: MessageEntity = message.toMessageEntity()
            insertMessage(messageEnt)

            // Initialize URLs as null
            var imageUrl: String? = null
            var audioUrl: String? = null
            var videoUrl: String? = null
            var docUrl: String? = null

            var text = ""
            var senderName = ""

            val dialog = dialogRepository.getDialog(dialogToUpdate)

            Log.d(TAG, "dialog to update: $dialog")

            senderName = message.sender.username

            if (dialog.users.size > 1) {
                text = if (senderName.isNotEmpty()) {
                    "<font color='#FF5722'>$senderName:</font> " // Highlighted sender's name
                } else {
                    "Anonymous:"
                }
            }


            // Handle attachments and assign URLs
            if (message.attachments != null && message.attachments?.isNotEmpty() == true) {
                val attachments = message.attachments
                if (attachments != null) {
                    for (attachment in attachments) {
                        when (getFileType(attachment.url)) {
                            FileType.IMAGE -> {
                                imageUrl = attachment.url
                                Log.d(TAG, "Image, Path Of Image Received: $imageUrl")
                                text = "📷 Image"
                            }

                            FileType.AUDIO -> {
                                audioUrl = attachment.url
                                Log.d(TAG, "Audio, Path Of Image Received: $audioUrl")
//                                audioList.add(audioUrl)
                                text = "🎵 Audio"
                            }

                            FileType.VIDEO -> {
                                videoUrl = attachment.url
                                Log.d(TAG, "Video, Path Of Image Received: $videoUrl")
                                text = "🎬 Video"
                            }

                            FileType.DOCUMENT -> {
                                docUrl = attachment.url
                                Log.d(TAG, "Document, Path Of Image Received: $docUrl")
                                text = "📄 Document"
                            }

                            FileType.OTHER -> {
                                // Handle other types, if needed
                            }
                        }
                    }
                }
            }

            val createdAt = convertIso8601ToUnixTimestamp(message.createdAt)

            val messageEntity = MessageEntity(
                id = message._id,
                chatId = dialogToUpdate,
                userId = message.sender._id,
                user = message.sender.toUserEntity(),
                text = text,
                createdAt = createdAt,
                imageUrl = imageUrl,
                voiceUrl = null,
                voiceDuration = 0,
                userName = message.sender.username,
                status = "Received",
                videoUrl = videoUrl,
                audioUrl = audioUrl,
                docUrl = docUrl,
                fileSize = 0,
            )
//            dialogRepository.incrementUnreadCount(dialogToUpdate)
            dialogRepository.updateLastMessage(dialog, messageEntity)
        }
    }

    private fun User.toUserEntity(): UserEntity {
        return UserEntity(
            id = _id,
            name = username,
            avatar = avatar.url, // Assuming avatar.imageUrl is the string representation of the avatar
            online = false,// Set online status as needed
            lastSeen = lastseen
        )
    }


    private fun insertMessage(message: MessageEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            messageRepository.insertMessage(message)
        }
    }
}
