package com.uyscuti.social.core.common.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uyscuti.social.core.common.data.room.entity.LocalUserEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity

@Dao
interface ShortsDao {

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    suspend fun storeShorts(shorts: List<ShortsEntity>)

//    @Insert(onConflict =  OnConflictStrategy.REPLACE)
//    suspend fun storeFollowListShorts(shorts: List<FollowListItem>)

    @Insert(onConflict =  OnConflictStrategy.IGNORE)
    suspend fun storeUserProfileShorts(shorts: List<UserShortsEntity>)

    @Update
    suspend fun updateShortsList(shorts: List<ShortsEntity>)

    @Query("SELECT * FROM shorts")
    fun getAllShorts(): LiveData<List<ShortsEntity>>

    @Query("SELECT * FROM shorts")
    suspend fun getAllShortsList(): List<ShortsEntity>

//    @Query("SELECT * FROM userShorts")
//   suspend fun getUserAllShortsList(): List<UserShortsEntity>

    @Query("SELECT * FROM userShorts LIMIT :pageSize OFFSET :offset")
    suspend fun getShortsForPage(pageSize: Int, offset: Int): List<UserShortsEntity>
//    @Query("SELECT * FROM shorts_table")
//    fun getAllUserShorts(): LiveData<List<ShortsEntity>>


    @Query("SELECT * FROM local_user LIMIT 1")
    fun getUser(): LiveData<LocalUserEntity?>

    @Query("DELETE FROM shorts")
    fun deleteAll()
}