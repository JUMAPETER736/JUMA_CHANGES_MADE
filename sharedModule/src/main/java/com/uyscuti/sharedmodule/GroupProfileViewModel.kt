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





}
