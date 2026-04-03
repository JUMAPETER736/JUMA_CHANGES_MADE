package com.uyscuti.sharedmodule

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uyscuti.social.core.common.data.room.dao.GroupDialogDao
import com.uyscuti.social.network.api.models.AddMembersRequest
import com.uyscuti.social.network.api.models.AvatarData
import com.uyscuti.social.network.api.models.ChangeRoleRequest
import com.uyscuti.social.network.api.models.GroupChatDetail
import com.uyscuti.social.network.api.models.GroupLinkData
import com.uyscuti.social.network.api.models.GroupMember
import com.uyscuti.social.network.api.models.GroupMemberUser
import com.uyscuti.social.network.api.models.GroupRole
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

