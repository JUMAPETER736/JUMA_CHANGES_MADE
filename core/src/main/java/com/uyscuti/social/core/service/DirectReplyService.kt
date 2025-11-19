package com.uyscuti.social.core.service

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.core.common.data.room.repository.DialogRepository
import com.uyscuti.social.core.common.data.room.repository.GroupDialogRepository
import com.uyscuti.social.core.common.data.room.repository.MessageRepository
import com.uyscuti.social.network.eventmodels.DirectReplyEvent
import com.uyscuti.social.network.utils.LocalStorage

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Date
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class DirectReplyService : Service() {

    @Inject
    lateinit var groupDialogRepository: GroupDialogRepository

    @Inject
    lateinit var messageRepository: MessageRepository

    @Inject
    lateinit var dialogRepository: DialogRepository

    @Inject
    lateinit var localStorage: LocalStorage

    private lateinit var settings: SharedPreferences
    private val PREFS_NAME = "LocalSettings"

    private lateinit var myId: String

    override fun onCreate() {
        super.onCreate()
        // Register with EventBus
        EventBus.getDefault().register(this)

        settings = getSharedPreferences(PREFS_NAME, 0)
//        val accessToken = settings.getString("token", "").toString()
//        binding.toolbar.setLogo(R.drawable.flash)

        myId = localStorage.getUserId()
    }

    // Subscribe to the event
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDirectReplyEvent(event: DirectReplyEvent) {
        // Handle the direct reply here
        // Update UI, perform actions, etc.
        val message = createMessage(event.message,event.chatId)
        CoroutineScope(Dispatchers.IO).launch {
            messageRepository.insertMessage(message)
            try{
                dialogRepository.updateLastMessageForThisChat(event.chatId,message)
            } catch (e:Exception){
                groupDialogRepository.updateLastMessageForThisChat(event.chatId,message)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister with EventBus
        EventBus.getDefault().unregister(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun createMessage(text: String,chatId: String): MessageEntity {
        val createdAt = System.currentTimeMillis()
        val lastSeen = Date(createdAt)

        val avatar = settings.getString("avatar", "avatar").toString()

        val user = UserEntity(
            id = "0",
            name = "You",
            avatar = avatar,
            online = true,
            lastSeen = lastSeen
        )

        return MessageEntity(
            id = "Text_${Random.nextInt()}",
            chatId = chatId,
            text = text,
            userId = myId,
            user = user,
            createdAt = createdAt,
            imageUrl = null,
            voiceUrl = null,
            voiceDuration = 0,
            userName = "You",
            status = "Sent",
            videoUrl = null,
            audioUrl = null,
            docUrl = null,
            fileSize = 0,
            deleted = false
        )
    }
}
