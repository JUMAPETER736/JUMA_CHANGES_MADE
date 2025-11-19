package com.uyscuti.social.core.common.data.room.dao

import androidx.room.Dao
import androidx.room.Query


@Dao
interface TabDao {

    @Query("UPDATE tabs SET unreadCount = + 1 WHERE id = :tab")
    fun incrementTabUnreadCount(tab: String)

    @Query("UPDATE tabs SET unreadCount = - 1 WHERE id = :tab")
    fun decrementTabUnreadCount(tab: String)


    // Add a query method to get the unread count for a specific tab based on its `id`.
    @Query("SELECT unreadCount FROM tabs WHERE id = :tabId")
    fun getUnreadCount(tabId: String): Int

    @Query("DELETE FROM tabs")
    suspend fun deleteAll()

}