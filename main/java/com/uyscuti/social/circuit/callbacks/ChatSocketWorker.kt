package com.uyscuti.social.circuit.callbacks

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.CoreChatSocketClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ChatSocketWorker @AssistedInject constructor(
    @Assisted val chatSocketClient: CoreChatSocketClient,
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {

        Log.d("ChatSocketWorker", "Chat Socket Worker Started")
        return  try {
            chatSocketClient.connect()
            Result.success()
        } catch (e:Exception){
            Result.failure(Data.Builder().putString("error", e.message).build())
        }
    }
}