package com.uyscuti.sharedmodule.popupDialog

import android.content.Context
import android.util.Log
import com.uyscuti.sharedmodule.MessagesActivity
import com.uyscuti.sharedmodule.data.model.Dialog
import com.uyscuti.sharedmodule.data.model.Message
import com.uyscuti.sharedmodule.data.model.User
import com.uyscuti.sharedmodule.presentation.DialogViewModel
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class DialogManager(
    private val activity: Context,
    private val dialogViewModel: DialogViewModel,
    private val myId: String,
    private val productReference: String
) {
    private var participants: ArrayList<User> = arrayListOf()

    fun openChat(user: User) {
        CoroutineScope(Dispatchers.Main).launch {
            chatExist(user.name) { exists, dialogEntity ->
                if (exists) {
                    Log.d("UserDialog", "Dialog exists, and you can use dialogEntity")
                    participants.add(user)

                    val lastMessage = dialogEntity?.lastMessage?.toMessage()

                    val dialog = Dialog(
                        dialogEntity?.id,
                        dialogEntity?.dialogName,
                        dialogEntity?.dialogPhoto,
                        participants,
                        lastMessage,
                        0
                    )


                    MessagesActivity.open(activity, "", dialog, false, productReference)
                    participants.removeAt(0)
                } else {
                    Log.d("UserDialog", "Dialog does not exist")
                    doInBackGround(user)
                }
            }
        }
    }

    private suspend fun chatExist(name: String, resultHandler: (Boolean, DialogEntity?) -> Unit) {
        val dialogEntityLiveData = dialogViewModel.getDialogByName(name)
        val dialogs = dialogViewModel.getDialogsList()
        val available = dialogs.filter { it.id != it.dialogName }.find { it.dialogName == name }
        val dialogEntity = dialogEntityLiveData.value

        Log.d("Dialog", "Dialog Entity : $dialogEntity")
        Log.d("Dialog", "Available Dialog Entity : $available")

        resultHandler(available != null, available)
    }

    private fun MessageEntity.toMessage(): Message {
        val userId = if (userId == myId) "0" else "1"
        val status = status
        val user = User(userId, user.name, user.avatar, user.online, user.lastSeen)
        val date = Date(createdAt)

        val messageContent = when {
            imageUrl != null -> Message(id, user, null, date).apply {
                setImage(Message.Image(imageUrl))
                setStatus(status)
            }
            videoUrl != null -> Message(id, user, null, date).apply {
                setVideo(Message.Video(videoUrl!!))
                setStatus(status)
            }
            audioUrl != null -> Message(id, user, null, date).apply {
                setAudio(Message.Audio(audioUrl!!, 0, getNameFromUrl(audioUrl!!)))
                setStatus(status)
            }
            voiceUrl != null -> Message(id, user, null, date).apply {
                setVoice(Message.Voice(voiceUrl!!, 10000))
                setStatus(status)
            }
            docUrl != null -> Message(id, user, null, date).apply {
                setStatus(status)
            }
            else -> Message(id, user, text, date).apply {
                setStatus(status)
            }
        }

        return messageContent
    }

    private fun getNameFromUrl(videoUrl: String): String {
        val parts = videoUrl.split("/")
        return parts.last()
    }

    private fun doInBackGround(user: User) {
        val singleUserList = arrayListOf(user)

        Log.d("UserList", "Single User List Size : ${singleUserList.size}")

        val tempDialog = Dialog(
            user.id,
            user.name,
            user.avatar,
            singleUserList,
            null,
            0
        )

        val dialogEntity = DialogEntity(
            id = tempDialog.dialogName,
            dialogPhoto = tempDialog.dialogPhoto,
            dialogName = tempDialog.dialogName,
            users = listOf(user.toUserEntity()),
            lastMessage = null,
            unreadCount = 0
        )

        insertDialog(dialogEntity)
        openTempChat(tempDialog)
    }

    private fun insertDialog(dialog: DialogEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            dialogViewModel.insertDialog(dialog)
        }
    }

    private fun openTempChat(dialog: Dialog) {
        MessagesActivity.open(activity, "", dialog, true,productReference)
    }

    private fun User.toUserEntity(): UserEntity {
        return UserEntity(
            id,
            name,
            avatar,
            lastSeen = Date(),
            true
        )
    }
}