package com.uyscuti.social.circuit.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uyscuti.social.circuit.data.model.User
import com.uyscuti.social.core.common.data.room.entity.RecentUser
import com.uyscuti.social.core.common.data.room.repository.RecentUserRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class RecentUserViewModel @Inject constructor(private val recentUserRepository: RecentUserRepository) : ViewModel() {

    private val _selectedUserList: MutableLiveData<List<User>> = MutableLiveData()

    private val _users: LiveData<List<RecentUser>> = recentUserRepository.getRecentUsersLiveData()

    // Expose the LiveData to the UI
    val recentUsers: LiveData<List<RecentUser>>
        get() = _users

    // Getter for the LiveData (read-only access)
    val selectedUserList: LiveData<List<User>> get() = _selectedUserList

    // Setter for the MutableLiveData (write access)
    private fun setSelectedUserList(users: List<User>) {
        _selectedUserList.value = users
    }

    // Function to initialize selectedUserList with an empty list
    fun initializeSelectedUserList() {
        setSelectedUserList(emptyList())
    }

    // Function to add a user to the list
    fun addUser(user: User) {
        val currentList = _selectedUserList.value.orEmpty().toMutableList()
        currentList.add(user)
        setSelectedUserList(currentList)
    }

    // Function to remove a user from the list
    fun removeUser(user: User) {
        val currentList = _selectedUserList.value.orEmpty().toMutableList()
        currentList.remove(user)
        setSelectedUserList(currentList)
    }

    suspend fun addRecentUser(user: RecentUser){
        recentUserRepository.insertRecentUser(user)
    }

    fun getRecentUsers(): List<RecentUser>{
        return recentUserRepository.getRecentUsers()
    }
}