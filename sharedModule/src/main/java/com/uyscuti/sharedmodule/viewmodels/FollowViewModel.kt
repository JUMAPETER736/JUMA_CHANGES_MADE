package com.uyscuti.sharedmodule.viewmodels

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
class FollowViewModel @Inject constructor(private val repository: FollowUnFollowRepository) : ViewModel() {

    val allFollows: LiveData<List<FollowUnFollowEntity>> = repository.allFollows

    fun getFollowStatus(userId: String): LiveData<FollowUnFollowEntity?> {
        return repository.getFollowStatus(userId)
    }

    fun insertOrUpdateFollow(follow: FollowUnFollowEntity) {
        viewModelScope.launch {
            Log.d(TAG, "Inserting or updating follow: $follow")
            repository.insertOrUpdateFollow(follow)
            Log.d(TAG, "Follow inserted or updated successfully")
        }
    }

    suspend fun deleteFollowById(userId: String): Boolean {
        return repository.deleteFollowById(userId)
    }


//    fun deleteFollowById(userId: String) {
//        viewModelScope.launch {
//            repository.deleteFollowById(userId)
//        }
//    }
    // Additional methods can be added based on your requirements
}
