package com.uyscuti.social.core.common.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uyscuti.social.core.common.data.room.entity.NotificationEntity

@Dao
interface NotificationDao{
    @Query("SELECT * FROM notifications")
    suspend fun getAllNotifications(): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

//    @Query("UPDATE notifications SET isRead = 1 ")
//    suspend fun markAsRead(id: String, isRead: Boolean = true)

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()
}
