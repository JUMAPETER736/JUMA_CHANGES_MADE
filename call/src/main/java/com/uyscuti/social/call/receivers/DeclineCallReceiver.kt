package com.uyscuti.social.call.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.uyscuti.social.call.ui.CallActivity


class DeclineCallReceiver : BroadcastReceiver() {
    val TAG = "Decline Call"

    override fun onReceive(context: Context?, intent: Intent?) {


        // Handle the "Answer" action here
        Log.d(TAG, "Decline Clicked")//
        //if (intent?.action == ACTION_SEND_STRING) {
        val message = intent?.getStringExtra("message")
        Log.d(TAG, "$message")
        if (message != null) {
            // Send the string to the Chat Activity
            Log.d(TAG, "Message found $message")
            val chatIntent = Intent(context, CallActivity::class.java)
            chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            chatIntent.putExtra("message", message)
            context?.startActivity(chatIntent)
        }
        //}

    }
}
