package com.uyscuti.social.circuit.utils

import android.os.AsyncTask
import android.util.Log
import com.uyscuti.social.network.api.response.userstatus.UserStatusResponse
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import kotlinx.coroutines.runBlocking

class GetUserStatusTask(
    private val retrofitInstance: RetrofitInstance,
    private val callback: (UserStatusResponse?) -> Unit
) : AsyncTask<String, Void, UserStatusResponse?>() {

    override fun doInBackground(vararg params: String): UserStatusResponse? {
        if (params.isEmpty()) {
            return null
        }
        val userId = params[0]
        // Use the injected Retrofit instance to create the API service
        val apiService = retrofitInstance.apiService
        return runBlocking {
            try {
                // Make the API call to get user status within a coroutine
                val response = apiService.getUserStatus(userId)

                if (response.isSuccessful) {
                    // API call was successful, return the user status
                    return@runBlocking response.body()
                } else {
                    // API call was not successful, handle error or return null
                    return@runBlocking null
                }
            } catch (e: Exception) {
                // Handle exceptions, log or return null
                Log.e("GetUserStatusTask", "User Status Retrieval Failed: ${e.message}")
                e.printStackTrace()
                return@runBlocking null
            }
        }
    }

    override fun onPostExecute(result: UserStatusResponse?) {
        // Handle the result (UserStatusResponse) here
        if (result != null) {
            Log.d("GetUserStatusTask", "User status response: $result")
            // Update UI or perform further actions based on user status
            callback(result)
        } else {
            Log.e("GetUserStatusTask", "Error fetching user status")
            // Handle the error or display an error message
            callback(null)
        }
    }
}



