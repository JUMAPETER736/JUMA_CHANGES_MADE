package com.uyscuti.social.core.common.data.room.repository

import androidx.lifecycle.LiveData
import com.uyscuti.social.core.common.data.room.dao.ShortsDao
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity


class ShortsRepository(private val shortsDao: ShortsDao) {

    val allShorts: LiveData<List<ShortsEntity>> = shortsDao.getAllShorts()
//    val allShortsList: List<ShortsEntity> = shortsDao.getAllShortsList()
//    val allUserShortsList: List<UserShortsEntity> = shortsDao.getUserAllShortsList()

    fun getStoredShorts(): LiveData<List<ShortsEntity>> {
        return shortsDao.getAllShorts()
    }

    suspend fun getShortsForPage(page: Int, pageSize: Int): List<UserShortsEntity> {
        val offset = (page - 1) * pageSize
        return shortsDao.getShortsForPage(pageSize, offset)
    }
//    fun getUserProfileStoredShorts(): List<UserShortsEntity> {
//        return shortsDao.getUserAllShortsList()
//    }

    suspend fun addAllShorts(shortsEntity: List<ShortsEntity>) {
        shortsDao.storeShorts(shortsEntity)
    }

    suspend fun allShortsList(): List<ShortsEntity> {
        return shortsDao.getAllShortsList()
    }
//    suspend fun deleteAll() {
//        shortsDao.deleteAll()
//    }
    suspend fun addUserProfileShorts(shortsEntity: List<UserShortsEntity>) {
        shortsDao.storeUserProfileShorts(shortsEntity)
    }


//    fun getUsers(): List<UserEntity> {
//        // Retrieve all users from Room database
//        return shortsDao.getAllUsers()
//    }
//    fun deleteMyProfile() {
//        usersDao.deleteAll()
//    }

}