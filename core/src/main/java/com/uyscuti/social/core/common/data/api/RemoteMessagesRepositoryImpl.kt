package com.uyscuti.social.core.common.data.api

import com.uyscuti.social.network.api.request.messages.SendMessageRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance

import okhttp3.MultipartBody

class RemoteMessageRepositoryImpl(private val messageApi: RetrofitInstance) :
    RemoteMessageRepository {
    override suspend fun sendMessage(chatId: String, message: String): Result<Unit> {
        return try {

            val data = SendMessageRequest(message, null)
            val response = messageApi.apiService.sendMessage(chatId,data)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Message sending failed"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun sendAttachment(
        chatId: String,
        message: String?,
        filePath: MultipartBody.Part
    ): Result<Unit> {
        return  try {
//            val data = SendMessageRequest(null, filePath)
            val response = messageApi.apiService.sendAttachment(chatId,filePath) // Implement this method in your network API
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


//class RemoteMessageRepositoryImpl @Inject constructor(private val retrofitInterface: RetrofitInterface) :
//    RemoteMessageRepository {
//
//    override suspend fun sendMessage(chatId: String, message: String): Result<Unit> {
//        try {
//            // Make a network request using RetrofitInterface to send the message
//            val response = retrofitInterface.sendMessage(chatId, message)
//
//            if (response.isSuccessful) {
//                return Result.Success(Unit)
//            } else {
//                // Handle the error case
//                return Result.Error("Failed to send message")
//            }
//        } catch (e: Exception) {
//            // Handle network request failure
//            return Result.Error("Network request failed: ${e.message}")
//        }
//    }
//}

