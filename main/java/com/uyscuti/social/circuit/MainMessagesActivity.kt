package com.uyscuti.social.circuit

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

import com.uyscuti.social.chatsuit.commons.ImageLoader
import com.uyscuti.social.chatsuit.messages.MessagesListAdapter
import com.uyscuti.social.circuit.presentation.DialogViewModel
import com.uyscuti.social.circuit.presentation.MessageViewModel
import com.uyscuti.social.circuit.data.fixtures.MessagesFixtures
import com.uyscuti.social.circuit.data.model.Dialog
import com.uyscuti.social.circuit.data.model.Message
import com.uyscuti.social.circuit.data.model.User
import com.uyscuti.social.circuit.utils.AppUtils
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.entity.GroupDialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.core.common.data.room.repository.GroupDialogRepository
import com.uyscuti.social.core.common.data.room.repository.MessageRepository
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.pow
import kotlin.random.Random


@AndroidEntryPoint
abstract class MainMessagesActivity : AppCompatActivity(), MessagesListAdapter.SelectionListener,
    MessagesListAdapter.OnLoadMoreListener, MessagesListAdapter.OnDeleteListener {

    private var TOTAL_MESSAGES_COUNT = 10
    private var first_messages_count = 0

//    protected val senderId = "0"
//    protected lateinit var imageLoader: ImageLoader
//    protected lateinit var messagesAdapter: MessagesListAdapter<Message>

    private var firstLoad = true

    private lateinit var messageRepository: MessageRepository

    protected val senderId = "0"
    var imageLoader: ImageLoader? = null
    var messagesAdapter: MessagesListAdapter<Message>? = null
    private var menu: Menu? = null
    private var selectionCount = 0
    private var lastLoadedDate: Date? = null

    private lateinit var settings: SharedPreferences
    private val PREFS_NAME = "LocalSettings" // Change this to a unique name for your app

    private var dialog: Dialog? = null

    private var isGroup = false

    private var lastMessageId: String? = null

    private val messageViewModel: MessageViewModel by viewModels()
    private val dialogViewModel: DialogViewModel by viewModels()

    private lateinit var groupDialogRepository: GroupDialogRepository

    private var selectedMessagesIds = ArrayList<String>()

//    private var menu: Menu? = null
//    private var selectionCount = 0
//    private var lastLoadedDate: Date? = null

    private lateinit var chatId: String
    private lateinit var myId: String

    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//
//        imageLoader = ImageLoader { imageView, url, _ ->
//            Picasso.get().load(url).into(imageView)
//        }

//        messageRepository = MessageRepository(this,ChatDatabase.getInstance(this).messageDao(),re)

        groupDialogRepository = GroupDialogRepository(
            ChatDatabase.getInstance(this).groupDialogDao(),
            retrofitInterface,
            localStorage
        )
        imageLoader = ImageLoader { imageView: ImageView?, url: String?, _: Any? ->
            try {
                // Check if the URL is for a video file (you can adjust the list of video file extensions)
                if (isVideoFile(url)) {
//                    Log.d("Thumbnail", "The url is a video : $url")

                    if (url != null) {
                        if (imageView != null) {
//                            loadVideoThumbnailP(this, url, imageView)
                            loadVideoThumbnail(this, url, imageView)
                        }
                    }
                } else {
                    // It's not a video, load it as an image
//                    Picasso.get().load(url).into(imageView)

                    if (imageView != null) {
                        if (url != null) {
                            loadVideoThumbnail(this, url, imageView)
                        }
                    }

                }
            } catch (error: Exception) {
                Log.d("Thumbnail Exception", "The exception is : ${error.message}")
            }
        }
        settings = getSharedPreferences(PREFS_NAME, 0)

//        lastMessageId = dialog?.lastMessage?.id

        loadMessageList()
    }

    fun getRetrofit(): RetrofitInstance {
        return retrofitInterface
    }

    fun getLocalStorageInstance(): LocalStorage {
        return localStorage
    }

    override fun onStart() {
        super.onStart()
//        messagesAdapter.addToStart(getTextMessage(), true)

        if (firstLoad) {
//            loadFirstMessages()
            initMessages()
        }
    }

    fun setDependencies(localStorage: LocalStorage, retrofitInstance: RetrofitInstance) {
        this.localStorage = localStorage
        this.retrofitInterface = retrofitInstance
    }

//    private fun initMessages() {
//        firstLoad = false
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val message = messageViewModel.getLastMessage(chatId, myId)
//
//                val userId = if (message?.userId == myId) "0" else "1"
//                val status = message?.status
//                val user = User(
//                    userId,
//                    message?.user?.name,
//                    message!!.user.avatar,
//                    message.user.online,
//                    message.user.lastSeen
//                )
//                val date = Date(message.createdAt)
//
//                // Check if the text is "None" and imageUrl is not null
//                val messageContent = if (message.imageUrl != null) {
////                        user.id = "0"
//                    Message(
//                        message.id,
//                        user,
//                        null,
//                        date
//                    ).apply {
//                        setImage(Message.Image(message.imageUrl!!))
//                        setStatus(status)
//                    }
//                } else if (message.videoUrl != null) {
////                        user.id = "0"
//                    Message(
//                        message.id,
//                        user,
//                        null,
//                        date
//                    ).apply {
//                        setVideo(Message.Video(message.videoUrl!!))
//                        setStatus(status)
//                    }
//                } else if (message.audioUrl != null) {
////                        user.id = "0"
//                    Message(
//                        message.id,
//                        user,
//                        null,
//                        date
//                    ).apply {
//                        setAudio(
//                            Message.Audio(
//                                message.audioUrl!!,
//                                0,
//                                getNameFromUrl(message.audioUrl!!)
//                            )
//                        )
//                        setStatus(status)
//                    }
//                } else if (message.voiceUrl != null) {
////                        user.id = "0"
//                    Message(
//                        message.id,
//                        user,
//                        null,
//                        date
//                    ).apply {
//                        setVoice(Message.Voice(message.voiceUrl!!, 10000))
//                        setStatus(status)
//                    }
//                } else if (message.docUrl != null) {
////                        user.id = "0"
//                    Message(
//                        message.id,
//                        user,
//                        null,
//                        date
//                    ).apply {
//
//                        val size = getFileSize(message.docUrl!!)
//                        setDocument(
//                            Message.Document(
//                                message.docUrl!!,
//                                getNameFromUrl(message.docUrl!!),
//                                formatFileSize(size)
//                            )
//                        )
//                        setStatus(status)
//                    }
//                } else {
//                    Message(
//                        message.id,
//                        user,
//                        message.text,
//                        date
//                    ).apply {
//                        setStatus(status)
//                    }
//                }
//                withContext(Dispatchers.Main) {
//
//                    if (!message.deleted ){
//                        messagesAdapter?.addToStart(messageContent, true)
//                    }
//                }
//
//            } catch (e: Exception) {
//                Log.e("LoadMessages", "Failure : ${e.message}")
//                e.printStackTrace()
//            }
//        }
//    }


    private fun initMessages() {
        firstLoad = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val message = messageViewModel.getLastMessage(chatId, myId)

                // Add null check here
                if (message == null) {
                    Log.e("LoadMessages", "No message found")
                    return@launch
                }

                val userId = if (message.userId == myId) "0" else "1"
                val status = message.status

                // Add null check for user
                val user = message.user?.let {
                    User(
                        userId,
                        it.name,
                        it.avatar,
                        it.online,
                        it.lastSeen
                    )
                } ?: User(userId, "Unknown", "", false, null)

                val date = Date(message.createdAt)

                // Check if the text is "None" and imageUrl is not null
                val messageContent = if (message.imageUrl != null) {
                    Message(
                        message.id,
                        user,
                        null,
                        date
                    ).apply {
                        setImage(Message.Image(message.imageUrl!!))
                        setStatus(status)
                    }
                } else if (message.videoUrl != null) {
                    Message(
                        message.id,
                        user,
                        null,
                        date
                    ).apply {
                        setVideo(Message.Video(message.videoUrl!!))
                        setStatus(status)
                    }
                } else if (message.audioUrl != null) {
                    Message(
                        message.id,
                        user,
                        null,
                        date
                    ).apply {
                        setAudio(
                            Message.Audio(
                                message.audioUrl!!,
                                0,
                                getNameFromUrl(message.audioUrl!!)
                            )
                        )
                        setStatus(status)
                    }
                } else if (message.voiceUrl != null) {
                    Message(
                        message.id,
                        user,
                        null,
                        date
                    ).apply {
                        setVoice(Message.Voice(message.voiceUrl!!, 10000))
                        setStatus(status)
                    }
                } else if (message.docUrl != null) {
                    Message(
                        message.id,
                        user,
                        null,
                        date
                    ).apply {
                        val size = getFileSize(message.docUrl!!)
                        setDocument(
                            Message.Document(
                                message.docUrl!!,
                                getNameFromUrl(message.docUrl!!),
                                formatFileSize(size)
                            )
                        )
                        setStatus(status)
                    }
                } else {
                    Message(
                        message.id,
                        user,
                        message.text,
                        date
                    ).apply {
                        setStatus(status)
                    }
                }

                withContext(Dispatchers.Main) {
                    if (!message.deleted) {
                        messagesAdapter?.addToStart(messageContent, true)
                    }
                }

            } catch (e: Exception) {
                Log.e("LoadMessages", "Failure : ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun setIsGroup(isGroup: Boolean) {
        this.isGroup = isGroup
    }

    fun setChatId(chat: String) {
        chatId = chat
    }

    fun setDialog(dialog: Dialog) {
        if (dialog.lastMessage != null) {
            lastMessageId = dialog.lastMessage.id
        }
    }

    fun setMyId(id: String) {
        myId = id
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.chat_actions_menu, menu)
        onSelectionChanged(0)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Handle menu item clicks here
            R.id.action_delete_ -> {

//                Log.d("SelectedMessages", "${selectedMessagesIds.size}")
                messagesAdapter?.deleteSelectedMessages()

                val selectedMessages = messagesAdapter?.allSelectedMessages

//                Log.d("SelectedMessages", "${selectedMessages?.size}")

                selectedMessages?.map {
                    selectedMessagesIds.add(it.id)
                }
//                CoroutineScope(Dispatchers.IO).launch {
//                    messageViewModel.deleteMessages(selectedMessagesIds)
//                }
            }

            R.id.action_copy_ -> {
                messagesAdapter?.copySelectedMessagesText(this, getMessageStringFormatter(), true);
                AppUtils.showToast(this, R.string.copied_message, true);
            }

            else -> {

            }
        }
        return true
    }


    @OptIn(UnstableApi::class)
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed()
            val upIntent = Intent(this, MainActivity::class.java)
            upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            NavUtils.navigateUpTo(this, upIntent)
            finish()
        } else {
            messagesAdapter?.unselectAllItems()
        }
    }

    override fun onLoadMore(page: Int, totalItemsCount: Int) {
        if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
//            loadMessages()
//            loadMessageList()
        }
    }

    private fun loadFirstMessages() {
        firstLoad = false
        CoroutineScope(Dispatchers.IO).launch {

            val messageList: List<MessageEntity> = messageViewModel.getLastMessagesByChatId(chatId)

            first_messages_count = messageList.size

            val sortedMessages = messageList.sortedByDescending { it.createdAt }

            Log.d("TAG", "Sorted Message List: $sortedMessages")
            val filteredMessages = sortedMessages.filter { !it.deleted }

            val messages = filteredMessages.filter { message ->
                val userId = if (message.userId == myId) "0" else "1"
                userId != "0" || (userId == "0" && message.id.startsWith("Image") || message.id.startsWith(
                    "Video"
                ) || message.id.startsWith("Audio") || message.id.startsWith("Text") || message.id.startsWith(
                    "Doc"
                ))
            }.map { message ->
                val userId = if (message.userId == myId) "0" else "1"
                val status = message.status
                val user =
                    User(
                        userId,
                        message.user.name,
                        message.user.avatar,
                        message.user.online,
                        message.user.lastSeen
                    )
                val date = Date(message.createdAt)

                // Check if the text is "None" and imageUrl is not null
                val messageContent = if (message.imageUrl != null) {
//                        user.id = "0"
                    Message(
                        message.id,
                        user,
                        null,
                        date
                    ).apply {
                        setImage(Message.Image(message.imageUrl!!))
                        setStatus(status)
                    }
                } else if (message.videoUrl != null) {
//                        user.id = "0"
                    Message(
                        message.id,
                        user,
                        null,
                        date
                    ).apply {
                        setVideo(Message.Video(message.videoUrl!!))
                        setStatus(status)
                    }
                } else if (message.audioUrl != null) {
//                        user.id = "0"
                    Message(
                        message.id,
                        user,
                        null,
                        date
                    ).apply {
                        setAudio(
                            Message.Audio(
                                message.audioUrl!!,
                                0,
                                getNameFromUrl(message.audioUrl!!)
                            )
                        )
                        setStatus(status)
                    }
                } else if (message.voiceUrl != null) {
//                        user.id = "0"
                    Message(
                        message.id,
                        user,
                        null,
                        date
                    ).apply {
                        setVoice(Message.Voice(message.voiceUrl!!, 10000))
                        setStatus(status)
                    }
                } else if (message.docUrl != null) {
//                        user.id = "0"
                    Message(
                        message.id,
                        user,
                        null,
                        date
                    ).apply {

                        val size = getFileSize(message.docUrl!!)
                        setDocument(
                            Message.Document(
                                message.docUrl!!,
                                getNameFromUrl(message.docUrl!!),
                                formatFileSize(size)
                            )
                        )
                        setStatus(status)
                    }
                } else {
                    Message(
                        message.id,
                        user,
                        message.text,
                        date
                    ).apply {
                        setStatus(status)
                    }
                }
                messageContent
            } as List<Message>

            runOnUiThread {
                messagesAdapter?.addInitialMessages(messages)
            }
        }
    }

    private fun loadMessageList() {

        Handler().post {
            CoroutineScope(Dispatchers.Main).launch {
//                messageViewModel.getMessages(chatId)
                val messageList: List<MessageEntity> = messageViewModel.messages(chatId)
                val sortedMessages = messageList.sortedByDescending { it.createdAt }
                val filteredMessages = sortedMessages.filter { !it.deleted }
                TOTAL_MESSAGES_COUNT = filteredMessages.size
//                Log.d(TAG, "Message List: $messageList")
//                Log.d(TAG, "Sorted Message List: $sortedMessages")
                val messages = filteredMessages.filter { message ->
                    val userId = if (message.userId == myId) "0" else "1"
                    userId != "0" || (userId == "0" && message.id.startsWith("Image") || message.id.startsWith(
                        "Video"
                    ) || message.id.startsWith("Audio") || message.id.startsWith("Text") || message.id.startsWith(
                        "Doc"
                    ))
                }.map { message ->
                    val userId = if (message.userId == myId) "0" else "1"
                    val status = message.status
                    val user =
                        User(
                            userId,
                            message.user.name,
                            message.user.avatar,
                            message.user.online,
                            message.user.lastSeen
                        )
                    val date = Date(message.createdAt)

                    // Check if the text is "None" and imageUrl is not null
                    val messageContent = if (message.imageUrl != null) {
//                        user.id = "0"
                        Message(
                            message.id,
                            user,
                            null,
                            date
                        ).apply {
                            setImage(Message.Image(message.imageUrl!!))
                            setStatus(status)
                        }
                    } else if (message.videoUrl != null) {
//                        user.id = "0"
                        Message(
                            message.id,
                            user,
                            null,
                            date
                        ).apply {
                            setVideo(Message.Video(message.videoUrl!!))
                            setStatus(status)
                        }
                    } else if (message.audioUrl != null) {
//                        user.id = "0"
                        Message(
                            message.id,
                            user,
                            null,
                            date
                        ).apply {
                            setAudio(
                                Message.Audio(
                                    message.audioUrl!!,
                                    0,
                                    getNameFromUrl(message.audioUrl!!)
                                )
                            )
                            setStatus(status)
                        }
                    } else if (message.voiceUrl != null) {
//                        user.id = "0"
                        Message(
                            message.id,
                            user,
                            null,
                            date
                        ).apply {
                            setVoice(Message.Voice(message.voiceUrl!!, 10000))
                            setStatus(status)
                        }
                    } else if (message.docUrl != null) {
//                        user.id = "0"
                        Message(
                            message.id,
                            user,
                            null,
                            date
                        ).apply {

                            val size = getFileSize(message.docUrl!!)
                            setDocument(
                                Message.Document(
                                    message.docUrl!!,
                                    getNameFromUrl(message.docUrl!!),
                                    formatFileSize(size)
                                )
                            )
                            setStatus(status)
                        }
                    } else {
                        Message(
                            message.id,
                            user,
                            message.text,
                            date
                        ).apply {
                            setStatus(status)
                        }
                    }
                    messageContent
                } as List<Message>


                withContext(Dispatchers.Main) {
                    if (messages.isNotEmpty()) {
                        // Create a filtered list without the last message
                        val filteredMessageList = messages.subList(1, messages.size)

                        // Add the filtered messages to the adapter
                        messagesAdapter?.addToEnd(filteredMessageList, false)
                    }
                }
            }
        }
    }

    private fun formatFileSize(fileSize: Long): String {
        if (fileSize <= 0) {
            return "0 B"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(fileSize.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            "%.1f %s",
            fileSize / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    fun getNameFromUrl(videoUrl: String): String {
        // Split the URL using '/' as a delimiter and get the last part, which is the video filename
        val parts = videoUrl.split("/")

        // You can further process the filename if needed, such as removing the file extension
        return parts.last()
    }


    fun getFileSize(filePath: String): Long {
        Log.d("Attachment File Size", "File path to be is : $filePath")

        try {
            val uri = URI.create(filePath)
            if (uri.scheme == "file") {
                // It's a local file
                val file = File(uri)
                if (file.exists()) {
//                    Log.d("Attachment File Size", "File Size is : ${file.length()}")
                    return file.length()
                }
            } else if (uri.scheme == "http" || uri.scheme == "https") {
                // It's a remote URL, you can handle it differently or return an appropriate value
//                Log.d("Attachment File Size", "Remote URL detected")
                return 0L // Or handle it according to your requirements
            }
        } catch (e: IllegalArgumentException) {
            // Handle invalid URIs here if needed
//            Log.e("Attachment File Size", "Invalid URI: $filePath")
            e.printStackTrace()
        }

        return 0L // Return 0 for unsupported or non-existent files
    }

    override fun onSelectionChanged(count: Int) {
        selectionCount = count
        menu?.findItem(R.id.action_delete_)?.isVisible = count > 0
        menu?.findItem(R.id.action_copy_)?.isVisible = count > 0
        menu?.findItem(R.id.menu_voice)?.isVisible = count == 0
        menu?.findItem(R.id.menu_video)?.isVisible = count == 0
        menu?.findItem(R.id.menu_block)?.isVisible = count == 0
    }

    private fun loadMessages() {
        // Imitation of internet connection
        Handler().postDelayed({
            val messages = MessagesFixtures.getMessages(lastLoadedDate)
            lastLoadedDate = messages[messages.size - 1].createdAt
            messagesAdapter?.addToEnd(messages, false)
        }, 100)
    }

    private fun loadVideoThumbnail(context: Context, videoUrl: String, imageView: ImageView) {
        Glide.with(context)
            .load(videoUrl)
            .apply(
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .override(
                        resources.getDimensionPixelSize(com.uyscuti.social.chatsuit.R.dimen.max_image_width),
                        resources.getDimensionPixelSize(com.uyscuti.social.chatsuit.R.dimen.max_image_height)
                    )
            )
            .into(imageView)
    }

    private fun isVideoFile(url: String?): Boolean {
        // List of video file extensions (you can add more if needed)
        val videoExtensions = listOf(
            ".mp4", ".avi", ".mkv", ".mov", ".flv",
            ".webm", ".wmv", ".mpg", ".mpeg", ".3gp",
            ".ogv", ".ogm", ".ts", ".vob", ".m4v",
            ".divx", ".rm", ".rmvb", ".asf", ".m2ts",
            ".mts", ".f4v", ".swf", ".dat", ".yuv",
            ".r3d", ".m2v", ".m1v", ".fla", ".f4p"
        )

        // Check if the URL ends with any of the video extensions
        return videoExtensions.any { url?.endsWith(it, true) == true }
    }

    private fun getMessageStringFormatter(): MessagesListAdapter.Formatter<Message> {
        return MessagesListAdapter.Formatter { message ->
            val createdAt = SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                .format(message.createdAt)
            val text = message.text
            String.format(
                Locale.getDefault(),
                "%s: %s (%s)",
                message.user.name,
                text,
                createdAt
            )
        }
    }

    private fun isFileInLocalStorage(audioFilePath: String): Boolean {
        return try {
            // Check if the file path does not start with "file://" or "/storage/"
            !(audioFilePath.startsWith("file://") || audioFilePath.startsWith("/storage/"))
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    suspend fun getCachedOrCalculateAudioDuration(context: Context, audioFilePath: String): Long {
        val preferences: SharedPreferences =
            context.getSharedPreferences("audio_duration_cache", Context.MODE_PRIVATE)
        // Check if the cache contains the duration for the given audioFilePath
        if (preferences.contains(audioFilePath)) {
            return preferences.getLong(audioFilePath, 0)
        }

        if (audioFilePath.startsWith("file://") || audioFilePath.startsWith("/storage/")) {
            // Create or obtain a reference to the SharedPreferences

            val startTime = System.currentTimeMillis()
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(audioFilePath)

                val durationStr =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                if (durationStr != null) {
                    val endTime = System.currentTimeMillis()
                    val executionTime = endTime - startTime

                    Log.d("Audio Duration", "Execution Time: $executionTime")

                    val duration = durationStr.toLong()

                    // Cache the duration for future use
                    preferences.edit().putLong(audioFilePath, duration).apply()

                    return duration
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val endTime = System.currentTimeMillis()
            val executionTime = endTime - startTime
            Log.d("Audio Duration", "Execution Time: $executionTime")

            return 0

        } else {
            return 0
        }
        return 0 // Return 0 if there was an error or if the duration couldn't be retrieved.
    }

    override fun onDelete(deletedItems: MutableList<String>?) {
//        Log.d("OnDelete", "Messages : ${deletedItems?.size}")
//        Log.d("OnDelete", "last Message id : $lastMessageId ")
        CoroutineScope(Dispatchers.IO).launch {
            if (deletedItems != null) {

//                Log.d("onDelete", "Deleting Messages")

                val isLast = deletedItems.contains(lastMessageId)


                if (isGroup) {
                    messageViewModel.markMessagesDeleted(deletedItems)
//                    Log.d("onDelete", "Deleting Messages : ${deletedItems.size}")

                } else {

                    try {
                        messageViewModel.deleteMessages(deletedItems)

                    } catch (e:Exception){
                        e.printStackTrace()
                    } finally {
                        if (isLast){
                            val last = getLastMessage(chatId)
                            if (last != null) {
                                dialogViewModel.updateLastMessageForThisChat(chatId, last)
                            }
                        }
                    }
                }

//                val lastMessage = notifyMessageDeletion()



                if (isLast) {
                    if (isGroup) {
                        val groupDG = groupDialogRepository.getDialog(chatId)
                        val empty = setEmptyMessage(groupDG)
//                        val dialog = groupDialogRepository.getDialog(chatId)
                        groupDialogRepository.updateLastMessageForThisChat(chatId, empty)
//                        .updateLastMessageForThisChat(chatId,empty)
                    } else {
//                        dialogViewModel.updateLastMessageForThisChat(chatId, lastMessage)
                    }
                }
            }
        }
    }


    private fun convertIso8601ToUnixTimestamp(iso8601Date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")


        val date = sdf.parse(iso8601Date)
        return date?.time ?: 0
    }

    private suspend fun getLastMessage(chat: String): MessageEntity? {

        return messageViewModel.getLastMessage(chatId)
    }

    private fun notifyMessageDeletion(): MessageEntity {
        val createdAt = System.currentTimeMillis()
        val lastseen = Date(createdAt)

        val avatar = settings.getString("avatar", "avatar").toString()

        val user = UserEntity(
            id = myId,
            name = "You",
            avatar = avatar,
            online = true,
            lastSeen = lastseen
        )

        return MessageEntity(
            id = "DeletedMessage_${Random.nextInt()}",
            chatId = chatId,
            text = "You deleted a message.",
            userId = "Flash",
            user = user,
            createdAt = createdAt,
            imageUrl = null,
            voiceUrl = null,
            voiceDuration = 0,
            userName = "You",
            status = "Received",
            videoUrl = null,
            audioUrl = null,
            docUrl = null,
            fileSize = 0
        )
    }


    private fun setEmptyMessage(groupDG: GroupDialogEntity): MessageEntity {
        val createdAt = System.currentTimeMillis()
        val lastseen = Date(createdAt)

        val avatar = settings.getString("avatar", "avatar").toString()

        val user = UserEntity(
            id = myId,
            name = "You",
            avatar = "avatar",
            online = true,
            lastSeen = lastseen
        )

        return MessageEntity(
            id = "DeletedMessage_${Random.nextInt()}",
            chatId = chatId,
            text = "",
            userId = "Flash",
            user = user,
            createdAt = groupDG.lastMessage.createdAt,
            imageUrl = null,
            voiceUrl = null,
            voiceDuration = 0,
            userName = "You",
            status = "Received",
            videoUrl = null,
            audioUrl = null,
            docUrl = null,
            fileSize = 0,
            deleted = true
        )
    }


    companion object {
        private const val CACHE_DIR_NAME = "audio_duration_cache"
    }
}
