package com.uyscuti.social.core.common.data.api

import okhttp3.MultipartBody

interface RemoteMessageRepository {
    suspend fun sendMessage(chatId: String, message: String): Result<Unit>

    suspend fun sendAttachment(chatId: String, message: String?, filePath: MultipartBody.Part): Result<Unit>
}
