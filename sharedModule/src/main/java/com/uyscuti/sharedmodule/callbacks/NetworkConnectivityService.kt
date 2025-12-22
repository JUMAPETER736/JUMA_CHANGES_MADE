package com.uyscuti.sharedmodule.callbacks

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.uyscuti.sharedmodule.R
import android.content.pm.ServiceInfo
import androidx.annotation.RequiresApi

class NetworkConnectivityService : Service() {

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("NetworkConnectivityService", "onStartCommand , starting foreground service")

        // IMPORTANT: This service uses mediaProjection type, so:
        // - You MUST request user consent FIRST using MediaProjectionManager.createScreenCaptureIntent()
        //   in the activity that starts this service (e.g., RegisterActivity or MainActivity).
        // - Only start this service AFTER the user grants permission (RESULT_OK).
        // - Without consent, Android 14+ will throw SecurityException even with permissions.

        // Start foreground with the REQUIRED type for mediaProjection
        startForeground(
            NOTIFICATION_ID,
            createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )

        // Optional: Delay stopping foreground mode (e.g., after 20 minutes)
        Handler().postDelayed({
            stopForeground(false)
        }, 20 * 60 * 1000) // 20 minutes in milliseconds

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        // Cleanup code here (e.g., unregister network callbacks)
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val channelId = "network_check_channel"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Flash")
            .setContentText("Connecting...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setDefaults(0)
            .setSilent(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Network Check Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        return notificationBuilder.build()  // Just build and return - no startForeground here!
    }

    companion object {
        const val NOTIFICATION_ID = 1515
    }
}