package com.uyscuti.social.notifications.reply

interface ReplyMessageRepository {
    suspend fun sendReply(chatId: String, message: String): Result<Unit>
}