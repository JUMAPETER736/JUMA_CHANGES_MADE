package com.uyscuti.social.circuit.User_Interface.fragments.user_profile_fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class UserShortsViewModel @Inject constructor(private val retrofitInstance: RetrofitInstance): ViewModel() {

    fun getListData(): Flow<PagingData<UserShortsEntity>> {
        return Pager (config = PagingConfig(pageSize = 10, maxSize = 200),
            pagingSourceFactory = { UserProfileShortsPagingSource(retrofitInstance) }).flow.cachedIn(viewModelScope)
    }
}