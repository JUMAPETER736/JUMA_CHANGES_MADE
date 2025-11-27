package com.uyscuti.social.circuit.model

// PersonViewModel.kt
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.core.common.data.room.repository.ShortsRepository
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShortsViewModel @Inject constructor(
    private val apiService: IFlashapi,
    private val repository: ShortsRepository
) : ViewModel() {


    var isResuming = false
    var pageNumber = 1
    var shortIndex = 0

    var lastPosition = 0


    var isLiked: Boolean = false
    var isFavorite: Boolean = false
    var totalLikes: Int = 0

    var videoShorts = ArrayList<ShortsEntity>()
    var videoShortsList : List<ShortsEntity> = emptyList()
    var mutableShortsList = mutableListOf<ShortsEntity>()
    val followList = mutableListOf<ShortsEntityFollowList>()

    private val _allShortsList = MutableLiveData<List<ShortsEntity>>()
    val allShortsList: LiveData<List<ShortsEntity>> get() = _allShortsList

    fun fetchAllShortsList() {
        viewModelScope.launch {
            // Call the suspend function from the repository
            val result = repository.allShortsList()

            // Update the LiveData with the result on the main thread
            _allShortsList.value = result
        }
    }

    fun addAllShorts(shorts: List<ShortsEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAllShorts(shorts)
        }

    }

    fun addUserProfileShorts(shorts: List<UserShortsEntity>) {
        viewModelScope.launch {
            repository.addUserProfileShorts(shorts)
        }
    }

    private val pageSize = 10 // Adjust as needed

    suspend fun getUserShortsForPage(page: Int): List<UserShortsEntity> {
        // Fetch data from Room for the specified page
        Log.d("TAG", "getUserShortsForPage: ")

        return repository.getShortsForPage(page, pageSize)
    }

    fun getApiService(): IFlashapi {
        return apiService
    }


}
