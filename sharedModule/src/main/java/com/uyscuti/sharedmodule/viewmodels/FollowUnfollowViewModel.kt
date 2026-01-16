package com.uyscuti.sharedmodule.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "FollowUnfollowViewModel"

@HiltViewModel
class FollowUnfollowViewModel @Inject constructor(
    private val retrofitInstance: RetrofitInstance
) : ViewModel() {

    // LiveData for follow/unfollow status
    private val _followUnFollowLiveData = MutableLiveData<FollowResult>()
    val followUnFollowLiveData: LiveData<FollowResult> = _followUnFollowLiveData

    // Legacy observer for backward compatibility
    private val followUnFollowMutableLiveData: MutableLiveData<Boolean> = MutableLiveData()

    fun followUnFollowObserver(): MutableLiveData<Boolean> {
        return followUnFollowMutableLiveData
    }

    /**
     * Follow or unfollow a user
     * @param userId The ID of the user to follow/unfollow
     */
    fun followUnFollow(userId: String) {
        Log.d(TAG, "followUnFollow called for userId: $userId")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Make API call
                val response = retrofitInstance.apiService.followUnFollow(userId)

                Log.d(TAG, "API response code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()

                    if (responseBody != null) {
                        val isFollowing = responseBody.data.following

                        Log.d(TAG, "✅ Follow API SUCCESS - userId: $userId, isFollowing: $isFollowing")

                        // Post success result
                        withContext(Dispatchers.Main) {
                            followUnFollowMutableLiveData.value = isFollowing
                            _followUnFollowLiveData.value = FollowResult.Success(isFollowing, userId)
                        }
                    } else {
                        Log.e(TAG, "❌ Response body is null for userId: $userId")
                        withContext(Dispatchers.Main) {
                            _followUnFollowLiveData.value = FollowResult.Error("Empty response from server")
                        }
                    }
                } else {
                    val errorMsg = "API call failed with code: ${response.code()}"
                    Log.e(TAG, "❌ $errorMsg for userId: $userId")

                    withContext(Dispatchers.Main) {
                        _followUnFollowLiveData.value = FollowResult.Error(errorMsg)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception in followUnFollow for userId: $userId", e)

                withContext(Dispatchers.Main) {
                    _followUnFollowLiveData.value = FollowResult.Error(
                        e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    /**
     * Result class for follow operations
     */
    sealed class FollowResult {
        data class Success(val isFollowing: Boolean, val userId: String) : FollowResult()
        data class Error(val message: String) : FollowResult()
        object Loading : FollowResult()
    }
}