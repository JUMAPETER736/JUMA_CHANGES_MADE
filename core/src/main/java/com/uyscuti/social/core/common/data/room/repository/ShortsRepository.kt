package com.uyscuti.social.core.common.data.room.repository

import androidx.lifecycle.LiveData
import com.uyscuti.social.core.common.data.room.dao.ShortsDao
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity


class ShortsRepository(private val shortsDao: ShortsDao) {

    val allShorts: LiveData<List<ShortsEntity>> = shortsDao.getAllShorts()


    fun getStoredShorts(): LiveData<List<ShortsEntity>> {
        return shortsDao.getAllShorts()
    }

    suspend fun getShortsForPage(page: Int, pageSize: Int): List<UserShortsEntity> {
        val offset = (page - 1) * pageSize
        return shortsDao.getShortsForPage(pageSize, offset)
    }


    suspend fun addAllShorts(shortsEntity: List<ShortsEntity>) {
        shortsDao.storeShorts(shortsEntity)
    }

    suspend fun allShortsList(): List<ShortsEntity> {
        return shortsDao.getAllShortsList()
    }

    suspend fun addUserProfileShorts(shortsEntity: List<UserShortsEntity>) {
        shortsDao.storeUserProfileShorts(shortsEntity)
    }



}