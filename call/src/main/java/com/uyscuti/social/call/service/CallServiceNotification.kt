package com.uyscuti.social.call.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.uyscuti.social.call.CallMainActivity
import com.uyscuti.social.call.R

import com.uyscuti.social.call.repository.MainRepository
import com.uyscuti.social.call.ui.CallActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random
import com.uyscuti.social.call.service.NotificationServiceActions.*
import com.uyscuti.social.core.local.utils.CoreStorage


@AndroidEntryPoint
class CallServiceNotification : Service(), MainRepository.AnswerCallListener {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var notificationManager: NotificationManager
    private var mediaPlayer: MediaPlayer? = null

    @Inject
    lateinit var mainRepository: MainRepository

    @Inject
    lateinit var localStorage: CoreStorage

    @Inject
    lateinit var serviceRepository: MainServiceRepository

    private var missedCallNotificationIdCounter = 0


    var userClicked: Boolean = false

    override fun onBind(p0: Intent?): IBinder? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // Service is connected
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Service is disconnected
            serviceBound = false
        }
    }

    private var serviceBound: Boolean = false // Flag to track service binding

    override fun onCreate() {
        super.onCreate()

        mainRepository.callListener = this

        notificationManager = getSystemService(NotificationManager::class.java)

        // Check if the service is already running
//        if (isServiceRunning()) {
//            stopSelf()
////            return
//        }

        // Bind to the service
        val serviceIntent = Intent(this, CallServiceNotification::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { cm ->
            when (cm.action) {
                START_VIDEO_CALL_NOTIFICATION.name -> start(true)
                START_VOICE_CALL_NOTIFICATION.name -> start(false)
            }
        }

        return START_STICKY
    }

//    private fun isServiceRunning(): Boolean {
//        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val services = manager.getRunningServices(Int.MAX_VALUE)
//
//        for (service in services) {
//            if (CallServiceNotification::class.java.name == service.service.className) {
//                return true
//            }
//        }
//
//        return false
//    }

    private fun isServiceRunning(): Boolean {
        return serviceBound
    }


    override fun onDestroy() {
        super.onDestroy()
//        windowManager.removeView(overlayView)
//        unbindService(serviceConnection)

        // Unbind from the service if it was bound
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
        stopForeground(true)
//        stopSelf()
    }

    private fun start(isVideoCall: Boolean) {
//        CoroutineScope(Dispatchers.Main).launch {
//            Toast.makeText(applicationContext,"Call", Toast.LENGTH_SHORT).show()
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "your_channel_id",
                "Your Channel Name",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(notificationChannel)

            startPlayer()

            val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            val notification: Notification = NotificationCompat
                .Builder(this, "your_channel_id")
                .setSmallIcon(R.drawable.baseline_videocam_black)
                .setContentTitle("Calling")
                .setContentText("Incoming call")
                .build()

            startForeground(757, notification)

            overlayView = LayoutInflater.from(this).inflate(R.layout.custom_call_notification, null)
            // Handle the "Answer" action here
//            Log.d("TAG", "Answer Clicked")//

            val target = mainRepository.getTarget()
            val callAvatar = localStorage.getThisCallAvatar()

//            Log.d("TAG", "Call Target $target")


            val params = LayoutParams(
                LayoutParams.MATCH_PARENT,
                150,
                LayoutParams.TYPE_APPLICATION_OVERLAY,
                LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
//                LayoutParams.FLAG_NOT_TOUCHABLE or LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.RGB_565,
            )

            // Set the gravity to position the overlay at the top of the screen
//            params.gravity = Gravity.TOP or Gravity.CENTER

//            params.y =

            params.gravity = Gravity.TOP
//            params.verticalMargin  = 0.02f

            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.addView(overlayView, params)

            overlayView.findViewById<Button>(R.id.answer_button).setOnClickListener {
                userClicked = true
                stopPlayer()
                stopForeground(true)
                stopSelf()

                val flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Add these flags

                Log.d("Incoming", "Answer Clicked")
                val answerIntent = Intent(this, CallActivity::class.java)
                answerIntent.putExtra("message", "message from chat")
                answerIntent.putExtra("target", target)
                answerIntent.putExtra("avatar", callAvatar)
                answerIntent.putExtra("isVideoCall", isVideoCall)
                answerIntent.putExtra("isCaller", false)
                answerIntent.flags = flags
                startActivity(answerIntent)
                windowManager.removeView(overlayView)

//                userClicked = true
            }

            overlayView.findViewById<Button>(R.id.decline_button).setOnClickListener {
                Log.d("Incoming", "Decline Clicked")
                windowManager.removeView(overlayView)
                userClicked = true
                localStorage.clearThisCallAvatar()

                serviceRepository.sendDeclineCall()

                stopForeground(true)
                stopSelf()
//                userClicked = true
                stopPlayer()
//                showMissedCallNotification()
            }


            CoroutineScope(Dispatchers.IO).launch {
                delay(40000)

                if (!userClicked) {
                    windowManager.removeView(overlayView)
                    stopForeground(true)
                    stopSelf()
                    stopPlayer()
                    showMissedCallNotification()
                }
            }
        }
    }

    private fun showMissedCallNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Generate a random notification ID
            val notificationChannelId = Random.nextInt()

            missedCallNotificationIdCounter++
            val missedCallId = missedCallNotificationIdCounter

            val target = mainRepository.getTarget()

            val notificationChannel = NotificationChannel(
                "Missed_Call_Notification_Id_$missedCallId",
                "MissedCallNotification",
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(notificationChannel)

            // Build the missed call notification
            val builder =
                NotificationCompat.Builder(this, "Missed_Call_Notification_Id_$missedCallId")
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
            with(NotificationManagerCompat.from(this@CallServiceNotification)) {
                if (ActivityCompat.checkSelfPermission(
                        this@CallServiceNotification,
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
                notify(
                    notificationChannelId,
                    notification
                ) // Use a different notification ID if needed
            }
        }
    }


    private fun startPlayer() {
        val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        if (mediaPlayer == null) {
//            mediaPlayer = MediaPlayer.create(this, R.raw.incoming)
            mediaPlayer = MediaPlayer.create(this, defaultRingtoneUri)

            mediaPlayer?.setOnCompletionListener{
//                startPlayer()
            }
            

//            try {
//                // Set the data source to the default ringtone URI
//                mediaPlayer?.setDataSource(this, defaultRingtoneUri)
//
//                // Prepare the MediaPlayer
//                mediaPlayer?.prepare()
//
//                // Start playing the ringtone
//                mediaPlayer?.start()
//
//                // Add an optional listener to handle when the ringtone finishes playing
//                mediaPlayer?.setOnCompletionListener {
//                    // Handle completion here if needed
//                    mediaPlayer?.release()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
        }
        mediaPlayer?.start()
    }

    private fun stopPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCallAnswered() {}

    override fun onReceiverBusy() {}

    override fun onCallDeclined() {

        userClicked = true

        stopPlayer()
//        Log.d("CallService", "onCallDeclined")
        stopForeground(true)
        stopSelf()
        try {
            windowManager.removeView(overlayView)
        } catch (e:Exception){
            e.printStackTrace()
        }
        showMissedCallNotification()
    }
}