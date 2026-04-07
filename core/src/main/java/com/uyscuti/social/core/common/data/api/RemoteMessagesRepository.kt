package com.uyscuti.social.core.common.data.api

import com.uyscuti.social.network.api.request.messages.SendMessageRequest
import okhttp3.MultipartBody

interface RemoteMessageRepository {
    suspend fun sendMessage(chatId: String, request: SendMessageRequest): Result<Unit>
    suspend fun sendAttachment(chatId: String, message: String?, filePath: MultipartBody.Part): Result<Unit>
}