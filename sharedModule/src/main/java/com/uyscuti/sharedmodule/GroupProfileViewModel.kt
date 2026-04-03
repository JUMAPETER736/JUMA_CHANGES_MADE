package com.uyscuti.sharedmodule

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uyscuti.social.core.common.data.room.dao.GroupDialogDao
import com.uyscuti.social.network.api.request.group.GroupChatDetail
import com.uyscuti.social.network.api.request.group.GroupLinkData
import com.uyscuti.social.network.api.request.group.GroupMember
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class GroupResult<out T> {
    data class Success<T>(val data: T) : GroupResult<T>()
    data class Error(val message: String) : GroupResult<Nothing>()
    object Loading : GroupResult<Nothing>()
}


@HiltViewModel
class GroupProfileViewModel @Inject constructor(
    private val retrofit: RetrofitInstance,
    private val groupDialogDao: GroupDialogDao,
    private val localStorage: LocalStorage
) : ViewModel() {


    private val gson = Gson()

    private val _avatarResult = MutableLiveData<GroupResult<String>>()
    val avatarResult: LiveData<GroupResult<String>> = _avatarResult

    private val _members = MutableLiveData<GroupResult<List<GroupMember>>>()
    val members: LiveData<GroupResult<List<GroupMember>>> = _members


    private val _roleChange = MutableLiveData<GroupResult<GroupChatDetail>>()
    val roleChange: LiveData<GroupResult<GroupChatDetail>> = _roleChange

    private val _removeMember = MutableLiveData<GroupResult<String>>()
    val removeMember: LiveData<GroupResult<String>> = _removeMember

    private val _renameResult = MutableLiveData<GroupResult<String>>()
    val renameResult: LiveData<GroupResult<String>> = _renameResult

    private val _leaveResult = MutableLiveData<GroupResult<String>>()
    val leaveResult: LiveData<GroupResult<String>> = _leaveResult

    private val _deleteGroup = MutableLiveData<GroupResult<String>>()
    val deleteGroup: LiveData<GroupResult<String>> = _deleteGroup

    private val _inviteLink = MutableLiveData<GroupResult<GroupLinkData>>()
    val inviteLink: LiveData<GroupResult<GroupLinkData>> = _inviteLink

    private val _revokeResult = MutableLiveData<GroupResult<String>>()
    val revokeResult: LiveData<GroupResult<String>> = _revokeResult

    private val _joinResult = MutableLiveData<GroupResult<GroupChatDetail>>()
    val joinResult: LiveData<GroupResult<GroupChatDetail>> = _joinResult

    private val _descriptionResult = MutableLiveData<GroupResult<String>>()
    val descriptionResult: LiveData<GroupResult<String>> = _descriptionResult

    private val _groupDescription = MutableLiveData<String?>()
    val groupDescription: LiveData<String?> = _groupDescription

    private val _addMembersResult = MutableLiveData<GroupResult<String>>()
    val addMembersResult: LiveData<GroupResult<String>> = _addMembersResult

    private val _groupDetail = MutableLiveData<GroupResult<GroupChatDetail>>()
    val groupDetail: LiveData<GroupResult<GroupChatDetail>> = _groupDetail

    private val _muteMemberResult = MutableLiveData<GroupResult<String>>()
    val muteMemberResult: LiveData<GroupResult<String>> = _muteMemberResult

    private val _lockGroupResult = MutableLiveData<GroupResult<String>>()
    val lockGroupResult: LiveData<GroupResult<String>> = _lockGroupResult

    private val _reportResult = MutableLiveData<GroupResult<String>>()
    val reportResult: LiveData<GroupResult<String>> = _reportResult

    //  Cache helpers

    //  Save members list as JSON into Room so removed users can still see them
    private suspend fun saveMembersToCache(chatId: String, members: List<GroupMember>) {
        try {
            val json = gson.toJson(members)
            groupDialogDao.updateCachedMembers(chatId, json)
        } catch (e: Exception) {
            Log.e("GroupProfileViewModel", "Failed to cache members", e)
        }
    }

    //  Load members from Room cache — used when server rejects us (removed)
    private suspend fun loadMembersFromCache(chatId: String): List<GroupMember> {
        return try {
            val json = groupDialogDao.getCachedMembers(chatId) ?: return emptyList()
            val type = object : TypeToken<List<GroupMember>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e("GroupProfileViewModel", "Failed to load cached members", e)
            emptyList()
        }
    }


}
