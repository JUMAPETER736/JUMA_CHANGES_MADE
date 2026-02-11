package com.uyscuti.social.core.pushnotifications.socket.chatsocket.social

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import androidx.core.content.getSystemService
import com.uyscuti.social.core.R
import com.uyscuti.social.core.local.utils.CoreStorage
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.ChatNotificationServiceActions.ON_GROUP_MESSAGE
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.ChatNotificationServiceActions.ON_ONE_ON_ONE_MESSAGE
import com.uyscuti.social.network.api.models.Notification


class SocialNotificationService : Service() {


    companion object {
        private var instance: SocialNotificationService? = null

        fun getInstance(context: Context): SocialNotificationService? {
            if (instance == null) {
                instance = (context.applicationContext as? ContextWrapper)?.baseContext?.getSystemService() as? SocialNotificationService
            }
            return instance
        }

    }

    private val CHANNEL_ID = "FlashSocialNotifications"
    val foregroundNotificationId = 5757

    private var isForegroundStarted = false


    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(p0: Intent?): IBinder? = null

    var localStorage: CoreStorage? = null
    override fun onCreate() {
        super.onCreate()
        localStorage = CoreStorage.getInstance(applicationContext)
        createNotificationChannel()
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(!isForegroundStarted) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                stopSelf()
                return START_NOT_STICKY // Can't show notification, permissions need to be requested from Activity
            }

            startForeground(foregroundNotificationId, createMinimalServiceNotification())
            isForegroundStarted = true

            handler.postDelayed({
                stopForeground(true)
                stopSelf()
            }, 2 * 1000)
        }



        intent?.let { cm ->
            val socialNotification = cm.getSerializableExtra("notification") as Notification

            when (cm.action) {
                ON_ONE_ON_ONE_MESSAGE.name -> showNotification(socialNotification)
                ON_GROUP_MESSAGE.name -> showGroup()
                else -> {}
            }
        }

        return START_NOT_STICKY
    }

    private fun showGroup() {

    }


    @SuppressLint("ObsoleteSdkInt")
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Social Notifications Channel"
            val description = "Push Notifications for Social"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(socialNotification: Notification) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            return // Can't show notification, permissions need to be requested from Activity
        }
        val currentTimeMillis = System.currentTimeMillis()

        val notificationId = "notification_id_${socialNotification._id}".hashCode()

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(com.uyscuti.social.notifications.R.drawable.ic_launcher_foreground)
            .setContentTitle(socialNotification.sender.username)
            .setContentText(socialNotification.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true) // Automatically remove the notification when clicked
            .setOngoing(false) // Allows swiping to dismiss
            .setWhen(currentTimeMillis)


        // This is safe - won't interfere with foreground notification
        NotificationManagerCompat.from(this).notify(notificationId, notificationBuilder.build())
    }

    private fun createMinimalServiceNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Circuit")
            .setContentText("New notifications")
            .setSmallIcon(com.uyscuti.social.core.R.drawable.ic_notification) // Use a very small, subtle icon
            .setPriority(NotificationCompat.PRIORITY_MIN) // Even lower than LOW
            .setOngoing(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setDefaults(0)
            .setSilent(true)
            .build()
    }


    private fun notificationBuilder(): NotificationCompat.Builder {

        return NotificationCompat.Builder(applicationContext, "Social_Notification")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)


    }



}