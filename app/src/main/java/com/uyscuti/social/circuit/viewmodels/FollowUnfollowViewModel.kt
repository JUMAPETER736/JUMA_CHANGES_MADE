package com.uyscuti.social.circuit.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FollowUnfollowViewModel"
@HiltViewModel
class FollowUnfollowViewModel @Inject constructor(private val retrofitInstance: RetrofitInstance) : ViewModel(){

    private val followUnFollowMutableLiveData: MutableLiveData<Boolean> = MutableLiveData()

    fun followUnFollowObserver(): MutableLiveData<Boolean> {
        return followUnFollowMutableLiveData
    }

    fun followUnFollow(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try{
                val response = retrofitInstance.apiService.followUnFollow(userId)
                val responseBody = response.body()
                if(responseBody != null) {
                    val isFollowing = responseBody.data.following

                    Log.d(TAG, "followUnFollow: $isFollowing")
                    followUnFollowMutableLiveData.postValue(isFollowing)

                }
            }catch (e: Exception) {

            }
        }

    }
}