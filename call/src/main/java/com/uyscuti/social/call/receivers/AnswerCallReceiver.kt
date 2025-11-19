package com.uyscuti.social.call.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.uyscuti.social.call.repository.MainRepository
import com.uyscuti.social.call.ui.CallActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AnswerCallReceiver : BroadcastReceiver() {
    val TAG = "Answer Call"

    @Inject
    lateinit var mainRepository: MainRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        // Handle the "Answer" action here
        Log.d(TAG, "Answer Clicked")//

        val target = mainRepository.getTarget()

        Log.d(TAG, "Call Target $target")

        //if (intent?.action == ACTION_SEND_STRING) {
        val message = intent?.getStringExtra("message")
        Log.d(TAG, "$message")
        if (message != null) {
            // Send the string to the Chat Activity
            Log.d(TAG, "Message found $message")
            val chatIntent = Intent(context, CallActivity::class.java)
            chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            chatIntent.putExtra("message", message)
            chatIntent.putExtra("target", target)
            chatIntent.putExtra("isVideoCall", true)
            chatIntent.putExtra("isCaller", false)
            context?.startActivity(chatIntent)
        }
        //}

    }
}
