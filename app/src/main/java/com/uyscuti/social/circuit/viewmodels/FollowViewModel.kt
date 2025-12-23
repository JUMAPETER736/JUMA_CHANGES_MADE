package com.uyscuti.social.circuit.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.core.common.data.room.repository.FollowUnFollowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FollowViewModel"



@HiltViewModel
class FollowViewModel @Inject constructor(
    private val repository: FollowUnFollowRepository
) : ViewModel() {

    val allFollows: LiveData<List<FollowUnFollowEntity>> = repository.allFollows


    fun getFollowStatus(userId: String): LiveData<FollowUnFollowEntity?> {
        return repository.getFollowStatus(userId)
    }


    fun insertOrUpdateFollow(follow: FollowUnFollowEntity) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Attempting to insert or update follow: $follow")
                repository.insertOrUpdateFollow(follow)
                Log.d(TAG, "Successfully inserted or updated follow for user: ${follow.userId}")
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting or updating follow: ${follow.userId}", e)
            }
        }
    }


    suspend fun deleteFollowById(userId: String): Boolean {
        return try {
            Log.d(TAG, "Attempting to delete follow for user: $userId")
            val success = repository.deleteFollowById(userId)
            if (success) {
                Log.d(TAG, "Successfully deleted follow for user: $userId")
            } else {
                Log.w(TAG, "No follow found to delete for user: $userId")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting follow for user: $userId", e)
            false // Return false in case of an error
        }
    }


}