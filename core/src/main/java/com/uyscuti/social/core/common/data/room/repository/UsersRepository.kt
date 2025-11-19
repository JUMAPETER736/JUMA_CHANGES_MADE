package com.uyscuti.social.core.common.data.room.repository

import androidx.lifecycle.LiveData
import com.uyscuti.social.core.common.data.room.dao.UserDao
import com.uyscuti.social.core.common.data.room.entity.UserEntity

class UsersRepository(private val usersDao: UserDao) {

    fun getUsersLD(): LiveData<List<UserEntity>>{
        return usersDao.getUserList()
    }

    suspend fun addAllUsers(users: UserEntity) {
        usersDao.insertUser(users)
    }


    fun getUsers(): List<UserEntity> {
        // Retrieve all users from Room database
        return usersDao.getAllUsers()
    }
    fun deleteMyProfile() {
        usersDao.deleteAll()
    }

}