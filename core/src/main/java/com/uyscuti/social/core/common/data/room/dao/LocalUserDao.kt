package com.uyscuti.social.core.common.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uyscuti.social.core.common.data.room.entity.LocalUserEntity


@Dao
interface LocalUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: LocalUserEntity): Long

    @Query("SELECT * FROM local_user LIMIT 1")
    fun getUser(): LiveData<LocalUserEntity?> // LiveData or User? depending on your use case

    @Query("SELECT * FROM local_user LIMIT 1")
    fun checkUser(): LocalUserEntity?


    @Query("DELETE FROM local_user")
    fun deleteAll()
}