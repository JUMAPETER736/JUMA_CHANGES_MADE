package com.uyscuti.social.core.common.data.api

import android.util.Log
import com.uyscuti.social.network.api.request.messages.SendMessageRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import okhttp3.MultipartBody

class RemoteMessageRepositoryImpl(private val messageApi: RetrofitInstance) :
    RemoteMessageRepository {

    override suspend fun sendMessage(chatId: String, request: SendMessageRequest): Result<Unit> {
        return try {
            Log.d("RemoteMessageRepo", "Sending request: $request")
            val response = messageApi.apiService.sendMessage(chatId, request)
            if (response.isSuccessful) {
                Log.d("RemoteMessageRepo", "Message sent successfully")
                Result.Success(Unit)
            } else {
                Log.e("RemoteMessageRepo", "Failed: ${response.code()} ${response.errorBody()?.string()}")
                Result.Error(Exception("Message sending failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("RemoteMessageRepo", "Exception: ${e.message}")
            Result.Error(e)
        }
    }

    override suspend fun sendAttachment(
        chatId: String,
        message: String?,
        filePath: MultipartBody.Part
    ): Result<Unit> {
        return try {
            val response = messageApi.apiService.sendAttachment(chatId, filePath)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Attachment sending failed"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}