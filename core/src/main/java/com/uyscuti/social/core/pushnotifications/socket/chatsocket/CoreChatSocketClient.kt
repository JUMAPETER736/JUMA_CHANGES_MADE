package com.uyscuti.social.core.pushnotifications.socket.chatsocket

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.entity.GroupDialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.core.common.data.room.repository.DialogRepository
import com.uyscuti.social.core.common.data.room.repository.GroupDialogRepository
import com.uyscuti.social.core.common.data.room.repository.MessageRepository
import com.uyscuti.social.core.local.utils.FileType
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.social.FlashNotificationsEvents
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.social.SocialNotificationService
import com.uyscuti.social.network.api.models.Attachment
import com.uyscuti.social.network.api.models.Avatar
import com.uyscuti.social.network.api.models.AvatarX
import com.uyscuti.social.network.api.models.Message
import com.uyscuti.social.network.api.models.Notification
import com.uyscuti.social.network.api.models.Sender
import com.uyscuti.social.network.api.models.User
import com.uyscuti.social.network.api.response.chats.Participant


import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random


@Singleton
class CoreChatSocketClient @Inject constructor(
    private val localStorage: LocalStorage,
    private var retrofitInstance: RetrofitInstance,
    private val context: Context
) {
    private lateinit var socket: Socket
    private var token: String = localStorage.getToken()
    private val myId: String = localStorage.getUserId()

    private var listenersAdded = false
    private var onConnectListenerAdded = false
    private var onMessageListenerAdded = false

    private val onSocketAvailableListeners = mutableListOf<OnSocketAvailableListener>()

    private var messageRepository: MessageRepository = MessageRepository(
        context,
        ChatDatabase.getInstance(context).messageDao(),
        retrofitInstance
    )
    private var dialogRepository: DialogRepository = DialogRepository(
        ChatDatabase.getInstance(context).dialogDao(),
        retrofitInstance,
        localStorage
    )
    private var groupDialogRepository: GroupDialogRepository = GroupDialogRepository(
        ChatDatabase.getInstance(context).groupDialogDao(),
        retrofitInstance,
        localStorage
    )

    val TAG = "CoreChatSocketClient"

    var chatListener: ChatSocketEvents? = null

    private var opened = false

    init {
//        initialize()
    }

    private val socketConnectedObserver = Observer<Boolean> { connected ->
        if (connected) {
            // The socket is connected, you can register listeners and use it here
            socket.on(Socket.EVENT_CONNECT, onConnect)
            socket.on("messageReceived", onMessageReceived)
        }
    }

    fun connect() {
        Log.d(TAG, "connect, socket initializing...............")
        initialize()
    }


    private fun initialize() {
        token.let { ChatSocketManager.initSocket(it) }
        socket = ChatSocketManager.getSocket()!!
        ChatSocketManager.connectSocket()

        // Only add listeners if not already added
        if (!listenersAdded) {
            CoroutineScope(Dispatchers.Main).launch {
                // Listen for socket connection
                ChatSocketManager.socketConnectedLiveData.observeForever { connected ->
                    if (connected) {
                        // The socket is connected, you can register listeners and use it here
//                        socket.on(Socket.EVENT_CONNECT, onConnect)
                        // Add listeners only if not already added
                        if (!onConnectListenerAdded) {
                            socket.on(Socket.EVENT_CONNECT, onConnect)
                            onConnectListenerAdded = true
                        }
                        if (!onMessageListenerAdded){
                            socket.on("messageReceived", onMessageReceived)
                            onMessageListenerAdded = true
                        }
//                       socket.on(Socket.EVENT_DISCONNECT, onDisconnect)
                        socket.on("newChat", onNewChat)
                        socket.on("socketError", onSocketError)
                        socket.on("typing", onTyping)
                        socket.on("stopTyping", onStopTyping)
                        socket.on("updateGroupName", updateGroupName)
                        socket.on("messageDelivered", onMessageDelivered)
                        socket.on("lastSeen", onLastSeenUpdate)
                        socket.on("messageSeen", onMessageOpened)
                        socket.on("downloaded", onDownloaded)
                        socket.on("followed", onFollow)
                        socket.on("postLiked",onLike)
                        socket.on("bookMarked",onBookmarkedPost)
                        socket.on ("onCommentPosted",onComment)
                        socket.on("commentReply",onCommentReplyComment)
                        socket.on("followed", unFollowed)
                        socket.on("friendsSuggestions",friendSuggestions)
                        listenersAdded = true
                    }
                }
            }
        }


//        ChatSocketManager.connectSocket()

//        socket.on(Socket.EVENT_CONNECT, onConnect)
//        socket.on("messageReceived", onMessageReceived)
    }


    fun sendMessageOpenedReport(chatId: String, senderId: String) {
        try {
            // Add this check
            if (!::socket.isInitialized) {
                Log.w(TAG, "Socket not initialized, cannot send message opened report")
                return
            }

            val seenReport = JSONObject().apply {
                put("roomId", chatId)
                put("sender", senderId)
            }
            socket.emit("messageSeen", seenReport)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun unregisterSocketObserver() {
        ChatSocketManager.socketConnectedLiveData.removeObserver(socketConnectedObserver)
    }

    fun disconnect() {
        ChatSocketManager.disconnectSocket()
    }


    private val onBookmarkedPost = Emitter.Listener { args->
        val notificationData= args[0] as JSONObject
        Log.d (TAG,"Notification Data: $notificationData")
        try {
            val _id = notificationData.getString("_id")
            val message = notificationData.getString("message")
            val createdAt = notificationData.getString("createdAt")
            val updatedAt = notificationData.getString("updatedAt")
            val owner = notificationData.getString("owner")
            val link = ""

            val senderUsername = notificationData.getJSONObject("sender").getString("username")
            val senderEmail = notificationData.getJSONObject("sender").getString("email")
            val senderId = notificationData.getJSONObject("sender").getString("_id")

            val avatarUrl = notificationData.getJSONObject("sender").getJSONObject("avatar").getString("url")
            val avatarId = notificationData.getJSONObject("sender").getJSONObject("avatar").getString("_id")
            val avatarLocalPath = ""
            val postId = notificationData.getJSONObject("data").getString("postId")
//            val commentId = notificationData.getJSONObject("data").getString("commentId")
            val note = Notification(
                _id = _id,
                avatar = avatarUrl,
                createdAt = createdAt,
                updatedAt = updatedAt,
                message = message,
                read = false,
                owner = owner,
                postId = postId,
                commentId = "",
                sender = Sender(
                    _id = senderId,
                    email = senderEmail,
                    username = senderUsername.toString(),
                    avatar = AvatarX(
                        _id = avatarId,
                        localPath = avatarLocalPath,
                        url = avatarUrl,
                    ),
                )
            )
            chatListener?.onNotification(note)
            val socialNotificationService = Intent(context, SocialNotificationService::class.java)

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                startForegroundService(context,notificationIntent)
//            } else {
//                context.startService(notificationIntent)
//            }
            CoroutineScope(Dispatchers.Main).launch {
//                context.startService(notificationIntent)
                socialNotificationService.putExtra("notification", note)
                socialNotificationService.action = ChatNotificationServiceActions.ON_ONE_ON_ONE_MESSAGE.name
//                  ContextCompat.startForegroundService(context, notificationIntent)
                ContextCompat.startForegroundService(context, socialNotificationService)
            }
            EventBus.getDefault().post(FlashNotificationsEvents(note.sender.username,note.message,"postBooked","bookMarked",note.createdAt,note.avatar,note._id,note.sender._id,note.read,note.postId,note.commentId))
        } catch (e: JSONException) {
            // Handle JSON parsing errors here
            e.printStackTrace()
        }
    }
    private val onLike = Emitter.Listener { args->
        val notificationData = args[0] as JSONObject
        Log.d(TAG, "Notification Data: $notificationData")
        try {
            val _id = notificationData.getString("_id")
            val message = notificationData.getString("message")
            val createdAt = notificationData.getString("createdAt")
            val updatedAt = notificationData.getString("updatedAt")
            val owner = notificationData.getString("owner")

            val senderUsername = notificationData.getJSONObject("sender").getString("username")
            val senderEmail = notificationData.getJSONObject("sender").getString("email")
            val senderId = notificationData.getJSONObject("sender").getString("_id")

            val avatarUrl = notificationData.getJSONObject("sender").getJSONObject("avatar").getString("url")
            val avatarId = notificationData.getJSONObject("sender").getJSONObject("avatar").getString("_id")
            val avatarLocalPath = ""
            val postId = notificationData.getJSONObject("data").getString("postId")
            val note = Notification(
                _id = _id,
                avatar = avatarUrl,
                createdAt = createdAt,
                updatedAt = updatedAt,
                message = message,
                read = false,
                owner = owner,
                postId = postId,
                commentId = "",
                sender = Sender(
                    _id = senderId,
                    email = senderEmail,
                    username = senderUsername.toString(),
                    avatar =AvatarX(
                        _id = avatarId,
                        localPath = avatarLocalPath,
                        url = avatarUrl,
                    ),
                )
            )
            chatListener?.onNotification(note)
            val socialNotificationService = Intent(context, SocialNotificationService::class.java)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                startForegroundService(context,notificationIntent)
//            } else {
//                context.startService(notificationIntent)
//            }
            CoroutineScope(Dispatchers.Main).launch {
//                context.startService(notificationIntent)
                socialNotificationService.putExtra("notification", note)
                socialNotificationService.action =
                    ChatNotificationServiceActions.ON_ONE_ON_ONE_MESSAGE.name
//                  ContextCompat.startForegroundService(context, notificationIntent)
                ContextCompat.startForegroundService(context, socialNotificationService)
            }
            EventBus.getDefault().post(FlashNotificationsEvents(note.sender.username,note.message,"onLiked","postLiked",note.createdAt,note.avatar,note._id,note.sender._id,note.read, note.postId,note.commentId ))


        } catch (e: JSONException) {
            // Handle JSON parsing errors here
            e.printStackTrace()
        }
    }
    private val onFollow = Emitter.Listener { args ->
        val notificationData = args[0] as JSONObject
        Log.d(TAG, "Notification Data: $notificationData")
        try {
            val _id = notificationData.getString("_id")
            val message = notificationData.getString("message")
            val createdAt = notificationData.getString("createdAt")
            val updatedAt = notificationData.getString("updatedAt")
            val owner = notificationData.getString("owner")

            val senderUsername = notificationData.getJSONObject("sender").getString("username")
            val senderEmail = notificationData.getJSONObject("sender").getString("email")
            val senderId = notificationData.getJSONObject("sender").getString("_id")

            val avatarUrl = notificationData.getJSONObject("sender").getJSONObject("avatar").getString("url")
            val avatarId = notificationData.getJSONObject("sender").getJSONObject("avatar").getString("_id")
            val avatarLocalPath = ""
            val note = Notification(
                _id = _id,
                avatar = avatarUrl,
                createdAt = createdAt,
                updatedAt = updatedAt,
                message = message,
                read = false,
                owner = owner,
                postId = "",
                commentId = "",
                sender = Sender(
                    _id = senderId,
                    email = senderEmail,
                    username = senderUsername.toString(),
                    avatar =AvatarX(
                        _id = avatarId,
                        localPath = avatarLocalPath,
                        url = avatarUrl,
                    ),
                )
            )
            chatListener?.onNotification(note)
            val socialNotificationService = Intent(context, SocialNotificationService::class.java)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                startForegroundService(context,notificationIntent)
//            } else {
//                context.startService(notificationIntent)
//            }
            CoroutineScope(Dispatchers.Main).launch {
//                context.startService(notificationIntent)
                socialNotificationService.putExtra("notification", note)
                socialNotificationService.action =
                    ChatNotificationServiceActions.ON_ONE_ON_ONE_MESSAGE.name
//                  ContextCompat.startForegroundService(context, notificationIntent)
                ContextCompat.startForegroundService(context, socialNotificationService)
            }
            EventBus.getDefault().post(FlashNotificationsEvents(note.sender.username,note.message,"follow","followed",note.createdAt,note.avatar,note._id,note.sender._id,note.read,"","" ))
        } catch (e: JSONException) {
            // Handle JSON parsing errors here
            e.printStackTrace()
        }


    }
    private val friendSuggestions = Emitter.Listener { args ->
        val notificationData = args[0] as JSONObject
        Log.d(TAG, "Socket Connection Successful")

        try {
            // Extracting top-level fields
            val _id = notificationData.getString("_id")
            val message = notificationData.getString("message")
            val createdAt = notificationData.getString("createdAt")
            val updatedAt = notificationData.getString("updatedAt")
            val owner = notificationData.getString("owner")
            val read = notificationData.getBoolean("read")
            val type = notificationData.getString("type")

            // Extracting sender details
            val senderJson = notificationData.getJSONObject("sender")
            val senderUsername = senderJson.getString("username")
            val senderEmail = senderJson.getString("email")
            val senderId = senderJson.getString("_id")
            val senderAvatarJson = senderJson.getJSONObject("avatar")
            val senderAvatarUrl = senderAvatarJson.getString("url")
            val senderAvatarLocalPath = senderAvatarJson.getString("localPath")
            val senderAvatarId = senderAvatarJson.getString("_id")

            // Extracting suggested user details
            val dataJson = notificationData.getJSONObject("data")
            val suggestedUserId = dataJson.getString("suggestedUserId")
            val suggestedUserJson = dataJson.getJSONObject("suggestedUser")
            val suggestedUsername = suggestedUserJson.getString("username")
            val suggestedEmail = suggestedUserJson.getString("email")
            val suggestedAvatarJson = suggestedUserJson.getJSONObject("avatar")
            val suggestedAvatarUrl = suggestedAvatarJson.getString("url")
            val suggestedAvatarLocalPath = suggestedAvatarJson.getString("localPath")
            val suggestedAvatarId = suggestedAvatarJson.getString("_id")

            // Constructing Notification object
            val note = Notification(
                _id = _id,
                avatar = suggestedAvatarUrl, // or any other appropriate field if required
                createdAt = createdAt,
                updatedAt = updatedAt,
                message = message,
                read = read,
                owner = owner,
                postId = "", // Since postId is not used in friend suggestion notifications
                commentId = "", // Since commentId is not used in friend suggestion notifications
                sender = Sender(
                    _id = senderId,
                    email = senderEmail,
                    username = senderUsername,
                    avatar = AvatarX(
                        _id = senderAvatarId,
                        localPath = senderAvatarLocalPath,
                        url = senderAvatarUrl
                    )
                ),
            )

            // Notify the chat listener
            chatListener?.onNotification(note)

            // Handle notification service
            val socialNotificationService = Intent(context, SocialNotificationService::class.java)
            CoroutineScope(Dispatchers.Main).launch {
                socialNotificationService.putExtra("notification", note)
                socialNotificationService.action = ChatNotificationServiceActions.ON_ONE_ON_ONE_MESSAGE.name
                ContextCompat.startForegroundService(context, socialNotificationService)
            }

            // Post event
            EventBus.getDefault().post(
                FlashNotificationsEvents(
                    note.sender.username,
                    note.message,
                    "friendSuggestion",
                    "friendSuggestions",
                    note.createdAt,
                    note.avatar,
                    note._id,
                    note.sender._id,
                    note.read,
                    note.postId,
                    note.commentId
                )
            )

        } catch (e: JSONException) {
            // Handle JSON parsing errors here
            e.printStackTrace()
        }
    }

    private val unFollowed = Emitter.Listener { args ->
        val notificationData = args[0] as JSONObject
        Log.d(TAG,"socket Connection successful ")
        try {
            val _id = notificationData.getString("_id")
            val message = notificationData.getString("message")
            val createdAt = notificationData.getString("createdAt")
            val updatedAt = notificationData.getString("updatedAt")
            val owner = notificationData.getString("owner")

            val senderUsername = notificationData.getJSONObject("sender").getString("username")
            val senderEmail = notificationData.getJSONObject("sender").getString("email")
            val senderId = notificationData.getJSONObject("sender").getString("_id")

            val avatarUrl = notificationData.getJSONObject("sender").getJSONObject("avatar").getString("url")
            val avatarId = notificationData.getJSONObject("sender").getJSONObject("avatar").getString("_id")
            val avatarLocalPath = ""
            val note = Notification(
                _id = _id,
                avatar = avatarUrl,
                createdAt = createdAt,
                updatedAt = updatedAt,
                message = message,
                read = false,
                owner = owner,
                postId = "",
                commentId = "",
                sender = Sender(
                    _id = senderId,
                    email = senderEmail,
                    username = senderUsername.toString(),
                    avatar =AvatarX(
                        _id = avatarId,
                        localPath = avatarLocalPath,
                        url = avatarUrl,
                    ),
                )
            )
            chatListener?.onNotification(note)
            val socialNotificationService = Intent(context, SocialNotificationService::class.java)

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                startForegroundService(context,notificationIntent)
//            } else {
//                context.startService(notificationIntent)
//            }
            CoroutineScope(Dispatchers.Main).launch {
//                context.startService(notificationIntent)
                socialNotificationService.putExtra("notification", note)
                socialNotificationService.action =
                    ChatNotificationServiceActions.ON_ONE_ON_ONE_MESSAGE.name
//                  ContextCompat.startForegroundService(context, notificationIntent)
                ContextCompat.startForegroundService(context, socialNotificationService)
            }

            EventBus.getDefault().post(FlashNotificationsEvents(note.sender.username,note.message,"unfollowed","unfollow",note.createdAt,note.avatar,note._id,note.sender._id,note.read,"",""))


        } catch (e: JSONException) {
            // Handle JSON parsing errors here
            e.printStackTrace()
        }
    }

    private val onCommentReplyComment = Emitter.Listener { args ->
        val notificationData= args[0] as JSONObject
        Log.d ("onCommentReply","socket Connection Successful")

        try {
            val _id = notificationData.getString("_id")
            val message = notificationData.getString("message")
            val createdAt = notificationData.getString("createdAt")
            val updatedAt = notificationData.getString("updatedAt")
            val owner = notificationData.getString("owner")

            val senderUsername = notificationData.getJSONObject("sender").getString("username")
            val senderEmail = notificationData.getJSONObject("sender").getString("email")
            val senderId = notificationData.getJSONObject("sender").getString("_id")

            val avatarUrl = notificationData.getJSONObject("sender").getJSONObject("avatar").getString("url")
            val avatarId = notificationData.getJSONObject("sender").getJSONObject("avatar").getString("_id")
            val avatarLocalPath = ""
            val postId = notificationData.getJSONObject("data").getString("postId")
            val commentId = notificationData.getJSONObject("data").getString("commentId")
            val note = Notification(
                _id = _id,
                avatar = avatarUrl,
                createdAt = createdAt,
                updatedAt = updatedAt,
                message = message,
                read = false,
                owner = owner,
                postId = postId,
                commentId = commentId,
                sender = Sender(
                    _id = senderId,
                    email = senderEmail,
                    username = senderUsername.toString(),
                    avatar = AvatarX(
                        _id = avatarId,
                        localPath = avatarLocalPath,
                        url = avatarUrl,
                    ),
                )
            )
            chatListener?.onNotification(note)
            val socialNotificationService = Intent(context, SocialNotificationService::class.java)

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                startForegroundService(context,notificationIntent)
//            } else {
//                context.startService(notificationIntent)
//            }
            CoroutineScope(Dispatchers.Main).launch {
//                context.startService(notificationIntent)
                socialNotificationService.putExtra("notification", note)
                socialNotificationService.action =
                    ChatNotificationServiceActions.ON_ONE_ON_ONE_MESSAGE.name
//                  ContextCompat.startForegroundService(context, notificationIntent)
                ContextCompat.startForegroundService(context, socialNotificationService)
            }

            EventBus.getDefault().post(FlashNotificationsEvents(note.sender.username,note.message,"onCommentReply","reply",note.createdAt,note.avatar,note._id,note.sender._id,note.read,note.postId,note.commentId))


        } catch (e: JSONException) {
            // Handle JSON parsing errors here
            e.printStackTrace()
        }

    }
    private val onComment = Emitter.Listener { args ->
        val notificationData= args[0] as JSONObject
        Log.d(TAG,"Socket Connection Successful")

        try {
            val _id = notificationData.getString("_id")
            val message = notificationData.getString("message")
            val createdAt = notificationData.getString("createdAt")
            val updatedAt = notificationData.getString("updatedAt")
            val owner = notificationData.getString("owner")
//            val post = notificationData.getString("postId")

            val senderUsername = notificationData.getJSONObject("sender").getString("username")
            val senderEmail = notificationData.getJSONObject("sender").getString("email")
            val senderId = notificationData.getJSONObject("sender").getString("_id")

            val avatarUrl = notificationData.getJSONObject("sender").getJSONObject("avatar").getString("url")
            val avatarId = notificationData.getJSONObject("sender").getJSONObject("avatar").getString("_id")
            val avatarLocalPath = ""
            val postId = notificationData.getJSONObject("data").getString("postId")
            val commentId = notificationData.getJSONObject("data").getString("commentId")

            val note = Notification(
                _id = _id,
                avatar = avatarUrl,
                createdAt = createdAt,
                updatedAt = updatedAt,
                message = message,
                read = false,
                owner = owner,
                postId = postId,
                commentId = commentId,
                sender = Sender(
                    _id = senderId,
                    email = senderEmail,
                    username = senderUsername.toString(),
                    avatar =AvatarX(
                        _id = avatarId,
                        localPath = avatarLocalPath,
                        url = avatarUrl,
                    ),
                )
            )
            chatListener?.onNotification(note)
            val socialNotificationService = Intent(context, SocialNotificationService::class.java)

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                startForegroundService(context,notificationIntent)
//            } else {
//                context.startService(notificationIntent)
//            }
            CoroutineScope(Dispatchers.Main).launch {
//                context.startService(notificationIntent)
                socialNotificationService.putExtra("notification", note)
                socialNotificationService.action =
                    ChatNotificationServiceActions.ON_ONE_ON_ONE_MESSAGE.name
//                  ContextCompat.startForegroundService(context, notificationIntent)
                ContextCompat.startForegroundService(context, socialNotificationService)
            }
            EventBus.getDefault().post(FlashNotificationsEvents(note.sender.username,note.message,"onComment","onCommentPost",note.createdAt,note.avatar,note._id,note.sender._id,note.read,note.postId,note.commentId))
//            val event = FlashNotificationsEvents(note.sender.username,note.message,"onComment","onCommentPost",note.createdAt,note.avatar,note._id,note.sender._id,note.read,note.postId,note.commentId)
        } catch (e: JSONException) {
            // Handle JSON parsing errors here
            e.printStackTrace()
        }
    }

    private val onConnect = Emitter.Listener {
        chatListener?.onSocketConnect()
        Log.d(TAG, "Socket Connection Successful")
    }

    private val onMessageReceived = Emitter.Listener { args ->
        val messageData = args[0] as JSONObject

        Log.d(TAG, "Message Data: $messageData")

        try {
            val messageId = messageData.getString("_id")
            val senderId = messageData.getJSONObject("sender").getString("_id")
            val senderUsername = messageData.getJSONObject("sender").getString("username")
            val senderEmail = messageData.getJSONObject("sender").getString("email")
            val senderAvatarUrl =
                messageData.getJSONObject("sender").getJSONObject("avatar").getString("url")
            val senderAvatarLocalPath =
                messageData.getJSONObject("sender").getJSONObject("avatar").getString("localPath")
            val senderAvatarId =
                messageData.getJSONObject("sender").getJSONObject("avatar").getString("_id")
            val messageContent = messageData.getString("content")
            val chatId = messageData.getString("chat")
            val createdAt = messageData.getString("createdAt")
            val updatedAt = messageData.getString("updatedAt")


            // Extract the attachments array
            val attachmentsArray = messageData.getJSONArray("attachments")

            // Create a list to store attachments
            val attachments = mutableListOf<Attachment>()

            // Iterate through the attachments array and parse each attachment
            for (i in 0 until attachmentsArray.length()) {
                val attachmentData = attachmentsArray.getJSONObject(i)
                val attachment = Attachment(
                    url = attachmentData.getString("url"),
                    localPath = attachmentData.getString("localPath"),
                    _id = attachmentData.getString("_id")
                )
                attachments.add(attachment)
            }

            val date = convertIso8601ToUnixTimestamp(createdAt)
            val lastseen = Date(date)

            val avatar = Avatar(
                _id = senderAvatarId,
                url = senderAvatarUrl,
                localPath = senderAvatarLocalPath
            )

            val user = User(
                _id = senderId,
                avatar = avatar,
                email = senderEmail,
                isEmailVerified = false,
                role = "USER",
                username = senderUsername,
                lastseen = lastseen,
            )

            val message = Message(
                _id = messageId,
                sender = user,
                content = messageContent,
                chat = chatId,
                attachments = attachments,
                createdAt = createdAt,
                updatedAt = updatedAt
            )


            chatListener?.onNewMessage(message)

            updateDB(message)

            Log.d(TAG, "Starting Notification service")


            // Start the NotificationService to show the notification
            val notificationIntent = Intent(context, PushNotificationService::class.java)
            notificationIntent.putExtra("message", message)
//            notificationIntent.putExtra("isGroup", chatId)

            val chatNotificationService = Intent(context, ChatNotificationService::class.java)

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                startForegroundService(context,notificationIntent)
//            } else {
//                context.startService(notificationIntent)
//            }

            CoroutineScope(Dispatchers.Main).launch {
//                context.startService(notificationIntent)
                chatNotificationService.putExtra("message", message)
                chatNotificationService.action =
                    ChatNotificationServiceActions.ON_ONE_ON_ONE_MESSAGE.name
//                ContextCompat.startForegroundService(context, notificationIntent)
                ContextCompat.startForegroundService(context, chatNotificationService)
            }

            sendDeliveryReport(chatId, senderId)
            sendAcknowledgement(myId, messageId)


        } catch (e: JSONException) {
            // Handle JSON parsing errors here
            e.printStackTrace()
        }
//        val message = messageData.getString("message")
//        chatListener?.onNewMessage("There is a New Message")
    }

    private suspend fun updateLastMessage(chatId: String, imageMessage: MessageEntity) {
//        if (group){
//            val dialog = groupDialogRepository.getDialog(chatId)
//            groupDialogRepository.updateLastMessage(dialog,imageMessage)
//        } else {
//            val dialog = dialogRepository.getDialog(chatId)
//            dialogRepository.updateLastMessage(dialog,imageMessage)
//        }

        try {
            val dialog = groupDialogRepository.getDialog(chatId)
            groupDialogRepository.updateLastMessage(dialog, imageMessage)
        } catch (e: Exception) {
            // Handle the exception, and try getting the dialog from dialogRepository
            e.printStackTrace()
            try {
                val dialog = dialogRepository.getDialog(chatId)
                dialogRepository.updateLastMessage(dialog, imageMessage)
            } catch (e: Exception) {
                // Handle the exception from dialogRepository, if needed
                e.printStackTrace() // or log the exception
            }
        }

    }

    private suspend fun updateLastMessageForGroup(chatId: String, imageMessage: MessageEntity) {
        try {
            val dialog = groupDialogRepository.getDialog(chatId)
            groupDialogRepository.updateLastMessage(dialog, imageMessage)
        } catch (e: Exception) {
            // Handle the exception, and try getting the dialog from dialogRepository
            e.printStackTrace()
        }
    }

    private suspend fun updateLastMessageForDialog(chatId: String, imageMessage: MessageEntity) {
        try {
            val dialog = dialogRepository.getDialog(chatId)
            dialogRepository.updateLastMessage(dialog, imageMessage)
        } catch (e: Exception) {
            // Handle the exception from dialogRepository, if needed
            e.printStackTrace() // or log the exception
        }
    }

//    private fun insertNewChat(chatId: String, isGroup: Boolean){
//        try {
//            if (isGroup){
//                groupDialogRepository.insertNewChat(chatId)
//            } else {
//                dialogRepository.insertDialog()
//            }
//        } catch (e:Exception){
//            e.printStackTrace()
//        }
//    }

//    private suspend fun chatExists(chatId: String): Boolean {
//        val dialog = groupDialogRepository.checkGroup(chatId) ?: dialogRepository.checkDialog(chatId)
//        return dialog != null
//    }


    private suspend fun chatExists(chatId: String): Pair<Boolean, Boolean> {
        val groupDialog = groupDialogRepository.checkGroup(chatId)
        val regularDialog = dialogRepository.checkDialog(chatId)

        return when {
            groupDialog != null -> Pair(true, true) // Found a group dialog
            regularDialog != null -> Pair(true, false) // Found a regular dialog
            else -> Pair(false, false) // Dialog not found
        }
    }


    private fun updateDB(message: com.uyscuti.social.network.api.models.Message) {
        CoroutineScope(Dispatchers.IO).launch {

            delay(50)
            val dialogToUpdate = message.chat

            val (isChatFound, isGroup) = chatExists(dialogToUpdate)

            val notId = Random.nextInt()
            val messageEnt: MessageEntity = message.toMessageEntity()
            insertMessage(messageEnt)

            // Initialize URLs as null
            var imageUrl: String? = null
            var audioUrl: String? = null
            var videoUrl: String? = null
            var docUrl: String? = null

            var text = ""
            var senderName = ""

//            try {
//                val dialog = groupDialogRepository.getDialog(dialogToUpdate)
//
////            Log.d(TAG, "dialog to update: $dialog")
//
//                senderName = message.sender.username
//
//                if (dialog.users.size > 1) {
//                    text = if (senderName.isNotEmpty()) {
//                        "$senderName: " // Highlighted sender's name
//                    } else {
//                        "Anonymous:"
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }


            // Handle attachments and assign URLs
            if (message.attachments != null && message.attachments?.isNotEmpty() == true) {
                val attachments = message.attachments
                if (attachments != null) {
                    for (attachment in attachments) {
                        when (getFileType(attachment.url)) {
                            FileType.IMAGE -> {
                                imageUrl = attachment.url
                                Log.d(TAG, "Image, Path Of Image Received: $imageUrl")
                                text += "📷 Image"
                            }

                            FileType.AUDIO -> {
                                audioUrl = attachment.url
                                Log.d(TAG, "Audio, Path Of Image Received: $audioUrl")
//                                audioList.add(audioUrl)
                                text += "🎵 Audio"
                            }

                            FileType.VIDEO -> {
                                videoUrl = attachment.url
                                Log.d(TAG, "Video, Path Of Image Received: $videoUrl")
                                text += "🎬 Video"
                            }

                            FileType.DOCUMENT -> {
                                docUrl = attachment.url
                                Log.d(TAG, "Document, Path Of Image Received: $docUrl")
                                text += "📄 Document"
                            }

                            FileType.OTHER -> {
                                // Handle other types, if needed
                            }
                        }
                    }
                }
            } else {
                text += message.content
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

            if (isChatFound) {
                if (isGroup) {
                    // The chat is found, and it is a group
                    println("Chat found, and it's a group.")
                    updateLastMessageForGroup(dialogToUpdate, messageEntity)
                } else {
                    // The chat is found, but it is not a group
                    println("Chat found, but it's not a group.")
                    updateLastMessageForDialog(dialogToUpdate, messageEntity)
                }
            } else {
                // The chat is not found
                println("Chat not found.")
                fetchAndInsertNewDialog(dialogToUpdate, messageEntity)
            }


//            updateLastMessage(dialogToUpdate, messageEntity)
//            dialogRepository.incrementUnreadCount(dialogToUpdate)
//            dialogRepository.updateLastMessage(dialog, messageEntity)
        }
    }

    private suspend fun fetchAndInsertNewDialog(chatId: String, lastMessageEntity: MessageEntity) {
        var filteredUsers: List<UserEntity> = emptyList()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitInstance.apiService.fetchChat(chatId)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.data != null) {
                        val chat = responseBody.data
                        val users = chat.participants.map { it.toUserEntity() }

                        val firstUser = users.first { it.id != myId }

                        var chatName = ""
                        var chatAvatar = ""

                        var text = ""
                        var senderName = ""

                        if (!chat.isGroupChat) {
//                                    firstUser = users.firstOrNull { it.id != userId }
                            chatName = firstUser.name
                                ?: "" // Set dialogName to the name of the first user or an empty string if there are no users.
                            filteredUsers = users.filter { user -> user.id != myId }
                            chatAvatar = filteredUsers.firstOrNull()?.avatar ?: ""

                            val dialog = DialogEntity(
                                id = chat._id,
                                dialogPhoto = chatAvatar,
                                dialogName = chatName,
                                users = filteredUsers,
                                lastMessage = lastMessageEntity,
                                unreadCount = 0 // Set appropriate initial unread count
                            )
                            insertDialogEntity(dialog)

                        } else {

                            filteredUsers = users.filter { user -> user.id != myId }
                            val groupAvatar = filteredUsers.firstOrNull()?.avatar ?: ""
                            val groupName = chat.name

                            var adminName = ""

                            // Find the admin user based on the adminId
                            val adminUser = users.find { it.id == chat.admin }
                            val createdAt = convertIso8601ToUnixTimestamp(chat.createdAt)
                            val updatedAt = convertIso8601ToUnixTimestamp(chat.updatedAt)

                            // Check if the admin user is found
                            adminName = adminUser?.name ?: "Admin"

                            val groupEntity = GroupDialogEntity(
                                id = chat._id,
                                adminId = chat.admin,
                                adminName = adminName,
                                dialogPhoto = groupAvatar,
                                dialogName = groupName,
                                users = filteredUsers,
                                lastMessage = lastMessageEntity,
                                unreadCount = 0,
                                createdAt = createdAt,
                                updatedAt = updatedAt
                            )
                            insertGroupEntity(groupEntity)
                        }

                    }
                } else {
                    println("Error fetching chat: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun Participant.toUserEntity(): UserEntity {
        return UserEntity(
            id = _id,
            name = username,
            avatar = avatar.url, // Assuming avatar.imageUrl is the string representation of the avatar
            lastSeen = Date(),
            online = false // Set online status as needed
        )
    }

    private fun insertGroupEntity(groupDialogEntity: GroupDialogEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            groupDialogRepository.insertGroupDialog(groupDialogEntity)
        }
    }

    private fun insertDialogEntity(dialogEntity: DialogEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            dialogRepository.insertDialog(dialogEntity)
        }
    }

    private fun insertMessage(message: MessageEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            messageRepository.insertMessage(message)
        }
    }

    private fun updateMessageStatus(status: String, messages: List<MessageEntity>) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("Socket", "Messages Size :${messages.size}")
            messages.map {
                it.status = status
                messageRepository.updateMessage(it)
            }
            delay(300)
            opened = false
        }
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

    private fun com.uyscuti.social.network.api.models.Message.toMessageEntity(): MessageEntity {

        val createdAt = convertIso8601ToUnixTimestamp(createdAt)


        // Initialize URLs as null
        var imageUrl: String? = null
        var audioUrl: String? = null
        var videoUrl: String? = null
        var docUrl: String? = null
        val size: Long = 0

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

    private fun convertIso8601ToUnixTimestamp(iso8601Date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")

        val date = sdf.parse(iso8601Date)
        return date?.time ?: 0
    }

    private fun User.toUserEntity(): UserEntity {
        return UserEntity(
            id = _id,
            name = username,
            avatar = avatar.url,
            online = false, // Set online status as needed
            lastSeen = lastseen
        )
    }

    interface ChatSocketEvents {
        fun onSocketConnect()
        fun onNewMessage(message: Message)
        fun onDeliveryReport()
        fun onNotification(notification: Notification)
        fun onMessageOpenedReport()
    }

    // Example listener for the NEW_CHAT_EVENT
    private val onNewChat = Emitter.Listener { args ->
        // Handle the new chat event
        val chatData = args[0] as JSONObject
        Log.d("Socket", "New Chat Event: $chatData")
        // Add your logic to process the new chat event
    }


    // Example listener for the NEW_CHAT_EVENT
    private val onTyping = Emitter.Listener { args ->
        // Handle the new chat event
        val chatData = args[0] as JSONObject
        Log.d("Socket", "on Typing Event: $chatData")
        // Add your logic to process the new chat event
    }

    private val onStopTyping = Emitter.Listener { args ->
        // Handle the new chat event
        val chatData = args[0] as JSONObject
        Log.d("Socket", "on stop Typing Event: $chatData")
        // Add your logic to process the new chat event
    }

    private val onMessageDelivered = Emitter.Listener { args ->
        if (args.isNotEmpty()) {
            val arg = args[0]
            if (arg is JSONObject) {
                // Handle the new chat event
                Log.d("Socket", "on Message Delivered Event: $arg")
                // Add your logic to process the new chat event
            } else if (arg is String) {
                // If it's a string, you might want to handle it accordingly
                Log.d("Socket", "Received a String payload: $arg")
                // Add your logic to process the String payload
                chatListener?.onDeliveryReport()

//                handleChange(arg,"Delivered")

            }
        }
    }

    private val onDownloaded = Emitter.Listener { args ->
        if (args.isNotEmpty()) {
            Log.d("Socket", "on Downloaded Event: $args")
        }
    }


    private val onMessageOpened = Emitter.Listener { args ->
        opened = true
        if (args.isNotEmpty()) {
            val arg = args[0]
            if (arg is JSONObject) {
                // Handle the new chat event
                Log.d("Socket", "on Message Delivered Event: $arg")
                // Add your logic to process the new chat event
            } else if (arg is String) {
                // If it's a string, you might want to handle it accordingly
                Log.d("Socket", "Message Opened By Receiver: $arg")
                // Add your logic to process the String payload
                chatListener?.onMessageOpenedReport()

                // Create a Handler within the context of the main thread
                Handler(Looper.getMainLooper()).postDelayed({
                    handleChange(arg, "Seen")
                }, 1000)
            }
        }
    }

    private fun handleChange(chatId: String, status: String) {
        CoroutineScope(Dispatchers.IO).launch {
            messageRepository.processPendingMessages(chatId)

//            Log.d("Socket", "Loaded Messages Size : ${messages.size}")
//
//            if (messages.isNotEmpty()){
//                updateMessageStatus(status,messages)
//            }
        }
    }


    // Example listener for the SOCKET_ERROR_EVENT
    private val onSocketError = Emitter.Listener { args ->
        // Handle the socket error event
        val errorMessage = args[0] as String
        Log.e("Socket", "Socket Error: $errorMessage")
        // Add your logic to handle socket errors
    }

    private val updateGroupName = Emitter.Listener { args ->
        val message = args[0] as JSONObject
        Log.d("Socket", "updateGroupName: $message")
    }

    private val onLastSeenUpdate = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        Log.d("Socket", "on Last Seen Info: $data")
    }



    fun observeSocketAvailability(listener: OnSocketAvailableListener) {
        onSocketAvailableListeners.add(listener)
    }

    interface OnSocketAvailableListener {
        fun onSocketAvailable(socket: Socket?)
    }


    fun sendTypingEvent(chatId: String) {
        // Add initialization check
        if (!::socket.isInitialized) {
            Log.w(TAG, "Socket not initialized, cannot send typing event")
            return
        }

        val typingData = JSONObject().apply {
            put("chatId", chatId)
        }
        socket.emit("typing", typingData)
    }

    fun sendStopTypingEvent(chatId: String) {
        // Add initialization check
        if (!::socket.isInitialized) {
            Log.w(TAG, "Socket not initialized, cannot send stop typing event")
            return
        }

        val stopTypingData = JSONObject().apply {
            put("chatId", chatId)
        }
        socket.emit("stopTyping", stopTypingData)
    }

    fun sendDeliveryReport(chatId: String, senderId: String) {
        // Add initialization check
        if (!::socket.isInitialized) {
            Log.w(TAG, "Socket not initialized, cannot send delivery report")
            return
        }

        val delivery = JSONObject().apply {
            put("roomId", chatId)
            put("sender", senderId)
        }
        socket.emit("messageDelivered", delivery)
    }

    private fun sendAcknowledgement(userId: String, messageId: String) {
        // Add initialization check
        if (!::socket.isInitialized) {
            Log.w(TAG, "Socket not initialized, cannot send acknowledgement")
            return
        }

        val acknowledgment = JSONObject().apply {
            put("messageId", messageId)
            put("userId", userId)
        }
        socket.emit("messageAck", acknowledgment)
    }

    fun sendDownLoadedEvent(userId: String, messageId: String) {
        // Add initialization check
        if (!::socket.isInitialized) {
            Log.w(TAG, "Socket not initialized, cannot send downloaded event")
            return
        }

        val downloaded = JSONObject().apply {
            put("messageId", messageId)
            put("userId", userId)
        }
        socket.emit("downloaded", downloaded)
    }

}

