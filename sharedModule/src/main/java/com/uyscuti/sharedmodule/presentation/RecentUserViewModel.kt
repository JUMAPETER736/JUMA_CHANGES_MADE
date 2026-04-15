package com.uyscuti.sharedmodule.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.core.models.data.User
import com.uyscuti.social.core.common.data.room.entity.RecentUser
import com.uyscuti.social.core.common.data.room.repository.RecentUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentUserViewModel @Inject constructor(
    private val recentUserRepository: RecentUserRepository
) : ViewModel() {

    private val _selectedUserList = MutableLiveData<List<User>>()
    val selectedUserList: LiveData<List<User>> get() = _selectedUserList

    // Room delivers LiveData updates on Main automatically — no changes needed.
    val recentUsers: LiveData<List<RecentUser>> =
        recentUserRepository.getRecentUsersLiveData()

    // ─── Selected user list ───────────────────────────────────────────────────

    fun initializeSelectedUserList() {
        _selectedUserList.value = emptyList()
    }

    fun addUser(user: User) {
        val current = _selectedUserList.value.orEmpty().toMutableList()
        current.add(user)
        _selectedUserList.value = current
    }

    fun removeUser(user: User) {
        val current = _selectedUserList.value.orEmpty().toMutableList()
        current.remove(user)
        _selectedUserList.value = current
    }

    // ─── Room operations ──────────────────────────────────────────────────────

    // FIX: was a plain suspend fun called from the Activity with withContext(IO).
    // The ViewModel should own the coroutine scope and threading — callers just
    // call these functions normally from the Main thread.
    //
    // getRecentUsers() returns a value so we still need it as suspend so the
    // caller can await the result. The IO dispatch is now inside the repository.
    suspend fun getRecentUsers(): List<RecentUser> =
        recentUserRepository.getRecentUsers()

    // addRecentUser and deleteAll don't return values — fire-and-forget with
    // viewModelScope so the Activity doesn't need a coroutine at all.
    fun addRecentUser(user: RecentUser) {
        viewModelScope.launch {
            recentUserRepository.insertRecentUser(user)
        }
    }

    fun deleteAllRecentUsers() {
        viewModelScope.launch {
            recentUserRepository.deleteAllRecentUsers()
        }
    }
}