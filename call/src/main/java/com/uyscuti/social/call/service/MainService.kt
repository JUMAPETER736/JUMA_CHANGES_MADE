package com.uyscuti.social.call.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.uyscuti.social.call.R

import com.uyscuti.social.call.models.DataModel
import com.uyscuti.social.call.repository.MainRepository
import com.uyscuti.social.call.socket.Repository
import com.uyscuti.social.call.service.MainServiceActions.*
import com.uyscuti.social.call.ui.CallActivity
import com.uyscuti.social.call.utils.RTCAudioManager
import com.uyscuti.social.core.local.utils.CoreStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.webrtc.MediaStream
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainService : Service(), MainRepository.Listener {

    private val TAG = "MainService"

    private var isServiceRunning = false
    private var username: String? = null

    @Inject
    lateinit var mainRepository: MainRepository

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var localStorage: CoreStorage

//    @Inject
//    lateinit var mainViewModel: MainViewModel

    private lateinit var notificationManager: NotificationManager
    private lateinit var rtcAudioManager: RTCAudioManager
    private var isPreviousCallStateVideo = true

    @Inject
    @Named("chat_notification_compat_builder")
    lateinit var notificationBuilder: NotificationCompat.Builder

    private lateinit var callNotificationManager: NotificationManager

    private var notificationIdCounter = 0


    companion object {
        var listener: Listener? = null
        var endCallListener: EndCallListener? = null
        var localSurfaceView: SurfaceViewRenderer? = null
        var remoteSurfaceView: SurfaceViewRenderer? = null
        var screenPermissionIntent: Intent? = null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate() {
        super.onCreate()
        rtcAudioManager = RTCAudioManager.create(this)
        rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        notificationManager = getSystemService(
            NotificationManager::class.java
        )
        callNotificationManager = getSystemService(NotificationManager::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { incomingIntent ->
            when (incomingIntent.action) {
                START_SERVICE.name -> handleStartService(incomingIntent)
                SETUP_VIEWS.name -> handleSetupViews(incomingIntent)
                END_CALL.name -> handleEndCall()
                SWITCH_CAMERA.name -> handleSwitchCamera()
                TOGGLE_AUDIO.name -> handleToggleAudio(incomingIntent)
                TOGGLE_VIDEO.name -> handleToggleVideo(incomingIntent)
                TOGGLE_AUDIO_DEVICE.name -> handleToggleAudioDevice(incomingIntent)
                TOGGLE_SCREEN_SHARE.name -> handleToggleScreenShare(incomingIntent)
                STOP_SERVICE.name -> handleStopService()
                DECLINE_CALL.name -> handleDeclineCall()
                else -> Unit
            }
        }
        return START_STICKY
    }

    private fun handleStopService() {
        mainRepository.endCall()
        mainRepository.logOff {
            isServiceRunning = false
            stopSelf()
        }
    }

    private fun handleDeclineCall() {
        mainRepository.sendDecline()
    }

    private fun handleToggleScreenShare(incomingIntent: Intent) {
        val isStarting = incomingIntent.getBooleanExtra("isStarting", true)

        Log.d("MainService", "main service handleToggleScreenShare: $isStarting")

        if (isStarting) {
            // we should start screen share
            //but we have to keep it in mind that we first should remove the camera streaming first
            startServiceWithNotification()
            if (isPreviousCallStateVideo) {
                mainRepository.toggleVideo(true)
            }
            mainRepository.setScreenCaptureIntent(screenPermissionIntent!!)
            mainRepository.toggleScreenShare(true)

        } else {
            //we should stop screen share and check if camera streaming was on so we should make it on back again
            mainRepository.toggleScreenShare(false)
            stopForegroundService()
            if (isPreviousCallStateVideo) {
                mainRepository.toggleVideo(false)
            }
        }
    }

    private fun handleToggleAudioDevice(incomingIntent: Intent) {
        val type = when (incomingIntent.getStringExtra("type")) {
            RTCAudioManager.AudioDevice.EARPIECE.name -> RTCAudioManager.AudioDevice.EARPIECE
            RTCAudioManager.AudioDevice.SPEAKER_PHONE.name -> RTCAudioManager.AudioDevice.SPEAKER_PHONE
            else -> null
        }

        Log.d("MainService", "main service handleToggleAudioDevice: ")


        type?.let {
            rtcAudioManager.setDefaultAudioDevice(it)
            rtcAudioManager.selectAudioDevice(it)
            Log.d(TAG, "handleToggleAudioDevice: $it")
        }


    }

    private fun handleToggleVideo(incomingIntent: Intent) {
        val shouldBeMuted = incomingIntent.getBooleanExtra("shouldBeMuted", true)
        this.isPreviousCallStateVideo = !shouldBeMuted
        mainRepository.toggleVideo(shouldBeMuted)
    }

    private fun handleToggleAudio(incomingIntent: Intent) {
        val shouldBeMuted = incomingIntent.getBooleanExtra("shouldBeMuted", true)
        mainRepository.toggleAudio(shouldBeMuted)
    }

    private fun handleSwitchCamera() {
        mainRepository.switchCamera()
    }

    private fun handleEndCall() {
        //1. we have to send a signal to other peer that call is ended
        mainRepository.sendEndCall()
        //2.end out call process and restart our webrtc client
        endCallAndRestartRepository()
    }

    private fun endCallAndRestartRepository() {
        try {

            val name = localStorage.getUsername()
            mainRepository.endCall()
            endCallListener?.onCallEnded()
            mainRepository.initWebrtcClient(name)
        } catch (e: Exception) {
//            throw e
            e.printStackTrace()
        }

    }

    private fun handleSetupViews(incomingIntent: Intent) {
        val isCaller = incomingIntent.getBooleanExtra("isCaller", false)
        val isVideoCall = incomingIntent.getBooleanExtra("isVideoCall", false)
        val target = incomingIntent.getStringExtra("target")
        this.isPreviousCallStateVideo = isVideoCall
        mainRepository.setTarget(target!!)
        //initialize our widgets and start streaming our video and audio source
        //and get prepared for call
        mainRepository.initLocalSurfaceView(localSurfaceView!!, isVideoCall)
        mainRepository.initRemoteSurfaceView(remoteSurfaceView!!)

        Log.d("MainService", "main service handleSetupViews: $isCaller")


        if (!isCaller) {
            //start the video call
            Log.d("Call", "Receiver Answered Call")
            mainRepository.startCall()
        }
    }

    private fun handleStartService(incomingIntent: Intent) {
        //start our foreground service
        if (!isServiceRunning) {
            isServiceRunning = true
            username = incomingIntent.getStringExtra("username")
//            startServiceWithNotification()

            //setup my clients
            mainRepository.listener = this
//            mainRepository.initFirebase()
            mainRepository.initWebrtcClient(username!!)

        }
    }

    private fun startServiceWithNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "channel1", "foreground", NotificationManager.IMPORTANCE_LOW
            )

            val intent = Intent(this, MainServiceReceiver::class.java).apply {
                action = "ACTION_EXIT"
            }
            val pendingIntent: PendingIntent =
                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            notificationManager.createNotificationChannel(notificationChannel)
            val notification = NotificationCompat.Builder(
                this, "channel1"
            ).setSmallIcon(R.drawable.ic_screen_share)
                .setContentTitle("Screen Sharing Started")
                .addAction(R.drawable.ic_end_call, "Exit", pendingIntent)

            startForeground(1, notification.build())
        }
    }

    private fun stopForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.cancel(1) // Cancel the notification with the given ID
            stopForeground(true)
        }
    }


    private fun startCallNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "Call_Notification_Id",
                "CallNotification",
                NotificationManager.IMPORTANCE_HIGH
            )

            callNotificationManager.createNotificationChannel(notificationChannel)

            val target = mainRepository.getTarget()

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

            val callNotification = NotificationCompat.Builder(this, "Call_Notification_Id")
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentTitle("Flash call notification")
                .setContentText("$target is Calling You")
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(contentPendingIntent, true) // Full-screen intent
                .setTimeoutAfter(10 * 1000) // Timeout after 10 seconds

            startForeground(notificationId, callNotification.build())

            CoroutineScope(Dispatchers.IO).launch {
                delay(10000)
                stopForeground(true)
                stopSelf()
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onLatestEventReceived(data: DataModel) {
//        if (data) {
//            when (data.type) {
//                DataModelType.StartVideoCall,
//                DataModelType.StartAudioCall -> {
//                        listener?.onCallReceived(data)
//                }
//                else -> Unit
//            }
//        }
        Log.d("data", "onLatestEventReceived: $data")
    }

    override fun onCallReceived() {
//        startForeground("Call_Notification",notificationBuilder )
//        startCallNotification()
    }


    override fun endCall() {
        //we are receiving end call signal from remote peer
        endCallAndRestartRepository()
    }

    interface Listener {
        fun onCallReceived(model: DataModel)
    }

    interface EndCallListener {
        fun onCallEnded()
    }

    override fun onConnectionRequestReceived(target: String) {
//        listener?.onCallReceived(target)
    }

    override fun onConnectionConnected() {
        TODO("Not yet implemented")
    }

    override fun onCallEndReceived() {
        endCallAndRestartRepository()
    }

    override fun onRemoteStreamAdded(stream: MediaStream) {
        TODO("Not yet implemented")
    }
}