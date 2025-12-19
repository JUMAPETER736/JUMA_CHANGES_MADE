package com.uyscuti.social.core.pushnotifications.socket.chatsocket

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException



object ChatSocketManager {
    private const val SERVER_URL = "http://192.168.1.103:8080/"
    private var socket: Socket? = null

//    val socketConnectedLiveData = MutableLiveData<Boolean>()


    val TAG = "ChatSocketManager"

    private val _socketConnectedLiveData = MutableLiveData<Boolean>()
    val socketConnectedLiveData: LiveData<Boolean>
        get() = _socketConnectedLiveData


    // Initialize the socket connection
    fun initSocket(token: String) {
        try {
            val options = IO.Options()
            options.forceNew = true // Force a new connection if needed
            options.query = "token=$token"

            Log.d(TAG, "Creating socket with URL: $SERVER_URL")
            socket = IO.socket(SERVER_URL, options)


            // Set up connection state listeners
            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected successfully")
                _socketConnectedLiveData.postValue(true)
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket disconnected")
                _socketConnectedLiveData.postValue(false)
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Socket connection error: ${args.contentToString()}")
                _socketConnectedLiveData.postValue(false)
            }

            Log.d(TAG, "Socket initialization complete")

        } catch (e: URISyntaxException) {
            e.printStackTrace()
            Log.e(TAG, "Socket initialization failed", e)
        }
    }
    // Get the socket instance
    fun getSocket(): Socket? {
        return socket
    }
    // Connect the socket
    fun connectSocket() {
        Log.d(TAG, "connectSocket() called")

        socket?.let {
            Log.d(TAG, "Socket exists, calling connect()")

            it.connect()
            Log.d(TAG, "connect() method called")

        } ?: Log.e(TAG, "Socket is null!")
    }
    // Disconnect the socket
    fun disconnectSocket() {
        socket?.disconnect()
        _socketConnectedLiveData.postValue(false)
    }

    interface SocketEventListener {
        fun onSocketConnect()
        // Add more socket event listener methods here
    }

}