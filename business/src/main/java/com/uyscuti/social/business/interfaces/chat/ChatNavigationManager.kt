package com.uyscuti.social.business.interfaces.chat

import android.annotation.SuppressLint
import android.content.Context


object ChatNavigationManager {
    const val ACTION_NAVIGATE_TO_CHAT = "navigate_to_chat"
    const val EXTRA_USER_ID = "user_id"
    const val EXTRA_CHAT_TYPE = "chat_type"

    @SuppressLint("UnsafeOptInUsageError")
    fun navigateToChat(context: Context, userId: String, chatType: String = "direct") {
//        val intent = Intent(context, MainActivity::class.java).apply {
//            Intent.setAction = ACTION_NAVIGATE_TO_CHAT
//            Intent.setFlags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            Intent.putExtra(EXTRA_USER_ID, userId)
//            Intent.putExtra(EXTRA_CHAT_TYPE, chatType)
//        }
//        context.startActivity(intent)
    }
}