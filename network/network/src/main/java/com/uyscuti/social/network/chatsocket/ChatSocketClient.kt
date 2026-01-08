package com.uyscuti.social.network.chatsocket

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.Observer
import com.uyscuti.social.network.api.models.Attachment
import com.uyscuti.social.network.api.models.Avatar
import com.uyscuti.social.network.api.models.Message
import com.uyscuti.social.network.api.models.User
import com.uyscuti.social.network.utils.LocalStorage
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ChatSocketClient @Inject constructor(
    private val localStorage: LocalStorage,
    private val context: Context
) {

    private lateinit var socket: Socket
    private var token: String = localStorage.getToken()


//    private lateinit var messageRepository: MessageRepository

    val TAG = "ChatSocketClient"

    var chatListener: ChatSocketEvents? = null

    init {
        initialize()
    }

    private val socketConnectedObserver = Observer<Boolean> { connected ->
        if (connected) {
            // The socket is connected, you can register listeners and use it here
            socket.on(Socket.EVENT_CONNECT, onConnect)
            socket.on("messageReceived", onMessageReceived)
        }
    }




    private fun initialize() {
        token.let { ChatSocketManager.initSocket(it) }
        socket = ChatSocketManager.getSocket()!!
        ChatSocketManager.connectSocket()

        // Listen for socket connection
        ChatSocketManager.socketConnectedLiveData.observeForever { connected ->
            if (connected) {
                // The socket is connected, you can register listeners and use it here
                socket.on(Socket.EVENT_CONNECT, onConnect)
                socket.on("messageReceived", onMessageReceived)
            }
        }

        ChatSocketManager.connectSocket()

//        socket.on(Socket.EVENT_CONNECT, onConnect)
//        socket.on("messageReceived", onMessageReceived)
    }

    fun unregisterSocketObserver() {
        ChatSocketManager.socketConnectedLiveData.removeObserver(socketConnectedObserver)
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

            val lastDate = convertIso8601ToUnixTimestamp(createdAt)

            val lastseen = Date(lastDate)


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

            Log.d(TAG,"Starting Notification service")


            // Start the NotificationService to show the notification
            val notificationIntent = Intent(context, PushNotificationService::class.java)
            notificationIntent.putExtra("message", message)
            notificationIntent.putExtra("isGroup", chatId)
//            ContextCompat.startForegroundService(context, notificationIntent)
            context.startService(notificationIntent)

        } catch (e: JSONException) {
            // Handle JSON parsing errors here
            e.printStackTrace()
        }
//        val message = messageData.getString("message")
//        chatListener?.onNewMessage("There is a New Message")
    }

    private fun convertIso8601ToUnixTimestamp(iso8601Date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")


        val date = sdf.parse(iso8601Date)
        return date?.time ?: 0
    }

    interface ChatSocketEvents {
        fun onSocketConnect()
        fun onNewMessage(message: Message)
    }
}

