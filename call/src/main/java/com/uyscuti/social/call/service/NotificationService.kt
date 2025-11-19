package com.uyscuti.social.call.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.uyscuti.social.call.CallMainActivity

import com.uyscuti.social.call.R
import com.uyscuti.social.call.receivers.AnswerCallReceiver
import com.uyscuti.social.call.receivers.DeclineCallReceiver
import com.uyscuti.social.call.repository.MainRepository
import com.uyscuti.social.call.ui.CallActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class NotificationService: Service() {

    private lateinit var callNotificationManager: NotificationManager

    private var notificationIdCounter = 0
    private var missedCallNotificationIdCounter = 0

    @Inject
    lateinit var mainRepository: MainRepository


    override fun onCreate() {
        super.onCreate()

        callNotificationManager = getSystemService(NotificationManager::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent.let { incoming ->
            when (incoming?.action) {
                NotificationServiceActions.ON_INCOMING_CALL.name -> startCallNotification()
            }

        }
        return START_STICKY
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun startCallNotification() {

        Log.d("NotificationService", "startCallNotification Service")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "Call_Notification_Id",
                "CallNotification",
                NotificationManager.IMPORTANCE_HIGH
            )

            callNotificationManager.createNotificationChannel(notificationChannel)

            val target = mainRepository.getTarget()

            val customView = RemoteViews(packageName, R.layout.custom_call_notification)


            val answerIntent = Intent(this,  AnswerCallReceiver::class.java)
            answerIntent.putExtra("message", "message from chat")
            val answerPendingIntent = PendingIntent.getBroadcast(
                this, 0, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            customView.setOnClickPendingIntent(R.id.answer_button, answerPendingIntent)
            val declineIntent = Intent(this, DeclineCallReceiver::class.java)
            answerIntent.putExtra("message", "message from chat")
            val declinePendingIntent = PendingIntent.getBroadcast(
                this, 0, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            customView.setOnClickPendingIntent(R.id.decline_button, declinePendingIntent)

            // Create an Intent to handle the notification click action
            val contentIntent = Intent(this, CallActivity::class.java)
            contentIntent.putExtra("target", target)
            contentIntent.putExtra("isCaller", false)
            contentIntent.putExtra("isVideoCall", true)
            val contentPendingIntent = PendingIntent.getActivity(
                this, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Increment the notification ID counter to make it unique
            notificationIdCounter++
            val notificationId = notificationIdCounter

//            val caller = Person.Builder()
//                .setName(target)
//                .setImportant(true)
//                .build()

            CoroutineScope(Dispatchers.IO).launch {

                val callNotification = NotificationCompat.Builder(this@NotificationService, "Call_Notification_Id")
                    .setSmallIcon(R.drawable.ic_baseline_notifications_24)
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentTitle("$target is Calling You")
                    .setContentText("$target is Calling You")
                    .setCustomContentView(customView)
//                .setStyle(Notification.CallStyle.forIncomingCall(caller, answerPendingIntent, declinePendingIntent))
                    .setCategory(NotificationCompat.CATEGORY_CALL)
//                    .setFullScreenIntent(contentPendingIntent, false) // Full-screen intent
                    .setWhen(System.currentTimeMillis())
//                    .setTimeoutAfter(10 * 1000) // Timeout after 10 seconds

                with(NotificationManagerCompat.from(this@NotificationService)){
//                    notify(notificationId, callNotification.build())
                    startForeground(55, callNotification.build())
                }

                startForeground(55, callNotification.build())
//
                delay(10000)
                stopForeground(true)
                stopSelf()

                showMissedCallNotification()
            }
        }
    }


    private fun showMissedCallNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            missedCallNotificationIdCounter++
            val missedCallId = missedCallNotificationIdCounter

            val target = mainRepository.getTarget()

            val notificationChannel = NotificationChannel(
                "Missed_Call_Notification_Id_$missedCallId",
                "MissedCallNotification",
                NotificationManager.IMPORTANCE_HIGH
            )

            callNotificationManager.createNotificationChannel(notificationChannel)

            // Build the missed call notification
            val builder = NotificationCompat.Builder(this, "Missed_Call_Notification_Id_$missedCallId")
                .setSmallIcon(R.drawable.baseline_call_missed_24)
                .setContentTitle("Missed Call")
                .setContentText("You have a missed call from $target")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            // Create an Intent to handle the notification click action (if needed)
            val intent = Intent(this, CallMainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.setContentIntent(pendingIntent)

            // Build the notification and display it
            val notification = builder.build()
            with(NotificationManagerCompat.from(this@NotificationService)){
                if (ActivityCompat.checkSelfPermission(
                        this@NotificationService,
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
                notify(missedCallId, notification) // Use a different notification ID if needed
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}