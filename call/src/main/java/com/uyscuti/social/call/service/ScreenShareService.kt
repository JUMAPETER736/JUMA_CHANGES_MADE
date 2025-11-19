package com.uyscuti.social.call.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.uyscuti.social.call.R

class ScreenShareService : Service() {
    private val NOTIFICATION_ID = 9078

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle onStartCommand logic

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)

        // ...

        return START_STICKY
    }
//
//    override fun onBind(p0: Intent?): IBinder? {
//        TODO("Not yet implemented")
//    }

    override fun onBind(p0: Intent?): IBinder? = null

    // ... Other methods and logic

    private fun createNotification(): Notification {
        val channelId = "screen_share_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Screen Started")
            .setSmallIcon(R.drawable.ic_screen_share)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

        // Create the notification channel (required for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Screen Share Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return notificationBuilder.build()
    }

}
