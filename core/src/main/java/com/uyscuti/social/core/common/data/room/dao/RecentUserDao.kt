package com.uyscuti.social.core.common.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uyscuti.social.core.common.data.room.entity.RecentUser

@Dao
interface RecentUserDao {

    @Query("SELECT * FROM recent_users")
    fun getRecentUserLiveData(): LiveData<List<RecentUser>>

    @Query("SELECT * FROM recent_users ORDER BY dateAdded DESC")
    fun getRecentUsers(): List<RecentUser>

//    @Query("SELECT * FROM recent_users")
//    fun getRecentUsers(): List<RecentUser>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecentUser(dialog: RecentUser)

    @Query("DELETE FROM recent_users")
    fun deleteAll()

}