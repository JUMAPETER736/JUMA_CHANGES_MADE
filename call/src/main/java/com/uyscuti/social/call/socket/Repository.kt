package com.uyscuti.social.call.socket

import android.content.Intent


import com.google.gson.Gson
import com.uyscuti.social.call.models.DataModel
import com.uyscuti.social.call.utils.UserStatus
import com.uyscuti.social.call.webrtc.MyPeerObserver
import com.uyscuti.social.call.webrtc.WebRTCClient
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val socketClient: SocketClient,
    private val webRTCClient: WebRTCClient,
    private val gson: Gson
) : WebRTCClient.Listener, SocketClient.Listener
{

    private var target: String? = null
    var listener: Listener? = null
    private var remoteView: SurfaceViewRenderer?=null

    fun init(username: String, surfaceView: SurfaceViewRenderer) {
        socketClient.listener = this
        socketClient.init(username)
//        initWebrtcClient()

    }

//    fun login(username: String, password: String, isDone: (Boolean, String?) -> Unit) {
//        firebaseClient.login(username, password, isDone)
//    }

    fun observeUsersStatus(status: (List<Pair<String, String>>) -> Unit) {
//        firebaseClient.observeUsersStatus(status)
    }


    fun sendConnectionRequest(target: String, isVideoCall: Boolean, success: (Boolean) -> Unit) {
//        firebaseClient.sendMessageToOtherClient(
//            DataModel(
//                type = if (isVideoCall) DataModelType.StartVideoCall else DataModelType.StartAudioCall,
//                target = target
//            ), success
//        )
    }

    fun setTarget(target: String) {
        this.target = target
    }

    interface Listener {
        fun onLatestEventReceived(data: DataModel)
        fun endCall()
    }

    fun initWebrtcClient(username: String) {
        webRTCClient.listener = this
        webRTCClient.initializeWebrtcClient(username, object : MyPeerObserver() {

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                try {
                    p0?.videoTracks?.get(0)?.addSink(remoteView)
                }catch (e:Exception){
                    e.printStackTrace()
                }

            }

            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                p0?.let {
                    webRTCClient.sendIceCandidate(target!!, it)
                }
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                super.onConnectionChange(newState)
                if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                    // 1. change my status to in call
                    changeMyStatus(UserStatus.IN_CALL)
                    // 2. clear latest event inside my user section in firebase database
//                    firebaseClient.clearLatestEvent()
                }
            }
        })
    }

    fun initLocalSurfaceView(view: SurfaceViewRenderer, isVideoCall: Boolean) {
        webRTCClient.initLocalSurfaceView(view, isVideoCall)
    }

    fun initRemoteSurfaceView(view: SurfaceViewRenderer) {
        webRTCClient.initRemoteSurfaceView(view)
        this.remoteView = view
    }

    fun startCall() {
        webRTCClient.call(target!!)
    }

    fun endCall() {
        webRTCClient.closeConnection()
        changeMyStatus(UserStatus.ONLINE)
    }

//    fun sendEndCall() {
//        onTransferEventToSocket(
//            DataModel(
//                type = DataModelType.EndCall,
//                target = target!!
//            )
//        )
//    }

    private fun changeMyStatus(status: UserStatus) {
//        firebaseClient.changeMyStatus(status)
    }

    fun toggleAudio(shouldBeMuted: Boolean) {
        webRTCClient.toggleAudio(shouldBeMuted)
    }

    fun toggleVideo(shouldBeMuted: Boolean) {
        webRTCClient.toggleVideo(shouldBeMuted)
    }

    fun switchCamera() {
        webRTCClient.switchCamera()
    }

    override fun onTransferEventToSocket(data: DataModel) {
//        firebaseClient.sendMessageToOtherClient(data) {}
    }

    fun setScreenCaptureIntent(screenPermissionIntent: Intent) {
        webRTCClient.setPermissionIntent(screenPermissionIntent)
    }

    fun toggleScreenShare(isStarting: Boolean) {
        if (isStarting){
            webRTCClient.startScreenCapturing()
        }else{
            webRTCClient.stopScreenCapturing()
        }
    }

    override fun onNewMessageReceived(model: DataModel) {
        TODO("Not yet implemented")
    }

//    fun logOff(function: () -> Unit) = socketClient.logOff(function)
//    override fun onNewMessageReceived(model: com.codewithkael.webrtcprojectforrecord.socket.DataModel) {
//        TODO("Not yet implemented")
//    }
//

}