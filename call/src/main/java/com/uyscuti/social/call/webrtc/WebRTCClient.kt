package com.uyscuti.social.call.webrtc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.google.gson.Gson
import com.uyscuti.social.call.models.DataModel
import com.uyscuti.social.call.models.DataModelType
import com.uyscuti.social.call.service.ScreenShareService
import com.uyscuti.social.call.webrtc.MySdpObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.webrtc.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCClient @Inject constructor(
    private val context: Context,
    private val gson: Gson
) {
    //class variables
    var listener: Listener? = null
    private lateinit var username: String

    //webrtc variables
    private val eglBaseContext = EglBase.create().eglBaseContext
    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }
    private var peerConnection: PeerConnection? = null
//    private val iceServer = listOf(
//        PeerConnection.IceServer.builder("turn:a.relay.metered.ca:443?transport=tcp")
//            .setUsername("83eebabf8b4cce9d5dbcb649")
//            .setPassword("2D7JvfkOQtBdYW3R").createIceServer()
//    )
//    private val iceServer = listOf(
//
//
//    )

    private val iceServer = listOf(
        PeerConnection.IceServer.builder("turn:turn.flashmobile.app:3478?transport=tcp")
            .setUsername("flashadmin")
            .setPassword("adminflash").createIceServer(),

        PeerConnection.IceServer.builder("stun:iphone-stun.strato-iphone.de:3478")
            .createIceServer(),
        PeerConnection.IceServer("stun:openrelay.metered.ca:80"),
        PeerConnection.IceServer(
            "turn:openrelay.metered.ca:80",
            "openrelayproject",
            "openrelayproject"
        ),
        PeerConnection.IceServer(
            "turn:openrelay.metered.ca:443",
            "openrelayproject",
            "openrelayproject"
        ),
        PeerConnection.IceServer(
            "turn:openrelay.metered.ca:443?transport=tcp",
            "openrelayproject",
            "openrelayproject"
        ),

        )
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }
    private val videoCapturer = getVideoCapturer(context)
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private val mediaConstraint = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
    }

    //call variables
    private lateinit var localSurfaceView: SurfaceViewRenderer
    private lateinit var remoteSurfaceView: SurfaceViewRenderer
    private var localStream: MediaStream? = null
    private var localTrackId = ""
    private var localStreamId = ""
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null

    //screen casting
    private var permissionIntent: Intent? = null
    private var screenCapturer: VideoCapturer? = null
    private val localScreenVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private var localScreenShareVideoTrack: VideoTrack? = null

    //installing requirements section
    init {
        initPeerConnectionFactory()
    }

    private fun initPeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true).setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory.builder()
            .setVideoDecoderFactory(
                DefaultVideoDecoderFactory(eglBaseContext)
            ).setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglBaseContext, true, true
                )
            ).setOptions(PeerConnectionFactory.Options().apply {
                disableNetworkMonitor = true
                disableEncryption = false
            }).createPeerConnectionFactory()
    }

    fun initializeWebrtcClient(
        username: String, observer: PeerConnection.Observer
    ) {
        this.username = username
        localTrackId = "${username}_track"
        localStreamId = "${username}_stream"
        peerConnection = createPeerConnection(observer)
    }

    private fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        return peerConnectionFactory.createPeerConnection(iceServer, observer)
    }

    //negotiation section
    fun call(target: String) {
        Log.d("Socket", "Sending Ice Call")

        peerConnection?.createOffer(object : MySdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                super.onCreateSuccess(desc)
                peerConnection?.setLocalDescription(object : MySdpObserver() {
                    override fun onSetSuccess() {
                        super.onSetSuccess()
                        listener?.onTransferEventToSocket(
                            DataModel(
                                type = DataModelType.Offer,
                                username = username,
                                target = target,
                                data = desc?.description
                            )
                        )
                    }
                }, desc)
            }
        }, mediaConstraint)
    }

    fun answer(target: String) {

        Log.d("Socket", "Sending Ice Answer")

        peerConnection?.createAnswer(object : MySdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                super.onCreateSuccess(desc)
                peerConnection?.setLocalDescription(object : MySdpObserver() {
                    override fun onSetSuccess() {
                        super.onSetSuccess()
                        listener?.onTransferEventToSocket(
                            DataModel(
                                type = DataModelType.Answer,
                                username = username,
                                target = target,
                                data = desc?.description
                            )
                        )
                    }
                }, desc)
            }
        }, mediaConstraint)
    }

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        peerConnection?.setRemoteDescription(MySdpObserver(), sessionDescription)
    }

    fun addIceCandidateToPeer(iceCandidate: IceCandidate) {
        Log.d("Socket", "Adding Ice Candidates")
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun sendIceCandidate(target: String, iceCandidate: IceCandidate) {

        Log.d("Socket", "Sending Ice Candidates")
        addIceCandidateToPeer(iceCandidate)
        listener?.onTransferEventToSocket(
            DataModel(
                type = DataModelType.IceCandidates,
                username = username,
                target = target,
                data = gson.toJson(iceCandidate)
            )
        )
    }

    fun closeConnection() {
        try {
            videoCapturer.dispose()
            screenCapturer?.dispose()
            localStream?.dispose()
            peerConnection?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun switchCamera() {
        videoCapturer.switchCamera(null)
    }
//    fun toggleAudio(shouldBeMuted: Boolean) {
//        if (localAudioTrack != null && localAudioTrack!!.state() != MediaStreamTrack.State.ENDED) {
//            // The audio track is valid and not disposed, so you can manipulate it
//            Log.d("Socket", "Toggling Audio: $shouldBeMuted")
//            localStream?.addTrack(localAudioTrack)
//            localAudioTrack?.setEnabled(!shouldBeMuted)
//        }
//    }

    fun toggleAudio(shouldBeMuted: Boolean) {

//        localStream?.addTrack(localAudioTrack)
//        localAudioTrack?.setEnabled(true)

        if (shouldBeMuted) {
            localStream?.removeTrack(localAudioTrack)
//            peerConnection?.
            Log.d("Socket", "Toggling Audio: true")

//            localAudioTrack?.setEnabled(false)
        } else {
            localStream?.addTrack(localAudioTrack)
            Log.d("Socket", "Toggling Audio: false")
//            localAudioTrack?.setEnabled(true)
        }
    }

    fun toggleVideo(shouldBeMuted: Boolean) {
        try {
            if (shouldBeMuted) {
                stopCapturingCamera()
            } else {

                startCapturingCamera(localSurfaceView)
                localVideoTrack?.setEnabled(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //streaming section
    private fun initSurfaceView(view: SurfaceViewRenderer) {
        view.run {
            setMirror(false)
            setEnableHardwareScaler(true)
            init(eglBaseContext, null)
        }
    }

    fun initRemoteSurfaceView(view: SurfaceViewRenderer) {
        this.remoteSurfaceView = view
        initSurfaceView(view)
    }

    fun initLocalSurfaceView(localView: SurfaceViewRenderer, isVideoCall: Boolean) {
        this.localSurfaceView = localView
        initSurfaceView(localView)
        startLocalStreaming(localView, isVideoCall)
    }

    private fun startLocalStreaming(localView: SurfaceViewRenderer, isVideoCall: Boolean) {
        localStream = peerConnectionFactory.createLocalMediaStream(localStreamId)
        if (isVideoCall) {
            startCapturingCamera(localView)
        }

        localAudioTrack = peerConnectionFactory.createAudioTrack(localTrackId + "_audio", localAudioSource)
        localStream?.addTrack(localAudioTrack)
//        peerConnection?.addTrack(localAudioTrack)
        peerConnection?.addStream(localStream)
//        peerConnection?.addTrack(localAudioTrack)

        CoroutineScope(Dispatchers.IO).launch {
            delay(200)
            localAudioTrack?.setEnabled(true)
        }

    }

    private fun startCapturingCamera(localView: SurfaceViewRenderer) {
        surfaceTextureHelper = SurfaceTextureHelper.create(
            Thread.currentThread().name, eglBaseContext
        )

        videoCapturer.initialize(
            surfaceTextureHelper, context, localVideoSource.capturerObserver
        )
        // Adjust camera capture resolution
        videoCapturer.startCapture(1280, 720, 30) // Example resolution: 1280x720, 30fps

        //videoCapturer?.startCapture(320, 240, 30)
//        videoCapturer.startCapture(
//            720,480,20
//        )

        localVideoTrack =
            peerConnectionFactory.createVideoTrack(localTrackId + "_video", localVideoSource)
        localVideoTrack?.addSink(localView)
        localStream?.addTrack(localVideoTrack)
    }

    private fun getVideoCapturer(context: Context): CameraVideoCapturer =
        Camera2Enumerator(context).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }

    private fun stopCapturingCamera() {
        localVideoTrack?.setEnabled(false)

        val handler = Handler()

        handler.postDelayed({
            videoCapturer.dispose()
            localVideoTrack?.removeSink(localSurfaceView)
            localSurfaceView.clearImage()
            localStream?.removeTrack(localVideoTrack)
            localVideoTrack?.dispose()
        },500)
    }

    //screen capture section

    fun setPermissionIntent(screenPermissionIntent: Intent) {
        this.permissionIntent = screenPermissionIntent
    }

    fun startScreenCapturing() {
        val displayMetrics = DisplayMetrics()
        val windowsManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowsManager.defaultDisplay.getMetrics(displayMetrics)

        val screenWidthPixels = displayMetrics.widthPixels
        val screenHeightPixels = displayMetrics.heightPixels

        val surfaceTextureHelper = SurfaceTextureHelper.create(
            Thread.currentThread().name, eglBaseContext
        )

        val handler = Handler()

        handler.postDelayed({
            screenCapturer = createScreenCapturers()

            screenCapturer!!.initialize(
                surfaceTextureHelper, context, localScreenVideoSource.capturerObserver
            )

            screenCapturer!!.startCapture(screenWidthPixels, screenHeightPixels, 15)

            localScreenShareVideoTrack =
                peerConnectionFactory.createVideoTrack(localTrackId + "_video", localScreenVideoSource)
            localScreenShareVideoTrack?.addSink(localSurfaceView)
            localStream?.addTrack(localScreenShareVideoTrack)
            peerConnection?.addStream(localStream)

        },500)
    }

    fun stopScreenCapturing() {
        screenCapturer?.stopCapture()
        screenCapturer?.dispose()
        localScreenShareVideoTrack?.removeSink(localSurfaceView)
        localSurfaceView.clearImage()
        localStream?.removeTrack(localScreenShareVideoTrack)
        localScreenShareVideoTrack?.dispose()

    }

    fun toggleCamera(cameraPause: Boolean) {
        localVideoTrack?.setEnabled(cameraPause)
    }

    private fun createScreenCapturer(): VideoCapturer {
        val serviceIntent = Intent(context, ScreenShareService::class.java)
        context.startService(serviceIntent)

        val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, permissionIntent!!)

//        return mediaProjection.createVirtualDisplay()
        val mediaProjectionCallback = object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                Log.d("permissions", "onStop: permission of screen casting is stopped")

                // Stop the ScreenShareService when screen casting is stopped
                val stopServiceIntent = Intent(context, ScreenShareService::class.java)
                context.stopService(stopServiceIntent)
            }
        }

        return ScreenCapturerAndroid(
            permissionIntent,
            mediaProjectionCallback
        )
    }




    private fun createScreenCapturers(): VideoCapturer {
//        val serviceIntent = Intent(context, ScreenShareService::class.java)
//        context.startService(serviceIntent)

        return ScreenCapturerAndroid(permissionIntent, object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                Log.d("permissions", "onStop: permission of screen casting is stopped")
                // Stop the ScreenShareService when screen casting is stopped
                val stopServiceIntent = Intent(context, ScreenShareService::class.java)
                context.stopService(stopServiceIntent)
            }
        })
    }


    interface Listener {
        fun onTransferEventToSocket(data: DataModel)
    }
}