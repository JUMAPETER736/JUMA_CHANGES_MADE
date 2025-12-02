package com.uyscuti.social.call.ui

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.call.R
import com.uyscuti.social.call.databinding.ActivityCallBinding
import com.uyscuti.social.call.repository.MainRepository
import com.uyscuti.social.call.service.MainService
import com.uyscuti.social.call.service.MainServiceRepository
import com.uyscuti.social.call.utils.Anima
import com.uyscuti.social.call.utils.RTCAudioManager
import com.uyscuti.social.call.utils.convertToHumanTime
import com.uyscuti.social.call.utils.getCameraAndMicPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class CallActivity : AppCompatActivity() , MainService.EndCallListener,
    MainRepository.AnswerCallListener{

    private var target: String? = null
    private var isVideoCall: Boolean = true
    private var isCaller: Boolean = true
    private var chatId: String? = null
    private var targetUserId: String? = null

    private var isMicrophoneMuted = false
    private var isCameraMuted = false
    private var isSpeakerMode = true
    private var isScreenCasting = false
    private var mediaPlayer: MediaPlayer? = null
    private var callingJob: Job? = null // Declare a Job variable to keep track of the coroutine

    private var callAnswered: Boolean = false

    private lateinit var anima: Anima

    private val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT
    )

    private lateinit var defaultLayoutParams: ViewGroup.LayoutParams

    private lateinit var callingProgressBar: ProgressBar


    @Inject
    lateinit var serviceRepository: MainServiceRepository

    @Inject
    lateinit var mainRepository: MainRepository
    private lateinit var requestScreenCaptureLauncher: ActivityResultLauncher<Intent>

    private lateinit var views: ActivityCallBinding


    private var handlerAnimation = Handler()


    override fun onStart() {
        super.onStart()
        requestScreenCaptureLauncher = registerForActivityResult(
            ActivityResultContracts
                .StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                //its time to give this intent to our service and service passes it to our webrtc client
                MainService.screenPermissionIntent = intent
                isScreenCasting = true
                updateUiToScreenCaptureIsOn()
                serviceRepository.toggleScreenShare(true)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        views = ActivityCallBinding.inflate(layoutInflater)
        setContentView(views.root)


        anima = Anima()

        val targetAvatar = intent.getStringExtra("avatar")

        callingProgressBar = findViewById(R.id.progressBar)

        mainRepository.callListener = this



        Glide.with(this).load(targetAvatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(views.targetImageView)


        getCameraAndMicPermission{
            init()
        }

    }

    private fun getPermissions() {
        TODO("Not yet implemented")
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {
        intent.getStringExtra("target")?.let {
            this.target = it
        } ?: kotlin.run {
            finish()
        }

        chatId = intent.getStringExtra("chatId")
        targetUserId = intent.getStringExtra("userId")

        isVideoCall = intent.getBooleanExtra("isVideoCall", true)
        isCaller = intent.getBooleanExtra("isCaller", true)

        Log.d("CallingActivity", "call activity target: $target")

        isVideoCall = intent.getBooleanExtra("isVideoCall", true)
        isCaller = intent.getBooleanExtra("isCaller", true)

        defaultLayoutParams = views.localView.layoutParams

        if (!isCaller) {
            views.noResponseLayout.visibility = View.GONE
            callingProgressBar.visibility = View.GONE
            views.imgAnimation1.visibility = View.GONE
            views.targetImageView.visibility = View.GONE

            if (!callAnswered) {
                views.endCallButton.setOnClickListener {
                    serviceRepository.sendEndCall()
                }
            }


            views.callTitleTv.text = "In call with $target"
            startTimer()
        } else {
            views.callTitleTv.text = "Video Calling $target"
            views.callTimerTv.visibility = View.GONE

            setupAnimation()

            startCallingTimer()
            startPlayer()
            waitingForAnswer()
        }

        views.apply {
            //callTitleTv.text = "In call with $target"
            if (!isVideoCall) {
                toggleCameraButton.isVisible = false
                screenShareButton.isVisible = false
                switchCameraButton.isVisible = false
                views.callTitleTv.text = "Voice Calling $target"
                views.targetImageView.visibility = View.VISIBLE

                views.callBackgroundImageView.isVisible = true

            }
            MainService.remoteSurfaceView = remoteView
            MainService.localSurfaceView = localView
            serviceRepository.setupViews(isVideoCall, isCaller, target!!)

            endCallButton.setOnClickListener {
                if (!callAnswered && isCaller){
                    Log.d("Caller", "Caller Ended The Call")
                    serviceRepository.sendDeclineCall()
                    serviceRepository.sendEndCall()
                    stopPulse()
                    finish() // Add this line to close the activity
                } else {
                    Log.d("Receiver", "Receiver Ended The Call")
                    serviceRepository.sendEndCall()
                }
            }

            switchCameraButton.setOnClickListener {
                serviceRepository.switchCamera()
            }
        }
        setupMicToggleClicked()
        setupCameraToggleClicked()
        setupToggleAudioDevice()
        setupScreenCasting()
        MainService.endCallListener = this
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private fun startPulse() {
        runnable.run()
    }

    private fun stopPulse() {
        handlerAnimation.removeCallbacks(runnable)
    }

    private var runnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR1)
        override fun run() {

            //Log.i("Animation", "Animate")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                views.imgAnimation1.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(1000)
                    .withEndAction {
                        views.imgAnimation1.scaleX = 1f
                        views.imgAnimation1.scaleY = 1f
                        views.imgAnimation1.alpha = 1f
                    }
            }
            handlerAnimation.postDelayed(this, 1500)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun setupAnimation() {

        val contentResolver: ContentResolver = applicationContext.contentResolver
        val isAnimationEnabled = anima.isAnimationScaleEnabled(contentResolver)

        if (isAnimationEnabled) {
            // Animations are enabled in developer settings.

            startPulse()

        } else {
            // Animations are disabled in developer settings.


            CoroutineScope(Dispatchers.IO).launch {

            }
            showInstructionsDialog()

        }
    }

    private fun showInstructionsDialog() {
        val dialogMessage = """
            To enable Developer Options and turn on animations, follow these steps:
            
            1. Open your device's 'Settings'.
            2. Scroll down and select 'About phone' or 'About device'.
            3. Find 'Build number' and tap on it multiple times (usually 7 times) until you see a message that Developer Options have been enabled.
            4. Now, go back to 'Settings' and you'll find 'Developer Options' or 'System' (depending on your device).
            5. Tap on 'Developer Options'.
            6. Scroll down and find 'Window animation scale', 'Transition animation scale', and 'Animator duration scale'.
            7. Adjust these options to your preference (1x is the default).
            
            Enjoy your enhanced animations!
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Instructions")
            .setMessage(dialogMessage)
            .setPositiveButton("Open Developer Settings") { dialog, _ ->
                // Open Developer Options settings for the user to enable animations.
                val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Dismiss") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setupScreenCasting() {
        views.apply {
            screenShareButton.setOnClickListener {
                if (!isScreenCasting) {
                    //we have to start casting
                    AlertDialog.Builder(this@CallActivity)
                        .setTitle("Screen Casting")
                        .setMessage("You sure to start casting ?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            //start screen casting process
                            startScreenCapture()
                            dialog.dismiss()
                        }.setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }.create().show()
                } else {
                    //we have to end screen casting
                    isScreenCasting = false
                    updateUiToScreenCaptureIsOff()
                    serviceRepository.toggleScreenShare(false)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startScreenCapture() {
        val mediaProjectionManager = application.getSystemService(
            Context.MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        requestScreenCaptureLauncher.launch(captureIntent)
    }

    private fun updateUiToScreenCaptureIsOn() {
        views.apply {
            localView.isVisible = false
            switchCameraButton.isVisible = false
            toggleCameraButton.isVisible = false
            screenShareButton.setImageResource(R.drawable.ic_stop_screen_share)
        }
    }

    private fun updateUiToScreenCaptureIsOff() {
        views.apply {
            localView.isVisible = true
            switchCameraButton.isVisible = true
            toggleCameraButton.isVisible = true
            screenShareButton.setImageResource(R.drawable.ic_screen_share)
        }
    }

    private fun setupMicToggleClicked() {
        views.apply {
            toggleMicrophoneButton.setOnClickListener {
                if (!isMicrophoneMuted) {
                    //we should mute our mic
                    //1. send a command to repository
                    serviceRepository.toggleAudio(true)
                    //2. update ui to mic is muted
                    toggleMicrophoneButton.setImageResource(R.drawable.ic_mic_off)
                } else {
                    //we should set it back to normal
                    //1. send a command to repository to make it back to normal status
                    serviceRepository.toggleAudio(false)
                    //2. update ui
                    toggleMicrophoneButton.setImageResource(R.drawable.ic_mic_on)
                }
                isMicrophoneMuted = !isMicrophoneMuted
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        serviceRepository.sendEndCall()
    }

    private fun setupToggleAudioDevice() {
        views.apply {
            toggleAudioDevice.setOnClickListener {
                if (isSpeakerMode) {
                    //we should set it to earpiece mode
                    toggleAudioDevice.setImageResource(R.drawable.ic_speaker)
                    //we should send a command to our service to switch between devices
                    serviceRepository.toggleAudioDevice(RTCAudioManager.AudioDevice.EARPIECE.name)
                } else {
                    //we should set it to speaker mode
                    toggleAudioDevice.setImageResource(R.drawable.ic_ear)
                    serviceRepository.toggleAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE.name)
                }
                isSpeakerMode = !isSpeakerMode
            }
        }
    }

    private fun setupCameraToggleClicked() {
        views.apply {
            toggleCameraButton.setOnClickListener {
                if (!isCameraMuted) {
                    serviceRepository.toggleVideo(true)
                    toggleCameraButton.setImageResource(R.drawable.ic_camera_on)
                } else {
                    serviceRepository.toggleVideo(false)
                    toggleCameraButton.setImageResource(R.drawable.ic_camera_off)
                }
                isCameraMuted = !isCameraMuted
            }
        }
    }



    override fun onCallEnded() {
        Log.d("socket", "Call Activity onCallEnded")

        stopPulse()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        MainService.remoteSurfaceView?.release()
        MainService.remoteSurfaceView = null
        MainService.localSurfaceView?.release()
        MainService.localSurfaceView = null
        stopPlayer()
        stopPulse()
    }

    fun getView(): ProgressBar {
        return callingProgressBar
    }

    private fun waitingForAnswer() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                views.localView.layoutParams = layoutParams
            }
        }
    }

    private fun resetLocalView() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {


                views.localView.layoutParams = defaultLayoutParams
            }
        }
    }


    private fun startCallingTimer() {
        callingJob?.cancel() // Cancel the previous job if it exists

        callingJob = CoroutineScope(Dispatchers.IO).launch {
            delay(40000)

            stopPlayer()
            withContext(Dispatchers.Main) {
                views.inCallLayout.visibility = View.GONE
                views.noResponseLayout.visibility = View.VISIBLE

                views.unTarget.text = target

                views.unCancel.setOnClickListener {
                    serviceRepository.sendEndCall()
                    finish()
                }

                views.unCallAgain.setOnClickListener {
                    callAgain()
                }

                views.unMessage.setOnClickListener {
                    serviceRepository.sendEndCall()

                    // Make sure chatId is not null or empty
                    val safeChatId = chatId ?: ""
                    val safeTarget = target ?: ""
                    val safeTargetUserId = targetUserId ?: ""
                    val safeAvatar = intent.getStringExtra("avatar") ?: ""

                    if (safeChatId.isEmpty()) {
                        Log.e("CallActivity", "Cannot open MessagesActivity: chatId is empty")
                        Toast.makeText(this@CallActivity, "Unable to open chat", Toast.LENGTH_SHORT).show()
                        finish()
                        return@setOnClickListener
                    }

                    val messageIntent = Intent()
                    messageIntent.setClassName(
                        this@CallActivity,
                        "com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings.MessagesActivity"
                    )
                    messageIntent.putExtra("chatId", safeChatId)
                    messageIntent.putExtra("dialogName", safeTarget)
                    messageIntent.putExtra("dialogPhoto", safeAvatar)
                    messageIntent.putExtra("isGroup", false)
                    messageIntent.putExtra("temporally", false)
                    messageIntent.putExtra("firstUserId", safeTargetUserId)
                    messageIntent.putExtra("firstUserName", safeTarget)
                    messageIntent.putExtra("firstUserAvatar", safeAvatar)

                    Log.d("CallActivity", "Opening MessagesActivity with chatId: $safeChatId")

                    startActivity(messageIntent)
                    finish()
                }

            }
        }
    }

    private fun callingTimer() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(10000)

            stopPlayer()
            withContext(Dispatchers.Main) {
                views.inCallLayout.visibility = View.GONE
                views.noResponseLayout.visibility = View.VISIBLE

                views.unTarget.text = target
                views.unCancel.setOnClickListener {

                    serviceRepository.sendEndCall()

                }
                views.unCallAgain.setOnClickListener {
                    callAgain()
                }

                views.unMessage.setOnClickListener {

                    serviceRepository.sendEndCall()
                }
            }
        }
    }

    private fun callAgain() {
        views.noResponseLayout.visibility = View.GONE
        views.inCallLayout.visibility = View.VISIBLE
        startPlayer()

        startCallingTimer()
    }

    private fun startTimer() {
        CoroutineScope(Dispatchers.IO).launch {
            for (i in 0..3600) {
                delay(1000)
                withContext(Dispatchers.Main) {
                    //convert this int to human readable time
                    views.callTimerTv.text = i.convertToHumanTime()
                }
            }
        }
    }

    override fun onCallAnswered() {
        Log.d("Call", "Call Activity onCallAnswered")
        callingJob?.cancel()

        callAnswered = true

        stopPulse()

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                views.callTitleTv.text = "In Call With $target"
                callingProgressBar.visibility = View.GONE
                views.callTimerTv.visibility = View.VISIBLE
                views.imgAnimation1.visibility = View.GONE

                if (isVideoCall){
                    views.targetImageView.visibility = View.GONE
                }
            }
        }
        resetLocalView()
        startTimer()
        stopPlayer()
    }

    override fun onReceiverBusy() {

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@CallActivity, "$target is busy", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCallDeclined() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@CallActivity, "$target Declined Your Call", Toast.LENGTH_LONG)
                    .show()

            }

            delay(2000)
            finish()
        }
    }

    private fun startPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.incoming)
        }
        mediaPlayer?.start()
    }

    private fun stopPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = null
    }

}