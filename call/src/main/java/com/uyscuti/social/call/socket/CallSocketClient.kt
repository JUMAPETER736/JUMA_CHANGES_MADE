package com.uyscuti.social.call.socket

import android.util.Log
import com.google.gson.Gson
import com.uyscuti.social.call.models.DataModel
import com.uyscuti.social.call.models.DataModelType
import com.uyscuti.social.core.local.utils.CoreStorage
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CallSocketClient @Inject constructor(
    private val gson: Gson,
    private val localStorage: CoreStorage,
) {
    val TAG = "CallSocketClient"

    private var username: String = localStorage.getUsername()

    private val connectDeferred = CompletableDeferred<Unit>()

    private lateinit var socket: Socket

    private var onConnectListenerAdded = false
    private var onMessageListenerAdded = false
    private var onListenersAdded = false

    var listener: Listener? = null

    fun init() {
        CallSocketManager.initSocket()
        socket = CallSocketManager.getSocket()!!
        CallSocketManager.connectSocket()
        // Acquire a wake lock to prevent the device from entering deep sleep
//        acquireWakeLock()
        Log.d(TAG, "init socket client, adding listeners")

        if (!onListenersAdded){
            Log.d(TAG, "Listeners Added")
            CoroutineScope(Dispatchers.Main).launch {
//            Log.d(TAG, "init socket observer")
                CallSocketManager.socketConnectedLiveData.observeForever { connected ->
//                Log.d(TAG, "init socket observer")
                    if (connected) {
//                    Log.d(TAG, "call socket connected")
                        if (!onConnectListenerAdded){
                            Log.d(TAG, "on connected listener added")

                            socket.on(Socket.EVENT_CONNECT, onConnect)
                            onConnectListenerAdded = true
                        }
                        if (!onMessageListenerAdded){
                            Log.d(TAG, "on message listener added")

                            socket.on("message", onMessage)
                            onMessageListenerAdded = true
                        }
                        socket.on(Socket.EVENT_DISCONNECT, onDisconnect)
//                    socket.on(Socket.EVENT_CONNECT_ERROR, onError)
                        onListenersAdded = true
                    }
                }
            }
        }

//        CallSocketManager.connectSocket()
    }


//    suspend fun initialize(): Result<Unit> {
//        CallSocketManager.initSocket()
//        socket = CallSocketManager.getSocket() ?: return Result.failure(Exception("Socket is null"))
//        CallSocketManager.connectSocket()
//
//        var isConnected = false // Use a flag to track successful connection
//        val connectError = mutableListOf<Throwable>() // Store any connection errors
//
//        CoroutineScope(Dispatchers.Main).launch {
////            Log.d(TAG, "init socket observer")
//            CallSocketManager.socketConnectedLiveData.observeForever { connected ->
////                Log.d(TAG, "socket connection changed: $connected")
//                isConnected = connected
//
//                if (connected) {
////                    Log.d(TAG, "call socket connected")
//                    socket.on(Socket.EVENT_CONNECT, ){
//                        Log.d(TAG, "Call Socket Connection Successful initializer")
//                        sendMessageToSocket(
//                            "message",
//                            DataModel(
//                                type = DataModelType.SignIn, username = username, null, null
//                            )
//                        )
//                        connectDeferred.complete(Unit) // Return success only after confirmed connection
//                    }
//                    socket.on(Socket.EVENT_DISCONNECT, onDisconnect)
//                    socket.on(Socket.EVENT_CONNECT_ERROR, onError)
////                    socket.on("message", onMessage)
//                }
//            }
//
////            socket.on(Socket.EVENT_CONNECT) {
////                if (!isConnected) {
////                    Log.w(TAG, "Socket connected event received before observer update.")
////                }
////                onConnect
////                isConnected = true
////                connectDeferred.complete(Unit) // Return success only after confirmed connection
////            }
////
////            socket.on(Socket.EVENT_CONNECT_ERROR) { error ->
//////                connectError.add(error)
////                Log.e(TAG, "Socket connection error:  ${error[0]} ")
////
////            }
////
////            socket.on(Socket.EVENT_DISCONNECT) {
////                if (isConnected) {
////                    Log.d(TAG, "Socket disconnected.")
////                    onDisconnect
////                }
////            }
////
////            socket.on("message", onMessage)
//
//            // Handle errors and inform caller after all event listeners are registered
//            if (!isConnected) {
//                val errorMessage = buildString {
//                    appendLine("Socket connection failed.")
//                    if (connectError.isNotEmpty()) {
//                        appendLine("Errors:")
//                        connectError.forEach { appendLine("- $it") }
//                    }
//                }
//            }
//        }
//
//        return try {
//            connectDeferred.await()
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }


    private val onConnect = Emitter.Listener {
        Log.d(TAG, "Call Socket Connection Successful")
        sendMessageToSocket(
            "message",
            DataModel(
                type = DataModelType.SignIn, username = username, target = null, data = null
            )
        )
    }

    private val onError = Emitter.Listener { args ->
        val errorMessage = args[0] as? String ?: "Unknown error"
    }

    private val onDisconnect = Emitter.Listener {
        Log.d(TAG, "Call Socket Connection Closed")
    }

    private val onMessage = Emitter.Listener { args ->
//        val rawMessage = args[0].toString()
//        Log.d("CallSocketClient", "Call Socket Event Message: $rawMessage")

//        val message = JSONObject(rawMessage) // Parse the string to JSONObject

//        Log.d("CallSocketClient", "onMessage json: $message")


        val model = try {
            gson.fromJson(args[0].toString(), DataModel::class.java)
        } catch (e: Exception) {
//            Log.e("CallSocketClient", "model error: ${e.message}")
            null
        }

        Log.d("CallSocketClient", "Call Socket Event Message: $model")
        model?.let {
            listener?.onNewMessageReceived(it)
        }
    }

    fun sendMessageToSocket(event: String, message: Any?) {
        try {
            socket.emit("message", gson.toJson(message))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    interface Listener {
        fun onNewMessageReceived(model: DataModel)
    }
}