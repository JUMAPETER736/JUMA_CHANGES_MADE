package com.uyscuti.social.circuit.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.core.common.data.room.repository.ShortsFollowListRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowListItemViewModel  @Inject constructor(private val repository: ShortsFollowListRepository) : ViewModel() {

    val _followListItems: LiveData<List<ShortsEntityFollowList>> = repository.allFollowListItems
    val allShortsList: List<ShortsEntityFollowList> = repository.allFollowShortsList

    fun getFollowListItems(): LiveData<List<ShortsEntityFollowList>> {
        return _followListItems
    }


    fun insertFollowListItems(followList: List<ShortsEntityFollowList>) {
        viewModelScope.launch {
            repository.insertFollowListItems(followList)
        }
    }

    fun deleteFollowListItem(followersId: String) {
        viewModelScope.launch {
            repository.deleteFollowListItem(followersId)
        }
    }

    suspend fun deleteFollowById(userId: String): Boolean {
        return repository.deleteFollowById(userId)
    }


    fun deleteAllFollowListItems() {
        viewModelScope.launch {
            repository.deleteAllFollowListItems()
        }
    }

    fun insertOrUpdateEntity(entity: ShortsEntityFollowList) {
        viewModelScope.launch {
            repository.insertOrUpdateEntity(entity)
        }
    }

    fun getEntityById(id: String): LiveData<ShortsEntityFollowList?> {
        return liveData {
            emit(repository.getEntityById(id))
        }
    }
}
