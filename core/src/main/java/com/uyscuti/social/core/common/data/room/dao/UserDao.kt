package com.uyscuti.social.core.common.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uyscuti.social.core.common.data.room.entity.UserEntity


@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    fun getUserList(): LiveData<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(dialog: UserEntity)

    @Query("SELECT * FROM users")
    fun getAllUsers(): List<UserEntity>

    @Query("DELETE FROM users")
    fun deleteAll()
}