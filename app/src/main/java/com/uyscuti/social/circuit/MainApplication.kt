package com.uyscuti.social.circuit

import android.os.Looper
import android.util.Log
import androidx.core.os.postDelayed
import androidx.media3.common.util.UnstableApi
import com.uyscuti.sharedmodule.FlashApplication
import com.uyscuti.sharedmodule.ui.fragments.FragmentFactoryRegistry
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.CoreChatSocketClient
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.HiltAndroidApp
import java.util.logging.Handler
import javax.inject.Inject

@UnstableApi @HiltAndroidApp
class MainApplication : FlashApplication() {

    @Inject
    lateinit var clientSocket: CoreChatSocketClient

    @Inject
    lateinit var localStorage: LocalStorage

    override fun onCreate() {
        super.onCreate()
        // Add delay to ensure Hilt is fully initialized
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            if (isUserLoggedIn()) {
                clientSocket.connect()

                // Add this method call after connecting
                android.os.Handler(Looper.getMainLooper()).postDelayed({
                    Log.d("CoreChatSocketClient", "Socket status: ${clientSocket.getConnectionStatus()}")
                    Log.d("CoreChatSocketClient", "Socket ready: ${clientSocket.isReady()}")
                    clientSocket.debugReadiness()
                    clientSocket.debugSocketEvents()
                }, 1000)
            } else {
                clientSocket.disconnect()
            }
        }, 100)
    }

    private fun isUserLoggedIn(): Boolean {
        return localStorage.getToken().isNotEmpty()
    }
}