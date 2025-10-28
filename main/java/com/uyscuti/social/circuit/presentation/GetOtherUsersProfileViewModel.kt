package com.uyscuti.social.circuit.presentation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.network.api.response.otherusersprofile.Data
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "GetOtherUsersProfileViewModel"
@HiltViewModel
class GetOtherUsersProfileViewModel @Inject constructor(
    private val retrofitInstance: RetrofitInstance) :
    ViewModel() {
    private var userProfileLiveData: MutableLiveData<Data?> = MutableLiveData()
    private val onErrorFeedBack: MutableLiveData<String> = MutableLiveData()

    fun getUserProfileShortsObserver(): MutableLiveData<Data?> {
        return userProfileLiveData
    }

    fun getOnErrorFeedBackObserver(): MutableLiveData<String> {
        return onErrorFeedBack
    }

    fun getOtherUsersProfile(username: String) {
        viewModelScope.launch(Dispatchers.IO) {

            try {
                val response = retrofitInstance.apiService.getOtherUsersProfileByUsername(username)
                val responseBody = response.body()
//                val shortsEntity = responseBody?.data?.posts?.let { serverResponseToUserEntity(it) }
                if (responseBody != null) {
                    userProfileLiveData.postValue(responseBody.data)
                    Log.d(TAG, "getOtherUsersProfile data: ${responseBody.data}")
                    Log.d(TAG, "getOtherUsersProfile followers count: ${responseBody.data.followersCount}")
//                  responseBody.data.
                }else {
                    withContext(Dispatchers.Main) {
                        onErrorFeedBack.postValue("User Data is empty")
//                        Toast.makeText(this@withContext, "User Data is empty", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("UserProfileShortsViewModel", "Exception: ${e.message}")
                onErrorFeedBack.postValue("Error connecting to server.....check internet connection")
                e.printStackTrace()
            }

        }
    }

}