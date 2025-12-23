package com.uyscuti.social.notifications.receiver

import android.Manifest
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.uyscuti.social.network.eventmodels.DirectReplyEvent
import com.uyscuti.social.network.interfaces.DirectReplyListener
import com.uyscuti.social.network.utils.LocalStorage

import com.uyscuti.social.notifications.di.RESULT_KEY
import com.uyscuti.social.notifications.reply.ReplyMessageRepository
import com.uyscuti.social.notifications.reply.Result
import com.uyscuti.social.notifications.utils.NoteUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MyReceiver : BroadcastReceiver() {

    @Inject
    @Named("chat_notification_manager_compat")
    lateinit var notificationManager: NotificationManagerCompat
    @Inject
    @Named("chat_notification_compat_builder")
    lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var localStorage: LocalStorage

    private var directReplyListener: DirectReplyListener? = null

//    @Inject
//    lateinit var messageRepository: MessageRepository

    @Inject
    lateinit var replyMessageRepository: ReplyMessageRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        if (remoteInput != null) {
            val input = remoteInput.getCharSequence(RESULT_KEY).toString()

            val chatI = intent?.getStringExtra("chatId")
//            val results = RemoteInput.getResultsFromIntent(intent, RESULT_KEY)
//            val text = results.getString(RESULT_KEY)

            Log.d("MyReceiver", "onReceive ChatId: $chatI")

            val chatId = localStorage.getChatId()

//            Log.d("MyReceiver", "onReceive: chatId $chatId")
//            directReplyListener?.onDirectReply(input, chatId)
            // Post the event to EventBus
            EventBus.getDefault().post(DirectReplyEvent(input, chatId))

            CoroutineScope(Dispatchers.IO).launch {
                if (chatId.length >= 10 ) {
                    when(val result = replyMessageRepository.sendReply(chatId, input)){
                        is  Result.Success -> {
//                            Log.d("MyReceiver", "onReceive: message sent successfully")
//                            ChatNotificationService.stopSelf()

                            withContext(Dispatchers.Main) {
                                NoteUtils.showToast(context, "Reply sent", false)
                            }
                            localStorage.clearChatId()
//                            return
                        }
                        is  Result.Error -> {
//                            Log.d("MyReceiver", "onReceive: there was an error sending the message : ${result.exception.message}")
//                            withContext(Dispatchers.Main) {
////                                NoteUtils.showToast(context, "Reply failed", false)
//                            }
                            localStorage.clearChatId()
                        }
                    }
                }
            }
            val person = Person.Builder().setName("Me").build()
            val message = NotificationCompat.MessagingStyle.Message(
                input, System.currentTimeMillis(), person
            )
            val notificationStyle = NotificationCompat.MessagingStyle(person).addMessage(message)

            if (context?.let {
                    ActivityCompat.checkSelfPermission(
                        it,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                } != PackageManager.PERMISSION_GRANTED
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
//            notificationManager.notify(
//                5858,
//                notificationBuilder
////                    .setStyle(notificationStyle)
//                    .setContentTitle("Sent!")
//                    .setAutoCancel(true)
//                    .setStyle(null)
//                    .build()
//            )

            // Cancel the original notification
            notificationManager.cancel(5858)
//            notificationManager.
            return
        }
        return
    }

    private fun insertMessage(){

    }
}