package com.uyscuti.social.core.common.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uyscuti.social.core.common.data.room.entity.ProfileEntity

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMyProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profile")
    fun getMyProfile(): ProfileEntity

    @Query("DELETE FROM profile")
    fun deleteAll()
}
