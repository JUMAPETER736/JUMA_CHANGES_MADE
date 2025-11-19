package com.uyscuti.social.notifications.reply

import com.uyscuti.social.network.api.request.messages.SendMessageRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance

class ReplyMessageRepositoryImpl(private val messageApi: RetrofitInstance) :
    ReplyMessageRepository {
    override suspend fun sendReply(chatId: String, message: String): Result<Unit> {
        return try {

            val data = SendMessageRequest(message, null)
            val response = messageApi.apiService.sendMessage(chatId,data) // Implement this method in your network API
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Message sending failed"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}