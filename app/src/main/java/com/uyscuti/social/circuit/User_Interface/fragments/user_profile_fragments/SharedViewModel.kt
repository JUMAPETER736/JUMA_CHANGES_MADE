package com.uyscuti.social.circuit.User_Interface.fragments.user_profile_fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.network.api.response.getallshorts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(private val retrofitInstance: RetrofitInstance)  : ViewModel() {
    private val _storedShortsList = MutableLiveData<List<UserShortsEntity>?>()
    val storedShortsList: LiveData<List<UserShortsEntity>?> get() = _storedShortsList

    private val _storedShorts = MutableLiveData<List<UserShortsEntity>>()
    private val storedShorts :LiveData<List<UserShortsEntity>> = _storedShorts

    fun setStoredShorts(shortsList: List<UserShortsEntity>) {
        _storedShorts.value = shortsList
    }


    fun getStoredShorts(): List<UserShortsEntity>? {
        return _storedShorts.value
    }
    private fun setStoredShortsList(shortsList: List<UserShortsEntity>) {
        _storedShortsList.value = shortsList
    }
    fun getShorts(page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = retrofitInstance.apiService.getShorts(page.toString())
            val responseBody = response.body()
            val shortsEntity = responseBody?.data?.posts?.posts?.let { serverResponseToUserEntity(it) }

            _storedShortsList.postValue(shortsEntity)

            withContext(Dispatchers.Main) {
                setStoredShortsList(shortsEntity!!)
            }

        }
    }

    private fun serverResponseToUserEntity(serverResponse: List<Post>): List<UserShortsEntity> {
        return serverResponse.map { serverResponseItem ->
            UserShortsEntity(
                __v = serverResponseItem.__v,
                _id = serverResponseItem._id,
                content = serverResponseItem.content,
                author = serverResponseItem.author,
                comments = serverResponseItem.comments,
                createdAt = serverResponseItem.createdAt,
                images = serverResponseItem.images,
                isBookmarked = serverResponseItem.isBookmarked,
                isLiked = serverResponseItem.isLiked,
                likes = serverResponseItem.likes,
                tags = serverResponseItem.tags,
                updatedAt = serverResponseItem.updatedAt,
                thumbnail = serverResponseItem.thumbnail
                // map other properties...
            )
        }
    }

}
