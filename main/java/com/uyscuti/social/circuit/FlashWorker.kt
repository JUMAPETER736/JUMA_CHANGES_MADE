package com.uyscuti.social.circuit

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.uyscuti.social.circuit.CallHelper
import com.uyscuti.social.circuit.CallResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException

@HiltWorker
class FlashWorker @AssistedInject constructor(
    @Assisted val callHelper: CallHelper,
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Log.d("FlashWorker", "Call Worker Started")
        return try {
            when (val result = callHelper.initializeCallService()) {
                is CallResult.Success -> Result.success()
                is CallResult.Failure -> Result.failure(
                    Data.Builder().putString("error", result.errorMessage).build()
                )
                is CallResult.Retry -> Result.retry()
            }
        } catch (e: IOException) {
            // Network-related exception, retry the work
            Result.retry()
        } catch (e: Exception) {
            // Other exceptions, mark as failure
            Log.d("FlashWorker", "Call Worker Failed ${e.message}")
            Result.failure(Data.Builder().putString("error", e.toString()).build())
        }
    }
}

