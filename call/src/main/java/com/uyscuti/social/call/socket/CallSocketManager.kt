package com.uyscuti.social.call.socket

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import java.net.URISyntaxException

object CallSocketManager {
    private const val SERVER_URL = "http://echo.flashmobile.app:3000"
    private var socket: Socket? = null

//    val socketConnectedLiveData = MutableLiveData<Boolean>()

    private val _socketConnectedLiveData = MutableLiveData<Boolean>()
    val socketConnectedLiveData: LiveData<Boolean>
        get() = _socketConnectedLiveData

    // Initialize the socket connection
    fun initSocket() {
        try {
            val options = IO.Options()
            options.transports = arrayOf(WebSocket.NAME)
            options.forceNew = true // Force a new connection if needed
//            options.query = "token=$token"
            socket = IO.socket(SERVER_URL, options)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }
    // Get the socket instance
    fun getSocket(): Socket? {
        return socket
    }
    // Connect the socket
    fun connectSocket() {
        socket?.connect()
        _socketConnectedLiveData.postValue(true)
    }
    // Disconnect the socket
    fun disconnectSocket() {
        socket?.disconnect()
    }

    interface SocketEventListener {
        fun onSocketConnect()
        // Add more socket event listener methods here
    }
}