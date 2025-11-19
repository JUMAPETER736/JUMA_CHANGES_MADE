package com.uyscuti.social.call.socket

import android.os.PowerManager
import android.util.Log
import com.google.gson.Gson
import com.uyscuti.social.call.models.DataModel
import com.uyscuti.social.call.models.DataModelType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Exception

@Singleton
class SocketClient @Inject constructor(
    private val gson:Gson,
    private val powerManager: PowerManager, // Inject PowerManager for managing wake locks
){
    private var username:String?=null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isConnected = false
    companion object {
        private var webSocket:WebSocketClient?=null
    }

    var listener: Listener?=null

    fun init(username:String){
        this.username = username

        // Acquire a wake lock to prevent the device from entering deep sleep
        acquireWakeLock()


        webSocket = object : WebSocketClient(URI("ws://echo.flashmobile.app:3000")){
            override fun onOpen(handshakedata: ServerHandshake?) {

                Log.d("SocketClient", "onOpen handShakeData: $handshakedata")
                sendMessageToSocket(
                    DataModel(
                        type = DataModelType.SignIn,
                        username = username,
                        null,
                        null
                    )
                )
            }

            override fun onMessage(message: String?) {
                Log.d("SocketClient", "onMessage: $message")
                val model = try {
                    gson.fromJson(message.toString(), DataModel::class.java)
                }catch (e:Exception){
                    null
                }
                Log.d("SocketClient", "onMessage: $model")
                model?.let {
                    listener?.onNewMessageReceived(it)
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {

                // Release the wake lock
                releaseWakeLock()
                CoroutineScope(Dispatchers.IO).launch {
                    delay(2000)
                    init(username)
//                    if (isConnected.not()) {
//                        init(username)
//                    }
                }
            }

            override fun onError(ex: Exception?) {
            }

        }
        webSocket?.connect()
    }


    private fun acquireWakeLock() {
        if (wakeLock == null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SocketClient::WakeLockTag")
        }

        wakeLock?.apply {
            if (!isHeld) {
                acquire(10*60*1000L /*10 minutes*/)
            }
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.apply {
            if (isHeld) {
                release()
            }
        }
    }

    // Check network connectivity
//    private fun isNetworkConnected(): Boolean {
//        val networkInfo = connectivityManager.activeNetworkInfo
//        return networkInfo != null && networkInfo.isConnected
//    }
//
//    // Public method to check if the WebSocket is connected
//    fun isConnected(): Boolean {
//        return isConnected
//    }

    fun sendMessageToSocket(message:Any?){
        try {
            webSocket?.send(gson.toJson(message))
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun onDestroy(){
        webSocket?.close()
    }

    interface Listener {
        fun onNewMessageReceived(model: DataModel)
    }

    fun logOff(function: () -> Unit) {}
}