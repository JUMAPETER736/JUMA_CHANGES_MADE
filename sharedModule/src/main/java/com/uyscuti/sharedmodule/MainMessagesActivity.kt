package com.uyscuti.sharedmodule

import android.content.Context
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
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.sharedmodule.data.fixtures.MessagesFixtures
import com.uyscuti.social.core.models.data.Dialog
import com.uyscuti.social.core.models.data.Message
import com.uyscuti.social.core.models.data.User
import com.uyscuti.sharedmodule.presentation.DialogViewModel
import com.uyscuti.sharedmodule.presentation.MessageViewModel
import com.uyscuti.sharedmodule.utils.AppUtils
import com.uyscuti.social.chatsuit.commons.ImageLoader
import com.uyscuti.social.chatsuit.messages.MessagesListAdapter
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
import java.util.TimeZone
import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.pow
import kotlin.random.Random

@AndroidEntryPoint
abstract class MainMessagesActivity : AppCompatActivity(), MessagesListAdapter.SelectionListener,
    MessagesListAdapter.OnLoadMoreListener, MessagesListAdapter.OnDeleteListener {

    private var TOTAL_MESSAGES_COUNT = 10
    private var first_messages_count = 0

    private var firstLoad = true

    private lateinit var messageRepository: MessageRepository

    protected val senderId = "0"
    var imageLoader: ImageLoader? = null
    var messagesAdapter: MessagesListAdapter<Message>? = null
    private var menu: Menu? = null
    private var selectionCount = 0
    private var lastLoadedDate: Date? = null

    private lateinit var settings: SharedPreferences
    private val PREFS_NAME = "LocalSettings"

    private var dialog: Dialog? = null

    private var isGroup = false

    private var lastMessageId: String? = null

    private val messageViewModel: MessageViewModel by viewModels()
    private val dialogViewModel: DialogViewModel by viewModels()

    internal lateinit var groupDialogRepository: GroupDialogRepository

    private var selectedMessagesIds = ArrayList<String>()

    private lateinit var chatId: String
    private lateinit var myId: String

    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        groupDialogRepository = GroupDialogRepository(
            ChatDatabase.Companion.getInstance(this).groupDialogDao(),
            retrofitInterface,
            localStorage
        )
        imageLoader = ImageLoader { imageView: ImageView?, url: String?, _: Any? ->
            try {
                if (isVideoFile(url)) {
                    if (url != null) {
                        if (imageView != null) {
                            loadVideoThumbnail(this, url, imageView)
                        }
                    }
                } else {
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

        if (firstLoad) {
            initMessages()
        }
    }

    fun setDependencies(localStorage: LocalStorage, retrofitInstance: RetrofitInstance) {
        this.localStorage = localStorage
        this.retrofitInterface = retrofitInstance
    }

    private fun initMessages() {
        firstLoad = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val message = messageViewModel.getLastMessage(chatId, myId)

                val userId = if (message?.userId == myId) "0" else "1"
                val status = message?.status
                val user = User(
                    userId,
                    message?.user?.name,
                    message!!.user.avatar,
                    message.user.online,
                    message.user.lastSeen
                )
                val date = Date(message.createdAt)

                val messageContent = if (message.imageUrl != null) {
                    Message(message.id, user, null, date).apply {
                        setImage(Message.Image(message.imageUrl!!))
                        setStatus(status)
                    }
                } else if (message.videoUrl != null) {
                    Message(message.id, user, null, date).apply {
                        setVideo(Message.Video(message.videoUrl!!))
                        setStatus(status)
                    }
                } else if (message.audioUrl != null) {
                    Message(message.id, user, null, date).apply {
                        setAudio(Message.Audio(message.audioUrl!!, 0, getNameFromUrl(message.audioUrl!!)))
                        setStatus(status)
                    }
                } else if (message.voiceUrl != null) {
                    Message(message.id, user, null, date).apply {
                        setVoice(Message.Voice(message.voiceUrl!!, 10000))
                        setStatus(status)
                    }
                } else if (message.docUrl != null) {
                    Message(message.id, user, null, date).apply {
                        val size = getFileSize(message.docUrl!!)
                        setDocument(Message.Document(message.docUrl!!, getNameFromUrl(message.docUrl!!), formatFileSize(size)))
                        setStatus(status)
                    }
                } else {
                    val rawText = message.text ?: ""
                    val displayText = if (rawText.startsWith("circuit://join/group/"))
                        "👥  Group Invite — tap to join"
                    else
                        rawText
                    Message(message.id, user, displayText, date).apply {
                        setStatus(status)

                        // use stored flag
                        if (message.isSystemMessage) {
                            Log.d("SystemMsg", "text='$rawText' isSystem=true [initMessages]")
                            setSystemMessage(true)
                        }
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
            R.id.action_delete_ -> {
                messagesAdapter?.deleteSelectedMessages()
                val selectedMessages = messagesAdapter?.allSelectedMessages
                selectedMessages?.map {
                    selectedMessagesIds.add(it.id)
                }
            }

            R.id.action_copy_ -> {
                messagesAdapter?.copySelectedMessagesText(this, getMessageStringFormatter(), true)
                AppUtils.showToast(this, R.string.copied_message, true)
            }

            else -> {}
        }
        return true
    }

    @OptIn(UnstableApi::class)
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed()
            finish()
        } else {
            messagesAdapter?.unselectAllItems()
        }
    }

    override fun onLoadMore(page: Int, totalItemsCount: Int) {
        if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
            // reserved for pagination
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

                // system messages always pass through
                if (message.isSystemMessage) return@filter true

                val userId = if (message.userId == myId) "0" else "1"
                if (userId != "0") return@filter true

                message.id.startsWith("Image") ||
                        message.id.startsWith("Video") ||
                        message.id.startsWith("Audio") ||
                        message.id.startsWith("Text") ||
                        message.id.startsWith("Doc")
            }.map { message ->
                val userId = if (message.userId == myId) "0" else "1"
                val status = message.status
                val user = User(
                    userId,
                    message.user.name,
                    message.user.avatar,
                    message.user.online,
                    message.user.lastSeen
                )
                val date = Date(message.createdAt)

                if (message.imageUrl != null) {
                    Message(message.id, user, null, date).apply {
                        setImage(Message.Image(message.imageUrl!!))
                        setStatus(status)
                    }
                } else if (message.videoUrl != null) {
                    Message(message.id, user, null, date).apply {
                        setVideo(Message.Video(message.videoUrl!!))
                        setStatus(status)
                    }
                } else if (message.audioUrl != null) {
                    Message(message.id, user, null, date).apply {
                        setAudio(Message.Audio(message.audioUrl!!, 0, getNameFromUrl(message.audioUrl!!)))
                        setStatus(status)
                    }
                } else if (message.voiceUrl != null) {
                    Message(message.id, user, null, date).apply {
                        setVoice(Message.Voice(message.voiceUrl!!, 10000))
                        setStatus(status)
                    }
                } else if (message.docUrl != null) {
                    Message(message.id, user, null, date).apply {
                        val size = getFileSize(message.docUrl!!)
                        setDocument(Message.Document(message.docUrl!!, getNameFromUrl(message.docUrl!!), formatFileSize(size)))
                        setStatus(status)
                    }
                } else {
                    val rawText = message.text ?: ""
                    val displayText = if (rawText.startsWith("circuit://join/group/"))
                        "👥  Group Invite — tap to join"
                    else
                        rawText
                    Message(message.id, user, displayText, date).apply {
                        setStatus(status)

                        //  use stored flag
                        if (message.isSystemMessage) {
                            Log.d("SystemMsg", "text='$rawText' isSystem=true [loadFirstMessages]")
                            setSystemMessage(true)
                        }
                    }
                }
            } as List<Message>

            runOnUiThread {
                messagesAdapter?.addInitialMessages(messages)
            }
        }
    }

    private fun loadMessageList() {
        Handler().post {
            CoroutineScope(Dispatchers.Main).launch {
                val messageList: List<MessageEntity> = messageViewModel.messages(chatId)
                val sortedMessages = messageList.sortedByDescending { it.createdAt }
                val filteredMessages = sortedMessages.filter { !it.deleted }
                TOTAL_MESSAGES_COUNT = filteredMessages.size

                val messages = filteredMessages.filter { message ->

                    // system messages always pass through
                    if (message.isSystemMessage) return@filter true

                    val userId = if (message.userId == myId) "0" else "1"
                    if (userId != "0") return@filter true

                    message.id.startsWith("Image") ||
                            message.id.startsWith("Video") ||
                            message.id.startsWith("Audio") ||
                            message.id.startsWith("Text") ||
                            message.id.startsWith("Doc")
                }.map { message ->
                    val userId = if (message.userId == myId) "0" else "1"
                    val status = message.status
                    val user = User(
                        userId,
                        message.user.name,
                        message.user.avatar,
                        message.user.online,
                        message.user.lastSeen
                    )
                    val date = Date(message.createdAt)

                    if (message.imageUrl != null) {
                        Message(message.id, user, null, date).apply {
                            setImage(Message.Image(message.imageUrl!!))
                            setStatus(status)
                        }
                    } else if (message.videoUrl != null) {
                        Message(message.id, user, null, date).apply {
                            setVideo(Message.Video(message.videoUrl!!))
                            setStatus(status)
                        }
                    } else if (message.audioUrl != null) {
                        Message(message.id, user, null, date).apply {
                            setAudio(Message.Audio(message.audioUrl!!, 0, getNameFromUrl(message.audioUrl!!)))
                            setStatus(status)
                        }
                    } else if (message.voiceUrl != null) {
                        Message(message.id, user, null, date).apply {
                            setVoice(Message.Voice(message.voiceUrl!!, 10000))
                            setStatus(status)
                        }
                    } else if (message.docUrl != null) {
                        Message(message.id, user, null, date).apply {
                            val size = getFileSize(message.docUrl!!)
                            setDocument(Message.Document(message.docUrl!!, getNameFromUrl(message.docUrl!!), formatFileSize(size)))
                            setStatus(status)
                        }
                    } else {
                        val rawText = message.text ?: ""
                        val displayText = if (rawText.startsWith("circuit://join/group/"))
                            "👥  Group Invite — tap to join"
                        else
                            rawText
                        Message(message.id, user, displayText, date).apply {
                            setStatus(status)

                            // use stored flag
                            if (message.isSystemMessage) {
                                Log.d("SystemMsg", "text='$rawText' isSystem=true [loadMessageList]")
                                setSystemMessage(true)
                            }
                        }
                    }
                } as List<Message>

                withContext(Dispatchers.Main) {
                    if (messages.isNotEmpty()) {
                        val filteredMessageList = messages.subList(1, messages.size)
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
        val parts = videoUrl.split("/")
        return parts.last()
    }

    fun getFileSize(filePath: String): Long {
        Log.d("Attachment File Size", "File path to be is : $filePath")
        try {
            val uri = URI.create(filePath)
            if (uri.scheme == "file") {
                val file = File(uri)
                if (file.exists()) {
                    return file.length()
                }
            } else if (uri.scheme == "http" || uri.scheme == "https") {
                return 0L
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        return 0L
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
        val videoExtensions = listOf(
            ".mp4", ".avi", ".mkv", ".mov", ".flv",
            ".webm", ".wmv", ".mpg", ".mpeg", ".3gp",
            ".ogv", ".ogm", ".ts", ".vob", ".m4v",
            ".divx", ".rm", ".rmvb", ".asf", ".m2ts",
            ".mts", ".f4v", ".swf", ".dat", ".yuv",
            ".r3d", ".m2v", ".m1v", ".fla", ".f4p"
        )
        return videoExtensions.any { url?.endsWith(it, true) == true }
    }

    private fun getMessageStringFormatter(): MessagesListAdapter.Formatter<Message> {
        return MessagesListAdapter.Formatter { message ->
            val createdAt = SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                .format(message.createdAt)
            val text = message.text
            String.Companion.format(
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
            !(audioFilePath.startsWith("file://") || audioFilePath.startsWith("/storage/"))
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getCachedOrCalculateAudioDuration(context: Context, audioFilePath: String): Long {
        val preferences: SharedPreferences =
            context.getSharedPreferences("audio_duration_cache", MODE_PRIVATE)
        if (preferences.contains(audioFilePath)) {
            return preferences.getLong(audioFilePath, 0)
        }

        if (audioFilePath.startsWith("file://") || audioFilePath.startsWith("/storage/")) {
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
        return 0
    }

    override fun onDelete(deletedItems: MutableList<String>?) {
        CoroutineScope(Dispatchers.IO).launch {
            if (deletedItems != null) {
                val isLast = deletedItems.contains(lastMessageId)

                if (isGroup) {
                    messageViewModel.markMessagesDeleted(deletedItems)
                } else {
                    try {
                        messageViewModel.deleteMessages(deletedItems)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        if (isLast) {
                            val last = getLastMessage(chatId)
                            if (last != null) {
                                dialogViewModel.updateLastMessageForThisChat(chatId, last)
                            }
                        }
                    }
                }

                if (isLast) {
                    if (isGroup) {
                        val groupDG = groupDialogRepository.getDialog(chatId)
                        val empty = setEmptyMessage(groupDG)
                        groupDialogRepository.updateLastMessageForThisChat(chatId, empty)
                    }
                }
            }
        }
    }

    private fun convertIso8601ToUnixTimestamp(iso8601Date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
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
            id = "DeletedMessage_${Random.Default.nextInt()}",
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
            id = "DeletedMessage_${Random.Default.nextInt()}",
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