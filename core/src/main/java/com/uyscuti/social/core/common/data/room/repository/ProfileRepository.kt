package com.uyscuti.social.core.common.data.room.repository

import com.uyscuti.social.core.common.data.room.dao.ProfileDao
import com.uyscuti.social.core.common.data.room.entity.ProfileEntity

class ProfileRepository(private val profileDao: ProfileDao) {

    suspend fun insertProfile(myProfile: ProfileEntity) {
        profileDao.insertMyProfile(myProfile)
    }

    fun deleteMyProfile() {
        profileDao.deleteAll()
    }

}
