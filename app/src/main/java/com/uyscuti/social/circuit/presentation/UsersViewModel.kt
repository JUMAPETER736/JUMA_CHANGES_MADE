package com.uyscuti.social.circuit.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.core.common.data.room.repository.UsersRepository


import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UsersViewModel  @Inject constructor(private val usersRepository: UsersRepository) : ViewModel() {

    private val _users: LiveData<List<UserEntity>> = usersRepository.getUsersLD()

    // Expose the LiveData to the UI
    val users: LiveData<List<UserEntity>>
        get() = _users

    suspend fun addUsers(user: UserEntity){
        usersRepository.addAllUsers(user)
    }

    fun getUsers(): List<UserEntity>{
        return usersRepository.getUsers()
    }


}
