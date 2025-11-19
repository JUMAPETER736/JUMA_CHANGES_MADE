package com.uyscuti.social.core.common.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList


@Dao
interface FollowListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun storeFollowListItems(followList: List<ShortsEntityFollowList>)

    @Query("SELECT * FROM shorts_follow_list")
    fun getAllFollowListItems(): List<ShortsEntityFollowList>

    @Query("SELECT * FROM shorts_follow_list WHERE followersId = :followersId")
    suspend fun getFollowListItem(followersId: Long): ShortsEntityFollowList?

    @Query("SELECT * FROM shorts_follow_list")
    fun getAllFollowListItemsLiveData(): LiveData<List<ShortsEntityFollowList>>

    @Update
    suspend fun updateEntity(entity: ShortsEntityFollowList)

    @Query("SELECT * FROM shorts_follow_list WHERE followersId = :id")
    suspend fun getEntityById(id: String): ShortsEntityFollowList?

    @Query("DELETE FROM shorts_follow_list WHERE followersId = :followersId")
    suspend fun deleteFollowListItem(followersId: String): Int

    @Query("DELETE FROM shorts_follow_list")
    suspend fun deleteAllFollowListItems()

}