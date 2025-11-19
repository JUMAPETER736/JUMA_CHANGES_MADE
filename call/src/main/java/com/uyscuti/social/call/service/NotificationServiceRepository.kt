package com.uyscuti.social.call.service

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import javax.inject.Inject

class NotificationServiceRepository @Inject constructor(
    private val context: Context
) {

    fun startCallNotification() {
        val intent = Intent(context, NotificationService::class.java)
        intent.action = NotificationServiceActions.ON_INCOMING_CALL.name
        startServiceIntent(intent)
    }

    fun startCallService(isVideoCall: Boolean) {
        val intent = Intent(context, CallServiceNotification::class.java)
        if (isVideoCall){
            intent.action = NotificationServiceActions.START_VIDEO_CALL_NOTIFICATION.name
        } else {
            intent.action = NotificationServiceActions.START_VOICE_CALL_NOTIFICATION.name
        }
        ContextCompat.startForegroundService(context, intent)
    }


    fun stopCallService() {

    }
    private fun startServiceIntent(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}