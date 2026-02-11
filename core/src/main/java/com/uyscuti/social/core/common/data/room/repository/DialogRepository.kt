package com.uyscuti.social.core.common.data.room.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.uyscuti.social.core.common.data.room.dao.DialogDao
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.core.local.utils.FileType
import com.uyscuti.social.network.api.models.Message
import com.uyscuti.social.network.api.models.User
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.CancellationException


sealed class DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>()
    data class Failure(val message: String) : DataResult<Nothing>()
}
class DialogRepository(private val dialogDao: DialogDao, retrofitInstance: RetrofitInstance, private val localStorage: LocalStorage) {

    // Room executes database operations on a background thread by default.
    // Use LiveData to automatically notify observers when the data changes.
    private val apiService = retrofitInstance.apiService

    private var userId = localStorage.getUserId()
    private var username = localStorage.getUsername()


    val allDialogs: LiveData<List<DialogEntity>> = dialogDao.getDialogs()

    val allPersonal: LiveData<List<DialogEntity>> = dialogDao.getPersonalDialogs()

    val allGroupDialogs: LiveData<List<DialogEntity>> = dialogDao.getGroupDialogs()

    val allUnreadDialogs: LiveData<Int> = dialogDao.getLiveUnreadDialogsCount()

    private val chatIdList = ArrayList<String>()

    var filteredUsers: List<UserEntity> = emptyList()


    fun getTempDialogs(): LiveData<List<DialogEntity>>{
        return dialogDao.getTempDialogs()
    }

    fun getDialogsFlow(): Flow<List<DialogEntity>>{
        return dialogDao.getDialogsFlow()
    }

    suspend fun updateDialogEntityId(oldDialogId: String, newChatId: String){
        dialogDao.updateDialogEntityId(oldDialogId, newChatId)
    }

    suspend fun deleteDialogs(ids: List<String>){
        try {
            dialogDao.deleteDialogsByIds(ids)
        } catch (e:Exception){
            e.printStackTrace()
            Log.e("deleteDialogs", "deleteDialogs: ${e.message}")
        }
    }

    fun insertDialog(dialog: DialogEntity) {
        try {
            dialogDao.insertDialog(dialog)
        } catch (e: Exception){
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    private suspend fun insertDialogs(dialogs: List<DialogEntity>) {
        try {
            dialogDao.insertAllDialogs(dialogs)
        } catch (e: Exception){
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    private suspend fun insertNewDialogs(dialogs: List<DialogEntity>) {
        try {
            dialogDao.insertNewDialogs(dialogs)
        } catch (e: Exception){
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    suspend fun getDialogList() : List<DialogEntity>{
        return dialogDao.getDialogList()
    }

    fun getDialogByID(dialogId: String): LiveData<DialogEntity?> {
        return dialogDao.getDialogById(dialogId)
    }

    fun getDialogByName(name: String): LiveData<DialogEntity?>{
        return dialogDao.getDialogByName(name)
    }

    suspend fun getDialog(dialogId: String): DialogEntity {
        return dialogDao.getDialog(dialogId)
    }

    suspend fun checkDialog(dialogId: String): DialogEntity?{
        return dialogDao.checkDialog(dialogId)
    }

    suspend fun checkDialogByName(dialogId: String): DialogEntity?{
        return dialogDao.checkDialogByName(dialogId)
    }


    // Modify the function to return a Flow<DialogEntity?>
    fun getDialogByIDFlow(dialogId: String): Flow<DialogEntity?> {
        return dialogDao.getDialogByIdFlow(dialogId)
    }

    fun updateDialog(dialog: DialogEntity) {
        try {
            dialogDao.updateDialog(dialog)
        } catch (e: Exception){
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    fun observeThisDialog(name: String): LiveData<DialogEntity>{
        return dialogDao.observeDialogByName(name)
    }

    fun incrementUnreadCount(dialogId: String) {
        dialogDao.updateUnreadCount(dialogId)
    }

    suspend fun replaceDialog(old: DialogEntity, new: DialogEntity){
        dialogDao.replaceDialog(old,new)
    }

    fun updateLastMessage(dialog: DialogEntity, newLastMessage: MessageEntity) {
        dialog.lastMessage = newLastMessage
        dialog.unreadCount += 1
        dialogDao.updateLastMessage(dialog)
    }

    suspend fun updateLastMessageForThisChat(dialogId: String, newLastMessage: MessageEntity) {
        val dialog = dialogDao.getDialog(dialogId)
        dialog.lastMessage = newLastMessage
        dialog.unreadCount = 0
        dialogDao.updateLastMessage(dialog)
    }

    suspend fun setLastMessageNull(dialogId: String){
        val dialog = dialogDao.getDialog(dialogId)
        dialog.lastMessage = null
        dialog.unreadCount = 0
        dialogDao.updateDialog(dialog)
    }

    fun resetUnreadCount(dialogId: String): LiveData<DialogEntity?> {
        // Get the dialog by its ID from the database
        return  dialogDao.getDialogById(dialogId)
    }

    fun clearAll(){
        dialogDao.deleteAll()
    }


    private fun User.toUserEntity(): UserEntity {
        return UserEntity(
            id = _id,
            name = username,
            avatar = avatar.url, // Assuming avatar.imageUrl is the string representation of the avatar
            lastSeen = Date(),
            online = false // Set online status as needed
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

                            text += "📄 Document"

                        }

                        FileType.OTHER -> {
                            // Handle other types, if needed
                        }
                    }
                }
            }
        }else{
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
            userName =sender.username,
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


    suspend fun fetchAndInsertPersonalDialogs() {
        try {
            var offset = 0
            val batchSize = 20
            var allDialogsFetched = false
            val fetchedDialogIds = mutableSetOf<String>() // Keep track of fetched dialog IDs

            while (!allDialogsFetched) {
                val response = apiService.getChats(offset, batchSize)

                if (response.isSuccessful) {
                    val chatsResponse = response.body()

                    chatsResponse?.let {
                        val chatList = chatsResponse.data

                        val dialogs = chatList.filter { !it.isGroupChat }.mapNotNull { chat ->
                            // Check if the dialog ID is already fetched
                            if (!fetchedDialogIds.contains(chat._id)) {
                                chatIdList.add(chat._id)
                                fetchedDialogIds.add(chat._id)

                                val users = chat.participants.map { it.toUserEntity() }


                                val firstUser = users.first { it.id != userId }

                                var chatName = ""
                                var chatAvatar = ""

                                var text = ""
                                var senderName = ""

                                filteredUsers = listOf(firstUser) as List<UserEntity>

                                chatName = firstUser.name ?: ""
                                chatAvatar = firstUser.avatar ?: ""

                                // Perform the conversion for each Chat to DialogEntity
                                DialogEntity(
                                    id = chat._id,
                                    dialogPhoto = chatAvatar,
                                    dialogName = chatName,
                                    users = filteredUsers,
                                    lastMessage = chat.lastMessage?.toMessageEntity(),
                                    unreadCount = 0 // Set appropriate initial unread count
                                )
                            } else {
                                allDialogsFetched = true
                                null // Dialog already fetched, skip it
                            }
                        }
                        // dialogDao.insertAllDialogs(dialogs)
                        dialogs.filter { it.dialogName == username }
                        val filteredDialogs = dialogs.filter { it.lastMessage != null }
                        val newDialogs = dialogs.filter { it.lastMessage == null }

                        insertDialogs(filteredDialogs)
                        insertNewDialogs(newDialogs)

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
}
