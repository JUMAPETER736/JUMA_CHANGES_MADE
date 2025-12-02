package com.uyscuti.social.circuit.calls

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.circuit.calls.viewmodel.CallViewModel
import com.uyscuti.social.circuit.presentation.DialogViewModel
import com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings.MessagesActivity
import com.uyscuti.social.circuit.User_Interface.media.ViewImagesActivity
import com.uyscuti.social.call.models.DataModel
import com.uyscuti.social.call.models.DataModelType
import com.uyscuti.social.call.repository.MainRepository
import com.uyscuti.social.call.ui.CallActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.data.model.Dialog
import com.uyscuti.social.circuit.data.model.Message
import com.uyscuti.social.circuit.data.model.User
import com.uyscuti.social.circuit.databinding.ActivityCallInfoBinding
import com.uyscuti.social.core.common.data.room.entity.CallLogEntity
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import kotlin.random.Random


@AndroidEntryPoint
class CallInfoActivity: AppCompatActivity(){

    private lateinit var binding: ActivityCallInfoBinding
    private lateinit var caller: String
    private lateinit var date: String
    private lateinit var duration: String
    private lateinit var time: String
    private lateinit var type: String
    private lateinit var status: String
    private lateinit var avatar: String
    private lateinit var callerId: String

    private lateinit var username: String

    // Store dialog info for calls
    private var dialogId: String? = null

    private val dialogViewModel: DialogViewModel by viewModels()

    private lateinit var callViewModel: CallViewModel

    @Inject
    lateinit var mainRepository: MainRepository

    @Inject
    lateinit var localStorage: LocalStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCallInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        caller = intent.getStringExtra("caller").toString()
        date = intent.getStringExtra("date").toString()
        duration = intent.getStringExtra("duration").toString()
        time = intent.getStringExtra("time").toString()
        type = intent.getStringExtra("type").toString()
        status = intent.getStringExtra("status").toString()
        avatar = intent.getStringExtra("avatar").toString()
        callerId = intent.getStringExtra("callerId").toString()

        callViewModel = ViewModelProvider(this)[CallViewModel::class.java]

        setSupportActionBar(binding.toolbar)

        username = localStorage.getUsername()

        val title = "Call Info"

        supportActionBar?.title = title;
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationIcon(R.drawable.baseline_white_arrow_back_24)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        if (type == "Incoming") {
            if (status == "Answered"){
                binding.callType.setColorFilter(ContextCompat.getColor(this, R.color.green_dark), PorterDuff.Mode.SRC_IN)
            } else {
                binding.callType.setColorFilter(ContextCompat.getColor(this, R.color.red), PorterDuff.Mode.SRC_IN)
            }
            binding.callType.setImageResource(R.drawable.baseline_call_received_24)
        } else {
            binding.callType.setImageResource(R.drawable.baseline_call_made_24)
        }

        if (duration == "0 secs" && type == "Incoming"){
            val missed = "Missed"
            binding.callDuration.visibility = View.GONE
            binding.callState.text = missed
        } else {
            binding.callDuration.visibility = View.VISIBLE
            binding.callDuration.text = duration
            binding.callState.text = type
        }

        Glide.with(this).load(avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.callerAvatar)

        binding.callerName.text = caller
        binding.callDate.text = date
        binding.callCreatedAt.text = time

        binding.callerAvatar.setOnClickListener {
            viewImage(avatar, caller)
        }
        setListeners(callerId)

        // FIX: Load dialog info on startup
        loadDialogInfo(callerId)
    }

    // FIX: Add method to load and cache dialog info
    private fun loadDialogInfo(callerId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val callerDialog = dialogViewModel.getDialog(callerId)
                dialogId = callerDialog?.id
                Log.d("CallInfoActivity", "Loaded dialog ID: $dialogId for caller: $caller")
            } catch (e: Exception) {
                Log.e("CallInfoActivity", "Error loading dialog info", e)
            }
        }
    }

    private fun setListeners(callerId: String){
        binding.messageCaller.setOnClickListener {
            messageCaller(callerId)
        }

        binding.voiceCallCaller.setOnClickListener {
            startVoiceCall()
        }

        binding.videoCallCaller.setOnClickListener {
            startVideoCall()
        }
    }

    private fun messageCaller(callerId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("CallerDialog", "CallerId $callerId")

            val callerDialog = dialogViewModel.getDialog(callerId)

            Log.d("CallerDialog", "$callerDialog")

            if (callerDialog != null) {
                withContext(Dispatchers.Main) {
                    val dialog = fromDialogEntity(callerDialog)
                    openMessages(dialog)
                }
            }
        }
    }

    private fun viewImage(url: String, name:String){
        val intent = Intent(this, ViewImagesActivity::class.java)
        intent.putExtra("imageUrl", url)
        intent.putExtra("owner", name)
        startActivity(intent)
    }

    private fun fromDialogEntity(entity: DialogEntity): Dialog {
        val users = convertUserEntitiesToUsers(entity.users)

        val usersList: List<User> = users
        val usersArrayList: ArrayList<User> = ArrayList(usersList)

        val lastMessage = entity.lastMessage?.let { convertMessageEntityToMessage(it) }

        return Dialog(
            entity.id,
            entity.dialogName,
            entity.dialogPhoto,
            usersArrayList,
            lastMessage,
            entity.unreadCount
        )
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun openMessages(dialog: Dialog) {
        val temporally = dialog.id == dialog.dialogName
        MessagesActivity.open(this, dialog.dialogName, dialog, temporally)
        resetUnreadCount(dialog)
    }

    private fun resetUnreadCount(dialog: Dialog) {
        CoroutineScope(Dispatchers.IO).launch {
            val dg = dialogViewModel.getDialog(dialog.id)
            dg.unreadCount = 0
            dialogViewModel.updateDialog(dg)
        }
    }

    private fun convertMessageEntityToMessage(messageEntity: MessageEntity): Message {
        val id = messageEntity.id
        val user =
            User(
                messageEntity.userId,
                messageEntity.userName,
                messageEntity.user.avatar,
                messageEntity.user.online,
                messageEntity.user.lastSeen
            )
        val text = messageEntity.text
        val createdAt = Date(messageEntity.createdAt)

        val message = Message(id, user, text, createdAt)

        if (messageEntity.imageUrl != null) {
            message.setImage(Message.Image(messageEntity.imageUrl!!))
        }

        if (messageEntity.videoUrl != null) {
            message.setVideo(Message.Video(messageEntity.videoUrl!!))
        }

        if (messageEntity.voiceUrl != null) {
            message.setVoice(Message.Voice(messageEntity.voiceUrl!!, messageEntity.voiceDuration))
        }

        return message
    }

    private fun convertUserEntitiesToUsers(userEntities: List<UserEntity>): List<User> {
        return userEntities.map { userEntity ->
            User(
                userEntity.id,
                userEntity.name,
                userEntity.avatar,
                userEntity.online,
                userEntity.lastSeen
            )
        }
    }

    // FIX: Updated startVoiceCall with chatId and userId
    private fun startVoiceCall(){
        CoroutineScope(Dispatchers.IO).launch {
            // Get dialog info before making the call
            val callerDialog = dialogViewModel.getDialog(callerId)

            withContext(Dispatchers.Main) {
                mainRepository.sendConnectionRequest(
                    DataModel(
                        DataModelType.StartVoiceCall, username, caller, null
                    )
                ) {
                    if (it) {
                        startActivity(Intent(this@CallInfoActivity, CallActivity::class.java).apply {
                            putExtra("target", caller)
                            putExtra("isVideoCall", false)
                            putExtra("isCaller", true)
                            putExtra("avatar", avatar)

                            // FIX: Pass chatId and userId
                            if (callerDialog != null) {
                                putExtra("chatId", callerDialog.id)
                                putExtra("userId", callerId)
                                Log.d("CallInfoActivity", "✓ Voice call with chatId: ${callerDialog.id}, userId: $callerId")
                            } else {
                                // Fallback
                                putExtra("chatId", callerId)
                                putExtra("userId", callerId)
                                Log.w("CallInfoActivity", "⚠ Voice call using fallback - chatId: $callerId")
                            }
                        })
                    }
                }

                val newCallLog = CallLogEntity(
                    id = Random.nextLong(),
                    callerName = caller,
                    System.currentTimeMillis(),
                    callDuration = 0,
                    "Outgoing",
                    "Not Answered",
                    avatar,
                    callerId,
                    false,
                    false
                )
                insertCallLog(newCallLog)
            }
        }
    }

    // FIX: Updated startVideoCall with chatId and userId
    private fun startVideoCall(){
        CoroutineScope(Dispatchers.IO).launch {
            // Get dialog info before making the call
            val callerDialog = dialogViewModel.getDialog(callerId)

            withContext(Dispatchers.Main) {
                mainRepository.sendConnectionRequest(
                    DataModel(
                        DataModelType.StartVideoCall, username, caller, null
                    )
                ) {
                    if (it) {
                        startActivity(Intent(this@CallInfoActivity, CallActivity::class.java).apply {
                            putExtra("target", caller)
                            putExtra("isVideoCall", true)
                            putExtra("isCaller", true)
                            putExtra("avatar", avatar)

                            // FIX: Pass chatId and userId
                            if (callerDialog != null) {
                                putExtra("chatId", callerDialog.id)
                                putExtra("userId", callerId)
                                Log.d("CallInfoActivity", "✓ Video call with chatId: ${callerDialog.id}, userId: $callerId")
                            } else {
                                // Fallback
                                putExtra("chatId", callerId)
                                putExtra("userId", callerId)
                                Log.w("CallInfoActivity", "⚠ Video call using fallback - chatId: $callerId")
                            }
                        })
                    }
                }

                val newCallLog = CallLogEntity(
                    id = Random.nextLong(),
                    callerName = caller,
                    System.currentTimeMillis(),
                    callDuration = 0,
                    "Outgoing",
                    "Not Answered",
                    avatar,
                    callerId,
                    true,
                    false
                )
                insertCallLog(newCallLog)
            }
        }
    }

    private fun insertCallLog(callLog: CallLogEntity) {
        callViewModel.insertCallLog(callLog)
    }
}