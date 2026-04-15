package com.uyscuti.social.core.common.data.room.repository

import androidx.lifecycle.LiveData
import com.uyscuti.social.core.common.data.room.dao.RecentUserDao
import com.uyscuti.social.core.common.data.room.entity.RecentUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecentUserRepository(private val recentUserDao: RecentUserDao) {

    // FIX: Room cannot run queries on the Main thread.
    // Made suspend + withContext(Dispatchers.IO) so the caller never needs to
    // worry about dispatching — the repository owns its own threading.
    suspend fun getRecentUsers(): List<RecentUser> =
        withContext(Dispatchers.IO) {
            recentUserDao.getRecentUsers()
        }

    // LiveData is fine as-is — Room automatically delivers on Main.
    fun getRecentUsersLiveData(): LiveData<List<RecentUser>> =
        recentUserDao.getRecentUserLiveData()

    // FIX: same — suspend + IO so callers stay on Main safely.
    suspend fun insertRecentUser(recentUser: RecentUser) =
        withContext(Dispatchers.IO) {
            recentUserDao.insertRecentUser(recentUser)
        }

    suspend fun deleteAllRecentUsers() =
        withContext(Dispatchers.IO) {
            recentUserDao.deleteAll()
        }
}