package com.uyscuti.social.core.common.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity

@Dao
interface FollowUnFollowDao {
    @Query("SELECT * FROM follow_table")
    fun getAllFollows(): LiveData<List<FollowUnFollowEntity>>
//
//    @Query("SELECT * FROM follow_table WHERE userId = :userId")
//    fun getFollowUnFollowById(userId: String): LiveData<FollowUnFollowEntity?>

    @Query("SELECT * FROM follow_table WHERE userId = :userId")
    fun getFollowStatus(userId: String): LiveData<FollowUnFollowEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateFollow(follow: FollowUnFollowEntity)

    @Query("DELETE FROM follow_table WHERE userId = :userId")
    suspend fun deleteFollowById(userId: String): Int
}