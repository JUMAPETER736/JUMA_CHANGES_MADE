package com.uyscuti.social.circuit.callbacks

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
import com.uyscuti.social.circuit.R

class NetworkConnectivityService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Your network callback registration code here
        Log.d("NetworkConnectivityService", "onStartCommand , starting foreground service")

        // Start service in the foreground
        startForeground(NOTIFICATION_ID, createNotification())

        // Delay stopping the foreground service for 20 minutes
        Handler().postDelayed({
            stopForeground(false)
        }, 20 * 60 * 1000) // 20 minutes in milliseconds

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        // Your cleanup code here
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val channelId = "network_check_channel"
        val notificationId = 123 // Choose any unique ID for your notification

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Flash")
            .setContentText("Connecting...")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Adjust priority as needed
            .setVisibility(NotificationCompat.VISIBILITY_SECRET) // Set visibility to secret
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setDefaults(0)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a NotificationChannel for Android Oreo and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Network Check Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create the notification and display it
        val notification = notificationBuilder.build()
        startForeground(notificationId, notification)

        return notification
    }


    companion object {
        const val NOTIFICATION_ID = 1515
    }
}
