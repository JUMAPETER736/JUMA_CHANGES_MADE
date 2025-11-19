package com.uyscuti.social.core.common.data.room.repository

import com.uyscuti.social.core.common.data.room.dao.LocalUserDao
import com.uyscuti.social.core.common.data.room.entity.LocalUserEntity

class LocalUserRepository(private val localUserDao: LocalUserDao) {

    suspend fun insertLocalUser(localUser: LocalUserEntity) {
        localUserDao.insertUser(localUser)
    }

    suspend fun checkLocalUser(): LocalUserEntity? {
        return localUserDao.checkUser()
    }
    suspend fun clearLocalUser(){
        localUserDao.deleteAll()
    }
}