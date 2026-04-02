package com.uyscuti.social.core.pushnotifications


import android.Manifest
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.uyscuti.social.core.R
import com.uyscuti.social.network.api.models.Message


class PushNotificationService : IntentService("NotificationService") {
    private val CHANNEL_ID = "your_channel_id"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val message = intent.getSerializableExtra("message") as Message

            createNotificationChannel()
            showNotification(message)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Chat Notifications Channel"
            val description = "Push Notifications for Chat"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(message: Message) {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_message_24)
            .setContentTitle("New Message")
            .setContentText("${message.sender.username}: ${message.content}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true) // Automatically remove the notification when clicked

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(message._id.hashCode(), notificationBuilder.build())
    }
}
