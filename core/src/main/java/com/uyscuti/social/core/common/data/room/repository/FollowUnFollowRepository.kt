package com.uyscuti.social.core.common.data.room.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.uyscuti.social.core.common.data.room.dao.FollowUnFollowDao
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity

private const val TAG = "FollowUnFollowRepository"
class FollowUnFollowRepository(private val followDao: FollowUnFollowDao) {

    val allFollows: LiveData<List<FollowUnFollowEntity>> = followDao.getAllFollows()

    fun getFollowStatus(userId: String): LiveData<FollowUnFollowEntity?> {
        return followDao.getFollowStatus(userId)
    }

    suspend fun insertOrUpdateFollow(follow: FollowUnFollowEntity) {
        Log.d(TAG, "Inserting or updating follow: $follow")

        followDao.insertOrUpdateFollow(follow)

        Log.d(TAG, "Follow inserted or updated successfully")

    }

//    suspend fun deleteFollowById(userId: String) {
//        followDao.deleteFollowById(userId)
//    }
    suspend fun deleteFollowById(userId: String): Boolean {
        val rowsDeleted = followDao.deleteFollowById(userId)
        return rowsDeleted > 0
    }
}