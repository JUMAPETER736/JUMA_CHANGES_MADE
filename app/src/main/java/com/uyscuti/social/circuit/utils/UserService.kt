package com.uyscuti.social.circuit.utils


import com.uyscuti.social.network.api.response.userstatus.UserStatusResponse
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import javax.inject.Inject

class UserService @Inject constructor(private val retrofitInstance: RetrofitInstance) {

    suspend fun getUserStatus(userId: String): UserStatusResponse? {
        // Use the retrofitInstance to create the API service
        val apiService = retrofitInstance.apiService

        try {
            // Make the API call to get user status
            val response = apiService.getUserStatus(userId)

            if (response.isSuccessful) {
                // API call was successful, return the user status
                return response.body()
            } else {
                // API call was not successful, handle error or return null
                return null
            }
        } catch (e: Exception) {
            // Handle exceptions, log or return null
            return null
        }
    }
}
