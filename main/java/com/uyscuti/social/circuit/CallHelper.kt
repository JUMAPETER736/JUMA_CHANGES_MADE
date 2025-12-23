package com.uyscuti.social.circuit

import android.util.Log
import com.uyscuti.social.call.repository.MainRepository
import com.uyscuti.social.call.service.MainServiceRepository
import com.uyscuti.social.network.utils.LocalStorage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

sealed class CallResult {
    data class Success(val message: String) : CallResult()
    data class Failure(val errorMessage: String) : CallResult()
    data class Retry(val message: String) : CallResult()
}

class CallHelper @Inject constructor(
    private val localStorage: LocalStorage,
    private val mainRepository: MainRepository,
    private val mainServiceRepository: MainServiceRepository
) {

    suspend fun initializeCallService(): CallResult {
        return try {
            withContext(Dispatchers.IO) {
                val username = localStorage.getUsername()
                val userId = localStorage.getUserId()

                mainRepository.init(username)
                mainServiceRepository.startService(username)
                mainRepository.setUserName(username)
                // mainRepository.setUserId(userId)
//                if (result.isSuccess){
//                    CallResult.Success("Initialization successful")
//                } else {
//                    CallResult.Retry("Failed To Connect To Socket")
//                }

//                val result = mainRepository.init(username)
//                when (result) {
//                     -> {
//                        mainServiceRepository.startService(username)
//                        mainRepository.setUserName(username)
//                        // mainRepository.setUserId(userId)
//                        CallResult.Success("Initialization successful")
//                    }
//                    CallResult.Failure -> CallResult.Failure("Failed To Connect To Socket")
//                    else -> CallResult.Failure("Unexpected initialization error") // Handle other potential results
//                }
                CallResult.Success("Initialization successful")
            }
        } catch (e: IOException) {
            // Handle IOException (e.g., internet connection issue)
            Log.d("CallHelper", "Failed to initialize due to an IOException: ${e.message}")
            CallResult.Failure("Failed to initialize due to internet connection issue")
        } catch (e: Exception) {
            // Handle other exceptions
            Log.d("CallHelper", "Failed to initialize due to an unexpected error: ${e.message}")
            CallResult.Failure("Failed to initialize due to an unexpected error")
        }
    }
}

