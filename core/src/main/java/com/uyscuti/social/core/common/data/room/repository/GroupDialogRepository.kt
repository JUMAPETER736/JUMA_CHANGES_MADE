package com.uyscuti.social.core.common.data.room.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.uyscuti.social.core.common.data.room.dao.GroupDialogDao
import com.uyscuti.social.core.common.data.room.entity.GroupDialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.core.local.utils.FileType
import com.uyscuti.social.network.api.models.Message
import com.uyscuti.social.network.api.models.User
import com.uyscuti.social.network.api.request.group.GroupMemberUser
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.CancellationException

class GroupDialogRepository(
    private val groupDialogDao: GroupDialogDao,
    retrofitInstance: RetrofitInstance,
    private val localStorage: LocalStorage
) {

    private val apiService = retrofitInstance.apiService

    private var userId = localStorage.getUserId()


    private val chatIdList = ArrayList<String>()

    var filteredUsers: List<UserEntity> = emptyList()


    val allGroupDialogs: LiveData<List<GroupDialogEntity>> = groupDialogDao.getGroupDialogs()

    val allUnreadGroupDialogsCount: LiveData<Int> = groupDialogDao.getLiveUnreadGroupDialogsCount()


    suspend fun insertGroupDialog(dialog: GroupDialogEntity) {
        try {
            groupDialogDao.insertGroupDialog(dialog)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }


    private suspend fun insertDialogs(dialogs: List<GroupDialogEntity>) {
        try {
            groupDialogDao.insertAllGroupDialogs(dialogs)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    suspend fun updateDialog(dialog: GroupDialogEntity) {
        try {
            groupDialogDao.updateDialog(dialog)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    suspend fun incrementUnreadCount(dialogId: String) {
        groupDialogDao.updateUnreadCount(dialogId)
    }

    fun getGroupDialog(dialogId: String): GroupDialogEntity {
        return groupDialogDao.getGroupDialog(dialogId)
    }


    suspend fun updateLastMessage(dialog: GroupDialogEntity, newLastMessage: MessageEntity) {
        dialog.lastMessage = newLastMessage
        dialog.unreadCount += 1
        groupDialogDao.updateLastMessage(dialog)
    }

    suspend fun updateLastMessageForThisChat(chatId: String, message: MessageEntity) {
        val existing = groupDialogDao.checkGroup(chatId) ?: return
        val updated = existing.copy(lastMessage = message)
        groupDialogDao.updateDialogSuspend(updated)
    }


    suspend fun resetUnreadCount(dialogId: String): LiveData<GroupDialogEntity?> {
        // Get the dialog by its ID from the database
        return groupDialogDao.getGroupDialogById(dialogId)
    }

    suspend fun clearAll() {
        groupDialogDao.deleteAll()
    }

    fun getDialog(dialogId: String): GroupDialogEntity {
        return groupDialogDao.getGroupDialog(dialogId)
    }

    fun checkGroup(dialogId: String): GroupDialogEntity?{
        return groupDialogDao.checkGroup(dialogId)
    }

    suspend fun deleteGroups(ids: List<String>){
        try {
            groupDialogDao.deleteGroupDialogsByIds(ids)
        } catch (e:Exception){
            e.printStackTrace()
        }
    }



    private fun User.toUserEntity(): UserEntity {
        return UserEntity(
            id = _id,
            name = username,
            avatar = avatar.url, // Assuming avatar.imageUrl is the string representation of the avatar
            online = false, // Set online status as needed
            lastSeen = Date()
        )
    }

    private fun GroupMemberUser.toUserEntity(): UserEntity = UserEntity(
        id       = _id,
        name     = username ?: fullName ?: "Unknown",
        avatar   = avatar?.url ?: "",
        online   = false,
        lastSeen = Date()
    )

    private fun Message.toMessageEntity(): MessageEntity {
        val createdAt = convertIso8601ToUnixTimestamp(createdAt)

        // Initialize URLs as null
        var imageUrl: String? = null
        var audioUrl: String? = null
        var videoUrl: String? = null
        var docUrl: String? = null

        var text = ""
        var senderName = ""

        senderName = sender.username




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
//                            Log.d(TAG, "Document, Path Of Image Received: $docUrl")
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


    private fun createDefaultMessageEntity(date: String): MessageEntity {
        val createdAt = convertIso8601ToUnixTimestamp(date)
        val lastseen = Date(createdAt)

        val user = UserEntity(
            id = "Flash",
            name = "Flash",
            avatar = "Flash",
            online = true,
            lastSeen = lastseen
        )

        return MessageEntity(
            id = "FirstMessageId",
            chatId = "InitialMessage",
            text = " ",
            userId = "Flash",
            user = user,
            createdAt = createdAt,
            imageUrl = null,
            voiceUrl = null,
            voiceDuration = 0,
            userName = "Flash",
            status = "Received",
            videoUrl = null,
            audioUrl = null,
            docUrl = null,
            fileSize = 0
        )
    }


    private fun convertIso8601ToUnixTimestamp(iso8601Date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")


        val date = sdf.parse(iso8601Date)
        return date?.time ?: 0
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertTimestamp(iso8601Date: String): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val localDateTime = LocalDateTime.parse(iso8601Date, formatter)

        // Assuming the input time is in UTC, you can convert it to the device's time zone
        val zonedDateTime = ZonedDateTime.of(localDateTime, java.time.ZoneId.systemDefault())

        return zonedDateTime.toInstant().toEpochMilli()
    }


    suspend fun fetchAndInsertGroupDialogs() {
        try {
            var offset = 0
            val batchSize = 20
            var allDialogsFetched = false
            val fetchedDialogIds = mutableSetOf<String>() // Keep track of fetched dialog IDs

            while (!allDialogsFetched) {
                val response = apiService.getChats(offset, batchSize)

                if (response.isSuccessful) {
                    val chatsResponse = response.body()

//                    Log.d("FetchedDialogs", chatsResponse.toString())
//                    Log.d("FetchedDialogs", chatsResponse?.data?.size.toString())

                    chatsResponse?.let {
                        val chatList = chatsResponse.data

                        val dialogs = chatList.filter { it.isGroupChat }.mapNotNull { chat ->
                            // Check if the dialog ID is already fetched
                            if (!fetchedDialogIds.contains(chat._id)) {
                                chatIdList.add(chat._id)
                                fetchedDialogIds.add(chat._id)

                                val users = chat.participants.map { it.toUserEntity() }

                                val adminId = chat.admin ?: ""

                                var adminName = ""
                                // Find the admin user based on the adminId
                                val adminUser = users.find { it.id == adminId }

                                // Check if the admin user is found
                                adminName = adminUser?.name ?: "Admin Not Found"

                                var chatName = ""
                                var chatAvatar = ""

                                var text = ""
                                var senderName = ""

                                // Group chat
                                chatName = chat.name
                                filteredUsers = users

//                                    val random = Random()
//                                    chatAvatar = filteredUsers.getOrNull(random.nextInt(filteredUsers.size))?.avatar ?: ""

                                chatAvatar = filteredUsers.firstOrNull()?.avatar ?: ""

                                val createdAt = convertIso8601ToUnixTimestamp(chat.createdAt)
                                val updatedAt = convertIso8601ToUnixTimestamp(chat.updatedAt)

                                val lastMessage = chat.lastMessage?.toMessageEntity() ?: createDefaultMessageEntity(chat.createdAt)


//                                    val avatars = filteredUsers.take(4).map { it.avatar }
//                                    chatAvatar = groupChatImages().toString()

                                // Perform the conversion for each Chat to DialogEntity

                                GroupDialogEntity(
                                    id = chat._id,
                                    adminId = adminId,
                                    adminName = adminName,
                                    dialogPhoto = chatAvatar,
                                    dialogName = chatName,
                                    users = filteredUsers,
                                    lastMessage = lastMessage,
                                    unreadCount = 0 ,
                                    createdAt = createdAt,
                                    updatedAt = updatedAt
                                )

                            } else {
                                allDialogsFetched = true
                                null // Dialog already fetched, skip it
                            }
                        }
                        // dialogDao.insertAllDialogs(dialogs)
                        insertDialogs(dialogs)

                        offset += batchSize

                        if (chatList.size < batchSize) {
                            allDialogsFetched = true
                        }
                    } ?: run {
                        allDialogsFetched = true
                    }
                } else {
                    allDialogsFetched = true
                    // Handle API call failure
                    // Log or propagate the error if necessary
                    DataResult.Failure("Failed to fetch data")
                }
            }
        } catch (e: Exception) {
            // Handle exceptions, e.g., network issues, parsing errors
            // Log or propagate the error if necessary
            Log.d("FetchedDialogs", "Failed to fetch dialogs: ${e.message}")
            e.printStackTrace()
            DataResult.Failure("Network request failed: ${e.message}") // Handle exceptions
        }
    }

    fun dialogIds(): List<String> {
        return this.chatIdList
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


    suspend fun saveGroupDialogFromDetail(detail: GroupChatDetail) {
        try {
            val users = detail.participants.map { it.toUserEntity() }

            val adminName = users.find { it.id == detail.admin }?.name ?: "Admin"

            val chatAvatar = detail.groupAvatar?.url
                ?: users.firstOrNull()?.avatar
                ?: ""

            val createdAt = detail.createdAt?.let { convertIso8601ToUnixTimestamp(it) } ?: 0L
            val updatedAt = detail.updatedAt?.let { convertIso8601ToUnixTimestamp(it) } ?: 0L

            val lastMessage = detail.lastMessage?.let { last ->
                val senderEntity = UserEntity(
                    id       = last.sender?._id ?: "",
                    name     = last.sender?.username ?: "",
                    avatar   = last.sender?.avatar?.url ?: "",
                    online   = false,
                    lastSeen = Date()
                )
                MessageEntity(
                    id            = last._id,
                    chatId        = detail._id,
                    text          = last.content ?: "",
                    userId        = last.sender?._id ?: "",
                    user          = senderEntity,
                    createdAt     = last.createdAt?.let { convertIso8601ToUnixTimestamp(it) } ?: 0L,
                    imageUrl      = null,
                    voiceUrl      = null,
                    voiceDuration = 0,
                    userName      = last.sender?.username ?: "",
                    status        = "Received",
                    videoUrl      = null,
                    audioUrl      = null,
                    docUrl        = null,
                    fileSize      = 0
                )
            } ?: createDefaultMessageEntity(detail.createdAt ?: "1970-01-01T00:00:00.000Z")

            val entity = GroupDialogEntity(
                id          = detail._id,
                adminId     = detail.admin,
                adminName   = adminName,
                dialogPhoto = chatAvatar,
                dialogName  = detail.name,
                users       = users,
                lastMessage = lastMessage,
                unreadCount = 0,
                createdAt   = createdAt,
                updatedAt   = updatedAt,
                description = detail.description ?: ""
            )

            groupDialogDao.upsertGroupDialog(entity)   // REPLACE strategy — safe if row exists
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    suspend fun saveCachedMembers(chatId: String, members: List<GroupMember>) {
        // Convert to JSON string and store in the GroupDialogEntity
        val json = Gson().toJson(members)
        groupDialogDao.updateCachedMembers(chatId, json)
    }

    suspend fun getCachedMembers(chatId: String): List<GroupMember> {
        val json = groupDialogDao.getCachedMembers(chatId) ?: return emptyList()
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<GroupMember>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }



}