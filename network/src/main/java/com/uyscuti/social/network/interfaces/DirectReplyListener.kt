package com.uyscuti.social.network.interfaces



interface DirectReplyListener {
    fun onDirectReply(message: String, chatId: String)
}