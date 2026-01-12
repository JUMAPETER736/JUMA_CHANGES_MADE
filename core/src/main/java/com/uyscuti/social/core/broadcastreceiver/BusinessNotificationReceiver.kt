package com.uyscuti.social.core.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.uyscuti.social.core.models.User
import com.uyscuti.social.core.models.UserData
import java.util.Date

/**
 * BroadcastReceiver to handle notification button clicks
 */
class BusinessNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val intentAction = intent.action
        when (intentAction) {
            "com.uyscuti.social.circuit.VIEW_MORE_BUSINESS" -> {
                handleViewMoreAction(context, intent)
            }
            "com.uyscuti.social.circuit.DISMISS_BUSINESS_AD" -> {
                handleDismissAction(context, intent)
            }
        }
    }

    private fun handleViewMoreAction(context: Context, intent: Intent) {
        val businessId = intent.getStringExtra("business_id") ?: return
        val owner = intent.getSerializableExtra("business_owner") as User
        val notificationId = intent.getIntExtra("notification_id", -1)
        val isFullView = intent.getBooleanExtra("full_view", false)


        val user = UserData(
            owner.userId,
            owner.avatar,
            "",
            false,
            "user",
            owner.username,
            Date()
        )

        val intent = Intent("com.uyscuti.social.circuit.OPEN_VIEW_MORE").apply {
            putExtra("user", user)
            putExtra("notification_id", notificationId)
            setPackage(context.packageName)
        }

        val resolveInfo = context.packageManager.resolveActivity(
            intent,
            0
        )

        if (resolveInfo != null) {
            val explicitIntent = Intent(intent).apply {
                component = ComponentName(
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addNextIntentWithParentStack(explicitIntent)
            stackBuilder.startActivities()
        } else {
            Log.e("BusinessNotificationReceiver", "No activity found to handle intent")
        }

    }

    private fun handleDismissAction(context: Context, intent: Intent) {
        val businessId = intent.getStringExtra("business_id") ?: return
        val notificationId = intent.getIntExtra("notification_id", -1)

        // Dismiss the notification
        if (notificationId != -1) {
            val notificationManager = NotificationManagerCompat.from(context)
            cancelNotification(notificationId,  notificationManager)
        }
    }

    private fun cancelNotification(notificationId: Int,notificationManager: NotificationManagerCompat) {
        notificationManager.cancel(notificationId)
    }
}
