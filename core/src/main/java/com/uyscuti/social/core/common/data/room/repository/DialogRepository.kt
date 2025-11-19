package com.uyscuti.social.core.common.data.room.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.TimeZone
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


    suspend fun getDialogs(): LiveData<List<DialogEntity>>{
       return dialogDao.getDialogs()
    }

    suspend fun getTempDialogs(): LiveData<List<DialogEntity>>{
        return dialogDao.getTempDialogs()
    }

    suspend fun getDialogsFlow(): Flow<List<DialogEntity>>{
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

    suspend fun insertDialog(dialog: DialogEntity) {
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

    suspend fun getDialogById(dialogId: String){
        dialogDao.getDialogById(dialogId)
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

    suspend fun updateDialog(dialog: DialogEntity) {
        try {
            dialogDao.updateDialog(dialog)
        } catch (e: Exception){
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    suspend fun observeThisDialog(name: String): LiveData<DialogEntity>{
        return dialogDao.observeDialogByName(name)
    }

    suspend fun incrementUnreadCount(dialogId: String) {
        dialogDao.updateUnreadCount(dialogId)
    }

    suspend fun replaceDialog(old: DialogEntity, new: DialogEntity){
        dialogDao.replaceDialog(old,new)
    }

    suspend fun updateLastMessage(dialog: DialogEntity, newLastMessage: MessageEntity) {
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

    suspend fun resetUnreadCount(dialogId: String): LiveData<DialogEntity?> {
        // Get the dialog by its ID from the database
        return  dialogDao.getDialogById(dialogId)
    }

    suspend fun getDialogMM(dialogId: String): DialogEntity? {
        return  dialogDao.getDialog(dialogId)
    }

    suspend fun clearAll(){
        dialogDao.deleteAll()
    }


    suspend fun fetchDialogsFromNetwork(): DataResult<List<DialogEntity>> {
        return try {
            val response = apiService.getChats() // Perform API call
            if (response.isSuccessful) {
                val chatsResponse = response.body() // Parse data
                chatsResponse?.let {
                    val chatList = chatsResponse.data
                    val dialogs = chatList.map { chat ->
                        // Perform the conversion for each Chat to DialogEntity
                        DialogEntity(
                            id = chat._id,
                            dialogPhoto = "", // Set appropriate dialog photo
                            dialogName = chat.name,
                            users = chat.participants.map { it.toUserEntity()},
                            lastMessage = chat.lastMessage?.toMessageEntity(),
                            unreadCount = 0 // Set appropriate initial unread count
                        )
                    }
                    DataResult.Success(dialogs) // Return success result with data
                } ?: DataResult.Failure("Empty response") // Handle empty response
            } else {
                DataResult.Failure("Failed to fetch data") // Handle API call failure
            }
        } catch (e: Exception) {
            DataResult.Failure("Network request failed: ${e.message}") // Handle exceptions
        }
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

//        val dialog = dialogRepository.getDialog(chat)
//
//        if (dialog.users.size > 1) {
//            text = if (senderName.isNotEmpty()) {
//                "$senderName: " // Highlighted sender's name
//            } else {
//                "Anonymous:"
//            }
//        }

//        text += content



        // Handle attachments and assign URLs
        if (attachments != null && attachments?.isNotEmpty() == true) {
            val attachments = attachments
            if (attachments != null) {
                for (attachment in attachments) {
                    val fileType = getFileType(attachment.url)
                    when (fileType) {
                        FileType.IMAGE -> {
                            imageUrl = attachment.url
//                            Log.d(TAG, "Image, Path Of Image Received: $imageUrl")
                            text += "📷 Image"
                        }

                        FileType.AUDIO -> {
                            audioUrl = attachment.url
//                            Log.d(TAG, "Audio, Path Of Image Received: $audioUrl")
//                            audioList.add(audioUrl)
                            text += "🎵 Audio"
                        }

                        FileType.VIDEO -> {
                            videoUrl = attachment.url
//                            Log.d(TAG, "Video, Path Of Image Received: $videoUrl")
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


    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertTimestamp(iso8601Date: String): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val localDateTime = LocalDateTime.parse(iso8601Date, formatter)

        // Assuming the input time is in UTC, you can convert it to the device's time zone
        val zonedDateTime = ZonedDateTime.of(localDateTime, java.time.ZoneId.systemDefault())

        return zonedDateTime.toInstant().toEpochMilli()
    }


    private fun insertDialogS(dialog: DialogEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            // Check if the dialog with the same ID already exists in the database
            val existingDialogFlow = getDialogByIDFlow(dialog.id)
            withContext(Dispatchers.Main) {
                existingDialogFlow.collect { existingDialog ->
                    if (existingDialog == null) {
                        // Dialog does not exist in the database, so save it
                        CoroutineScope(Dispatchers.IO).launch {
                            insertDialog(dialog)
                        }
                    } else {
                        // Dialog already exists in the database, so update it with a new dialog entity
//                        Log.d(TAG, "${dialog.dialogName} already exists in the database")

                        if (existingDialog.lastMessage?.id != dialog.lastMessage?.id && dialog.lastMessage?.userId != userId) {
                            // Create or obtain a new ChatDialogEntity that you want to update the existing one with
//                            Log.d(TAG, "Updating dialog....... ${dialog.dialogName}")
                            val updatedDialog = DialogEntity(
                                id = existingDialog.id,
                                dialogPhoto = dialog.dialogPhoto,
                                dialogName = dialog.dialogName,
                                users = dialog.users,
                                lastMessage = dialog.lastMessage, // Provide the new last message
                                unreadCount = existingDialog.unreadCount + 1 // Increment the unread count
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                // Update the existing dialog in the database
                                updateDialog(updatedDialog)
                            }
                        } else {
//                            Log.d(
//                                TAG,
//                                "${dialog.dialogName} already has the same latest message"
//                            )
                        }
                    }
                }
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchAndInsertDialogs() {
        try {
            var offset = 0
            val batchSize = 20
            var allDialogsFetched = false

            while (!allDialogsFetched) {
                val response = apiService.getChats(offset, batchSize) // Fetch a batch of dialogs from the network with offset and batch size
                if (response.isSuccessful) {
                    val chatsResponse = response.body() // Parse data

//                    Log.d("FetchedDialogs", chatsResponse.toString())
                    chatsResponse?.let {
                        val chatList = chatsResponse.data

                        val dialogs = chatList.take(batchSize).map { chat ->
                            // Perform the conversion for each Chat to DialogEntity

                            chatIdList.add(chat._id)
                            DialogEntity(
                                id = chat._id,
                                dialogPhoto = "", // Set appropriate dialog photo
                                dialogName = chat.name,
                                users = chat.participants.map { it.toUserEntity() },
                                lastMessage = chat.lastMessage?.toMessageEntity(),
                                unreadCount = 0 // Set appropriate initial unread count
                            )
                        }

//                        dialogDao.insertAllDialogs(dialogs)
                        insertDialogs(dialogs)

                        offset += batchSize // Increment offset to fetch the next batch
                        if (chatList.size < batchSize) {
                            allDialogsFetched = true // Indicates all dialogs have been fetched
                        }
                    } ?: run {
                        allDialogsFetched = true // Handle empty response
                    }
                } else {
                    // Handle API call failure
                    // Log or propagate the error if necessary
                    DataResult.Failure("Failed to fetch data") // Handle API call failure
                    allDialogsFetched = true
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



    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertTime(iso8601Date: String): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val localDateTime = LocalDateTime.parse(iso8601Date, formatter)

        // Explicitly set the input date's time zone to UTC
        val utcDateTime = ZonedDateTime.of(localDateTime, java.time.ZoneOffset.UTC)

        // Convert UTC to the local time zone
        val localZonedDateTime = utcDateTime.withZoneSameInstant(TimeZone.getDefault().toZoneId())

        return localZonedDateTime.toInstant().toEpochMilli()
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

//                    Log.d("FetchedDialogs", chatsResponse.toString())
//                    Log.d("FetchedDialogs", chatsResponse?.data?.size.toString())

                    chatsResponse?.let {
                        val chatList = chatsResponse.data

                        val dialogs = chatList.filter { !it.isGroupChat }.mapNotNull { chat ->
                            // Check if the dialog ID is already fetched
                            if (!fetchedDialogIds.contains(chat._id)) {
                                chatIdList.add(chat._id)
                                fetchedDialogIds.add(chat._id)

                                val users = chat.participants.map { it.toUserEntity() }

//                                Log.d("User", "User Id : $userId")
//                                Log.d("User", "User Name : $username")

                                val firstUser = users.first { it.id != userId }

                                var chatName = ""
                                var chatAvatar = ""

                                var text = ""
                                var senderName = ""

                                filteredUsers = listOf(firstUser) as List<UserEntity>

                                chatName = firstUser.name ?: ""
                                chatAvatar = firstUser.avatar ?: ""

//                                Log.d("User", "Chat User : $firstUser")


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
                        val improperDialogs = dialogs.filter { it.dialogName == username }
                        val filteredDialogs = dialogs.filter { it.lastMessage != null }
                        val newDialogs = dialogs.filter { it.lastMessage == null }
//                        Log.d("Improper Dialogs", "Improper Dialogs : $improperDialogs")
//                        Log.d("Filtered Dialogs", "Filtered Dialogs : $filteredDialogs")
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


    private fun groupChatImages(): List<String> {
        // Replace these placeholder URLs with your actual image URLs
        return arrayListOf(
            "http://i.imgur.com/pv1tBmT.png",
            "http://i.imgur.com/R3Jm1CL.png",
            "http://i.imgur.com/ROz4Jgh.png",
            "http://i.imgur.com/Qn9UesZ.png"
        )
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
