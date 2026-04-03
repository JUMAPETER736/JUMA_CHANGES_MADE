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

    //  Members

    //  Single loadMembers — fetches from server, caches in Room,
    //    falls back to Room cache if server rejects (e.g. user was removed)
    fun loadMembers(chatId: String) {
        _members.value = GroupResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofit.apiService.getGroupMembers(chatId)
                if (response.isSuccessful) {
                    val list = response.body()?.data ?: emptyList()
                    saveMembersToCache(chatId, list)
                    withContext(Dispatchers.Main) {
                        _members.value = GroupResult.Success(list)
                    }
                } else {
                    // Try JSON cache first
                    var cached = loadMembersFromCache(chatId)

                    //  fall back to GroupDialogEntity.users if cache is empty
                    if (cached.isEmpty()) {
                        cached = loadMembersFromGroupDialog(chatId)
                    }

                    withContext(Dispatchers.Main) {
                        _members.value = if (cached.isNotEmpty()) {
                            GroupResult.Success(cached)
                        } else {
                            GroupResult.Error(response.errorBody()?.string() ?: "Unknown error")
                        }
                    }
                }
            } catch (e: Exception) {
                var cached = loadMembersFromCache(chatId)
                if (cached.isEmpty()) {
                    cached = loadMembersFromGroupDialog(chatId)
                }
                withContext(Dispatchers.Main) {
                    _members.value = if (cached.isNotEmpty()) {
                        GroupResult.Success(cached)
                    } else {
                        GroupResult.Error(e.message ?: "Network error")
                    }
                }
            }
        }
    }

    //  Report

    fun reportGroup(chatId: String, reason: String) {
        viewModelScope.launch {
            _reportResult.value = GroupResult.Loading
            try {
                val response = retrofit.apiService.reportGroup(
                    chatId,
                    mapOf("reason" to reason, "targetType" to "group")
                )
                if (response.isSuccessful) {
                    _reportResult.value = GroupResult.Success("Report submitted")
                } else {
                    _reportResult.value = GroupResult.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _reportResult.value = GroupResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    //  Group detail

    fun loadGroupDetail(chatId: String) {
        _groupDetail.value = GroupResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofit.apiService.getGroupChatDetails(chatId)
                if (response.isSuccessful) {
                    val detail = response.body()?.data
                    withContext(Dispatchers.Main) {
                        if (detail != null) _groupDetail.value = GroupResult.Success(detail)
                        else _groupDetail.value = GroupResult.Error("Empty response")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    withContext(Dispatchers.Main) { _groupDetail.value = GroupResult.Error(err) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _groupDetail.value = GroupResult.Error(e.message ?: "Network error")
                }
            }
        }
    }

    //  Add members

    fun addMembers(chatId: String, userIds: List<String>) {
        _addMembersResult.value = GroupResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofit.apiService.addMembersToGroup(
                    chatId, AddMembersRequest(participants = userIds)
                )
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        _addMembersResult.value = GroupResult.Success("Members added")
                        loadMembers(chatId)
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error (${response.code()})"
                    withContext(Dispatchers.Main) { _addMembersResult.value = GroupResult.Error(err) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _addMembersResult.value = GroupResult.Error(e.message ?: "Network error")
                }
            }
        }
    }

    //  Avatar

    fun updateGroupAvatarLocally(chatId: String, avatarUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            groupDialogDao.updateGroupAvatar(chatId, avatarUrl)
        }
    }

    fun updateAvatar(chatId: String, avatarPart: okhttp3.MultipartBody.Part) {
        _avatarResult.value = GroupResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofit.apiService.updateGroupAvatar(chatId, avatarPart)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        _avatarResult.value = GroupResult.Success("Avatar updated")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    withContext(Dispatchers.Main) { _avatarResult.value = GroupResult.Error(err) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _avatarResult.value = GroupResult.Error(e.message ?: "Network error")
                }
            }
        }
    }

    //  Description

    fun loadGroupDescription(chatId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val group = groupDialogDao.checkGroup(chatId)
            _groupDescription.postValue(group?.description ?: "")
        }
    }

    fun updateGroupDescriptionLocally(chatId: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            groupDialogDao.updateGroupDescription(chatId, description)
        }
    }

    fun updateDescription(chatId: String, description: String) {
        _descriptionResult.value = GroupResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofit.apiService.updateGroupDescription(
                    chatId, mapOf("description" to description)
                )
                if (response.isSuccessful) {
                    groupDialogDao.updateGroupDescription(chatId, description)
                    withContext(Dispatchers.Main) {
                        _descriptionResult.value = GroupResult.Success(description)
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    withContext(Dispatchers.Main) {
                        _descriptionResult.value = GroupResult.Error(err)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _descriptionResult.value = GroupResult.Error(e.message ?: "Network error")
                }
            }
        }
    }


    //  Name

    fun updateGroupNameLocally(chatId: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            groupDialogDao.updateGroupName(chatId, name)
        }
    }

    fun renameGroup(chatId: String, newName: String) {
        _renameResult.value = GroupResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofit.apiService.renameGroupChat(chatId, mapOf("name" to newName))
                if (response.isSuccessful) {
                    groupDialogDao.updateGroupName(chatId, newName)
                    withContext(Dispatchers.Main) {
                        _renameResult.value = GroupResult.Success(newName)
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    withContext(Dispatchers.Main) { _renameResult.value = GroupResult.Error(err) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _renameResult.value = GroupResult.Error(e.message ?: "Network error")
                }
            }
        }
    }

    fun changeMemberRole(chatId: String, userId: String, newRole: String) {
        _roleChange.value = GroupResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofit.apiService.changeMemberRole(
                    chatId, userId, ChangeRoleRequest(newRole)
                )
                if (response.isSuccessful) {
                    val updated = response.body()?.data
                    withContext(Dispatchers.Main) {
                        if (updated != null) {
                            _roleChange.value = GroupResult.Success(updated)
                            loadMembers(chatId)
                        } else {
                            _roleChange.value = GroupResult.Error("Empty response")
                        }
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    withContext(Dispatchers.Main) { _roleChange.value = GroupResult.Error(err) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _roleChange.value = GroupResult.Error(e.message ?: "Network error")
                }
            }
        }
    }

    //  Remove member

    fun removeMember(chatId: String, userId: String) {
        _removeMember.value = GroupResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofit.apiService.removeMember(chatId, userId)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        _removeMember.value = GroupResult.Success("Member removed")
                        loadMembers(chatId)
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    withContext(Dispatchers.Main) { _removeMember.value = GroupResult.Error(err) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _removeMember.value = GroupResult.Error(e.message ?: "Network error")
                }
            }
        }
    }

    //  Leave

    fun leaveGroup(chatId: String) {
        _leaveResult.value = GroupResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofit.apiService.leaveGroup(chatId)
                if (response.isSuccessful) {
                    groupDialogDao.deleteGroupDialogById(chatId)
                    withContext(Dispatchers.Main) {
                        _leaveResult.value = GroupResult.Success("Left group")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    withContext(Dispatchers.Main) { _leaveResult.value = GroupResult.Error(err) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _leaveResult.value = GroupResult.Error(e.message ?: "Network error")
                }
            }
        }
    }

    //  Delete

    fun deleteGroup(chatId: String) {
        _deleteGroup.value = GroupResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofit.apiService.deleteGroup(chatId)
                if (response.isSuccessful) {
                    groupDialogDao.deleteGroupDialogById(chatId)
                    withContext(Dispatchers.Main) {
                        _deleteGroup.value = GroupResult.Success("Group deleted")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error (${response.code()})"
                    withContext(Dispatchers.Main) { _deleteGroup.value = GroupResult.Error(err) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _deleteGroup.value = GroupResult.Error(e.message ?: "Network error")
                }
            }
        }
    }

    //  Invite link

    fun generateLink(chatId: String) {
        _inviteLink.value = GroupResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofit.apiService.generateGroupLink(chatId)
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    withContext(Dispatchers.Main) {
                        if (data != null) _inviteLink.value = GroupResult.Success(data)
                        else _inviteLink.value = GroupResult.Error("Empty response")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    withContext(Dispatchers.Main) { _inviteLink.value = GroupResult.Error(err) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _inviteLink.value = GroupResult.Error(e.message ?: "Network error")
                }
            }
        }
    }

    fun revokeLink(chatId: String) {
        _revokeResult.value = GroupResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofit.apiService.revokeGroupLink(chatId)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        _revokeResult.value = GroupResult.Success("Link revoked")
                        _inviteLink.value   = GroupResult.Error("Link revoked")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    withContext(Dispatchers.Main) { _revokeResult.value = GroupResult.Error(err) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _revokeResult.value = GroupResult.Error(e.message ?: "Network error")
                }
            }
        }
    }

    fun joinGroupViaLink(inviteToken: String) {
        _joinResult.value = GroupResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofit.apiService.joinGroupViaLink(inviteToken)
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    withContext(Dispatchers.Main) {
                        if (data != null) _joinResult.value = GroupResult.Success(data)
                        else _joinResult.value = GroupResult.Error("Empty response")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    withContext(Dispatchers.Main) { _joinResult.value = GroupResult.Error(err) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _joinResult.value = GroupResult.Error(e.message ?: "Network error")
                }
            }
        }
    }

}
