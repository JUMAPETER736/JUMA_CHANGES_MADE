package com.uyscuti.social.core.common.data.room.repository

import androidx.lifecycle.LiveData
import com.uyscuti.social.core.common.data.room.dao.RecentUserDao
import com.uyscuti.social.core.common.data.room.entity.RecentUser

class RecentUserRepository(private val recentUserDao: RecentUserDao) {

    fun getRecentUsers(): List<RecentUser> = recentUserDao.getRecentUsers()

    fun getRecentUsersLiveData(): LiveData<List<RecentUser>> = recentUserDao.getRecentUserLiveData()

    fun insertRecentUser(recentUser: RecentUser) = recentUserDao.insertRecentUser(recentUser)

    fun deleteAllRecentUsers() = recentUserDao.deleteAll()
}