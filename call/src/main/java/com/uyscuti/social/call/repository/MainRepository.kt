package com.uyscuti.social.call.repository

import android.content.Intent
import android.util.Log
import com.google.gson.Gson

import com.uyscuti.social.call.models.DataModel
import com.uyscuti.social.call.models.DataModelType
import com.uyscuti.social.call.representation.CallViewHandler
import com.uyscuti.social.call.representation.MainViewModel
import com.uyscuti.social.call.service.NotificationServiceRepository
import com.uyscuti.social.call.socket.CallSocketClient
import com.uyscuti.social.call.socket.SocketClient
import com.uyscuti.social.call.utils.UserStatus
import com.uyscuti.social.call.webrtc.MyPeerObserver
import com.uyscuti.social.call.webrtc.WebRTCClient
import com.uyscuti.social.core.common.data.room.entity.CallLogEntity
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.repository.DialogRepository
import com.uyscuti.social.core.local.utils.CoreStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

sealed class CallResult {
    data class Success(val message: String) : CallResult()
    data class Failure(val errorMessage: String) : CallResult()
}

@Singleton
class MainRepository @Inject constructor(
    private val socketClient: SocketClient,
    private val callSocketClient: CallSocketClient,
    private val webRTCClient: WebRTCClient,
    private val gson: Gson
) : SocketClient.Listener, WebRTCClient.Listener, CallSocketClient.Listener {

    private var target: String? = null
    private var username: String? = null
    var listener: Listener? = null
    private var remoteView: SurfaceViewRenderer? = null
    var callListener: AnswerCallListener? = null
    private var receiverBusy: Boolean = false

    private var myTarget: String? = null

    @Inject
    lateinit var notificationServiceRepository: NotificationServiceRepository

    @Inject
    lateinit var dialogRepository: DialogRepository

    @Inject
    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var localStorage: CoreStorage

    @Inject
    lateinit var callViewModel: CallViewHandler

    val TAG = "MainRepository"
//    interface Listener {
//        fun onLatestEventReceived(data: DataModel)
//        fun endCall()
//    }

//    suspend fun init(username: String):  Result<Unit> {
////        socketClient.listener = this
//        callSocketClient.listener = this
//        //        socketClient.init(username)
//        return callSocketClient.initialize()
//    }

    fun init(username: String){
//        socketClient.listener = this
        callSocketClient.listener = this
        //        socketClient.init(username)
        callSocketClient.init()
    }

//    suspend fun init(username: String): CallResult {
//        callSocketClient.listener = this
//        val initializationResult = callSocketClient.initialize()
//        return if (initializationResult.isSuccess) {
//            CallResult.Success("Initialization successful")
//        } else {
//            CallResult.Failure("Failed to initialize")
//        }
//    }

    fun setTarget(value: String) {
        target = value
    }

    fun setUserName(value: String){
        username = value
    }

    fun getUserName(): String? {
        return username
    }

    fun getTarget(): String? {
        return target
    }

    fun initWebrtcClient(username: String) {
        webRTCClient.listener = this
        Log.d(TAG, "Webrtc Initialized: ")

        CoroutineScope(Dispatchers.IO).launch {
            webRTCClient.initializeWebrtcClient(username, object : MyPeerObserver() {

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    try {
                        p0?.videoTracks?.get(0)?.addSink(remoteView)
                    } catch (e: Exception) {
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
    }

    fun initLocalSurfaceView(view: SurfaceViewRenderer, isVideoCall: Boolean) {
        webRTCClient.initLocalSurfaceView(view, isVideoCall)
    }

    fun initRemoteSurfaceView(view: SurfaceViewRenderer) {
        webRTCClient.initRemoteSurfaceView(view)
        this.remoteView = view
    }

    fun startCall() {
        Log.d(TAG, "startCall: ")
        webRTCClient.call(target!!)
//        callListener?.onCallAnswered()
    }

    fun endCall() {
        webRTCClient.closeConnection()
        changeMyStatus(UserStatus.ONLINE)
    }

    private suspend fun checkUserExists(username: String): DialogEntity? {
        return dialogRepository.checkDialogByName(username)
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

//    override fun onTransferEventToSocket(data: DataModel) {
////        firebaseClient.sendMessageToOtherClient(data) {}
//    }

    fun setScreenCaptureIntent(screenPermissionIntent: Intent) {
        webRTCClient.setPermissionIntent(screenPermissionIntent)
    }

    fun toggleScreenShare(isStarting: Boolean) {
        if (isStarting) {
            webRTCClient.startScreenCapturing()
        } else {
            webRTCClient.stopScreenCapturing()
        }
    }

    fun sendEndCall() {
        if (!receiverBusy){
            onTransferEventToSocket(
                DataModel(
                    type = DataModelType.EndCall,
                    target = target!!,
                    username = username!!,
                    data = null,
                )
            )
        } else {
            onTransferEventToSocket(
                DataModel(
                    type = DataModelType.EndCall,
                    target = "",
                    username = username!!,
                    data = null,
                )
            )
        }
    }

    fun sendDecline(){
        onTransferEventToSocket(
            DataModel(
                type = DataModelType.DeclineCall,
                target = target!!,
                username = "",
                data = null,
            )
        )
    }

    fun logOff(function: () -> Unit) = socketClient.logOff(function)


    override fun onNewMessageReceived(model: DataModel) {
//        var callProgressBar = callActivity?.getView()
        Log.d("snapshot", "snapshot receiver global: $model")
        when (model.type) {
            DataModelType.StartVideoCall -> {
                Log.d("snapshot", "snapshot receiver 101 : ${model.username} "  )
                this.target = model.username
//                this.username = model.target

                Log.d("snapshot", "snapshot receiver : ${model.username}")
//                mainViewModel.showSimpleNotification(model.target!!)
                logCall(true,model.username)

                listener?.onConnectionRequestReceived(model.username)

                notificationServiceRepository.startCallService(true)
                listener?.onCallReceived()
            }

            DataModelType.StartVoiceCall -> {
                this.target = model.username
//                this.username = model.target

                Log.d("IceCandidates", "snapshot receiver : ${model.username}")

                logCall(false,model.username)
//                mainViewModel.showSimpleNotification(model.target!!)
                //Toast.makeText(, "${model.username}", Toast.LENGTH_SHORT).show()
                //notify ui, conneciton request is being made, so show it
                listener?.onConnectionRequestReceived(model.username)

                notificationServiceRepository.startCallService(false)
                listener?.onCallReceived()
            }

            DataModelType.Offer -> {
                Log.d("IceCandidates", "Offer Received")
                webRTCClient.onRemoteSessionReceived(
                    SessionDescription(
                        SessionDescription.Type.OFFER,
                        model.data.toString()
                    )
                )
                webRTCClient.answer(target!!)
                callListener?.onCallAnswered()
            }

            DataModelType.EndCall -> {
                //notify ui call is ended
                Log.d("snapshot", "model end call : ${model.username}")
                listener?.onCallEndReceived()
                localStorage.clearThisCallAvatar()
            }

            DataModelType.DeclineCall -> {
                //notify ui call is ended
                Log.d("snapshot", "model decline call : ${model.username}")
                callListener?.onCallDeclined()
                localStorage.clearThisCallAvatar()
            }
//
            DataModelType.Answer -> {
                Log.d("IceCandidates", "Answer Received")
//                callProgressBar?.visibility = View.GONE
                webRTCClient.onRemoteSessionReceived(
                    SessionDescription(
                        SessionDescription.Type.ANSWER,
                        model.data.toString()
                    )
                )
//                callListener?.onCallAnswered()
            }

            DataModelType.ReceiverInCall -> {
                Log.d("IceCandidates", "Receiver Is In A Call")
                receiverBusy = true
//                callListener?.onReceiverBusy()
                CoroutineScope(Dispatchers.IO).launch {
                    delay(1000)
                    callListener?.onReceiverBusy()
                    delay(3000)
                    listener?.onCallEndReceived()
                }
            }
//
            DataModelType.IceCandidates -> {

                Log.d("IceCandidates", "IceCandidates Received")
                val candidate = try {
                    gson.fromJson(model.data.toString(), IceCandidate::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                candidate?.let {
                    webRTCClient.addIceCandidateToPeer(it)
                }
            }

            else -> Unit
        }
    }

    private fun logCall(isVideo: Boolean, username: String){
        CoroutineScope(Dispatchers.IO).launch {
            val dialogEntity = checkUserExists(username)

            if (dialogEntity != null){
                val callAvatar = dialogEntity.dialogPhoto
                localStorage.setThisCallAvatar(callAvatar)

                val newCallLog = CallLogEntity(
                    id = Random.nextLong(),
                    callerName = username,
                    System.currentTimeMillis(),
                    callDuration = 0,
                    "Incoming",
                    "Not Answered",
                    dialogEntity.dialogPhoto,
                    dialogEntity.id,
                    isVideo,
                    false
                )
                insertCallLog(newCallLog)
            }
        }
    }

    private fun insertCallLog(callLog: CallLogEntity){
        callViewModel.insertCallLog(callLog)
    }

    fun sendConnectionRequest(message: DataModel, success: (Boolean) -> Unit) {
        socketClient.sendMessageToSocket(message)
        if (message.type == DataModelType.StartVideoCall){
            callSocketClient.sendMessageToSocket("StartVideoCall", message)
        } else {
            callSocketClient.sendMessageToSocket("StartVoiceCall", message)

        }
//        callSocketClient.sendMessageToSocket(message.type.toString(),message)
        success(true)
    }

    interface Listener {
        fun onConnectionRequestReceived(target: String)
        fun onConnectionConnected()
        fun onCallEndReceived()
        fun onRemoteStreamAdded(stream: MediaStream)
        fun endCall()
        fun onLatestEventReceived(data: DataModel)
        fun onCallReceived()
    }

    interface AnswerCallListener {
        fun onCallAnswered()
        fun onReceiverBusy()
        fun onCallDeclined()
    }

    override fun onTransferEventToSocket(data: DataModel) {
        Log.d("Socket", "New Socket Event")
//        socketClient.sendMessageToSocket(data)
        callSocketClient.sendMessageToSocket(data.type.toString(), data)
    }

}
