package com.uyscuti.social.core.common.data.room.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.uyscuti.social.core.common.data.room.dao.MessageDao
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.core.local.utils.FileType
import com.uyscuti.social.network.api.models.Message
import com.uyscuti.social.network.api.models.User
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date


class MessageRepository(
    private val messageDao: MessageDao,
    retrofitInstance: RetrofitInstance
) {

    private val apiService = retrofitInstance.apiService

    suspend fun getMessagesListByChatId(chatId: String): List<MessageEntity> {
        return messageDao.getMessagesListByChatId(chatId)
    }

    suspend fun getMyLastMessageByChatId(chatId: String): MessageEntity? {
        return messageDao.getMyLastMessageByChatId(chatId)
    }


    suspend fun getLastMessagesByChatId(chatId: String): List<MessageEntity> {
        return messageDao.getLatestMessagesListByChatId(chatId)
    }

    suspend fun deleteMessages(ids: List<String>) {
        try {
            messageDao.deleteMessagesByIds(ids)

        } catch (e: Exception) {
            Log.e("Deletion", "Deletion failed : ${e.message}")
            e.printStackTrace()
        }
    }

    fun markMessagesDeleted(ids: List<String>){
        try {
            ids.map { messageId ->
                val message = messageDao.getMessageById(messageId)
                if (message != null){
                    message.deleted = true
                    messageDao.updateMessageStatus(message)
                }
            }
        } catch (e:Exception){
            e.printStackTrace()
        }
    }

    suspend fun deleteMessageByChat(chatId: String) {
        try {
            messageDao.deleteMessagesByChatId(chatId)
        } catch (e: Exception) {
            Log.e("Deletion", "Deletion failed : ${e.message}")
        }
    }

    suspend fun markDeletedByChat(chatId: String) {
        try {
            val pendingMessages = messageDao.getMessagesListByChatId(chatId)


            if (pendingMessages.isNotEmpty()) {
                // Update status to "Delivered"

                val updatedMessages = pendingMessages.map { it.copy(deleted = true) }
                messageDao.updateMessageStatus(updatedMessages)
            }
        } catch (e: Exception) {
            // Handle the exception, log it, or throw a custom exception
            Log.e("UpdateMessage", "Error: ${e.message}")
            e.printStackTrace()
        }
    }
    suspend fun insertMessage(message: MessageEntity) {
        try {
            messageDao.insertMessage(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun User.toUserEntity(): UserEntity {
        return UserEntity(
            id = _id,
            name = username,
            avatar = avatar.url, // Assuming avatar.imageUrl is the string representation of the avatar
            online = false,// Set online status as needed
            lastSeen = Date()
        )
    }

    private fun Message.toMessageEntity(): MessageEntity {
        val createdAt = convertIso8601ToUnixTimestamp(createdAt)

        // Initialize URLs as null
        var imageUrl: String? = null
        var audioUrl: String? = null
        var videoUrl: String? = null
        var docUrl: String? = null

        var text = ""


        // Handle attachments and assign URLs
        if (attachments != null && attachments?.isNotEmpty() == true) {
            val attachments = attachments
            if (attachments != null) {
                for (attachment in attachments) {
                    val fileType = getFileType(attachment.url)
                    when (fileType) {
                        FileType.IMAGE -> {
                            imageUrl = attachment.url

                            text += "📷 Image"
                        }

                        FileType.AUDIO -> {
                            audioUrl = attachment.url

                            text += "🎵 Audio"
                        }

                        FileType.VIDEO -> {
                            videoUrl = attachment.url

                            text += "🎬 Video"

                        }

                        FileType.DOCUMENT -> {
                            docUrl = attachment.url

                            text += "📄 Document"

                        }

                        FileType.OTHER -> {
                            // Handle other types, if needed
                        }
                    }
                }
            }
        } else {
            text += content
        }

        return MessageEntity(
            id = _id,
            chatId = chat,
            text = text,
            userId = sender._id,
            user = sender.toUserEntity(),
            createdAt = createdAt,
            imageUrl = imageUrl,
            voiceUrl = audioUrl,
            voiceDuration = 0,
            userName = sender.username,
            status = "Received",
            videoUrl = videoUrl,
            audioUrl = audioUrl,
            docUrl = docUrl,
            fileSize = 0
        )
    }


    private fun convertIso8601ToUnixTimestamp(iso8601Date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")


        val date = sdf.parse(iso8601Date)
        return date?.time ?: 0
    }

    private suspend fun insertMessages(messages: List<MessageEntity>) {
        try {
            messageDao.insertMessages(messages)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSendingMessages(chatId: String): LiveData<List<MessageEntity>> {
        return messageDao.getSendingMessagesByChatId(chatId)
    }

    fun getTempMessages(name: String): LiveData<List<MessageEntity>> {
        return messageDao.getTempMessagesByChatId(name)
    }

    fun getPendingMessages(chatId: String): List<MessageEntity> {
        return messageDao.getPendingMessagesByChatId(chatId)
    }

    fun updateMessageStatus(message: MessageEntity) {
        message.status = "Sent"
        messageDao.updateMessageStatus(message)
    }

    suspend fun getMyMessages(chatId: String): List<MessageEntity> {
        return messageDao.getMyListMessageByChatId(chatId)
    }

    suspend fun processPendingMessages(chatId: String) {
        try {
            val pendingMessages = messageDao.getPendingMessages(chatId)


            val messagesToUpdate = pendingMessages.filter { it.status != "Seen" }

           if (messagesToUpdate.isNotEmpty()) {
                // Update status to "Delivered"

                val updatedMessages = messagesToUpdate.map { it.copy(status = "Seen") }
                messageDao.updateMessageStatus(updatedMessages)
            }
        } catch (e: Exception) {
            // Handle the exception, log it, or throw a custom exception
            Log.e("UpdateMessage", "Error: ${e.message}")
            e.printStackTrace()
        }
    }


    fun updateMessage(message: MessageEntity) {
        messageDao.updateMessageStatus(message)
    }

    fun getMessageByMessageId(chatId: String): MessageEntity? {
        return messageDao.getMessageById(chatId)
    }

    suspend fun getLastMessage(chatId: String): MessageEntity? {
        return messageDao.getLastMessage(chatId)
    }

    suspend fun getLastMessage(chatId: String, myId: String): MessageEntity? {
        return messageDao.getLastMessage(chatId, myId)
    }

    fun clearAll() {
        messageDao.deleteAll()
    }

    private fun getFileType(url: String): FileType {
        return when (url.substringAfterLast(".").toLowerCase()) {
            "jpg", "jpeg", "png", "gif" -> FileType.IMAGE
            "mp3", "wav", "ogg" -> FileType.AUDIO
            "mp4", "avi", "mkv" -> FileType.VIDEO
            "pdf", "doc", "docx", "txt" -> FileType.DOCUMENT
            else -> FileType.OTHER
        }
    }

    fun getMessagesWithMediaType(chatId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val messageResponse = apiService.getMessages(chatId)

                if (messageResponse.isSuccessful) {
                    val messages = messageResponse.body()?.data

                    if (messages != null) {
                        insertMessages(messages.map { it.toMessageEntity() })
                    }

                }
            } catch (e: Exception) {
                // Handle network or API call exception
                e.printStackTrace()
                Log.e("TAG", "getMessages exception: $e")
            }
        }
    }

}
