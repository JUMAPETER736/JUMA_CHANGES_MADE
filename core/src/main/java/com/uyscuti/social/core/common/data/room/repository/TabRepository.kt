package com.uyscuti.social.core.common.data.room.repository

import com.uyscuti.social.core.common.data.room.dao.TabDao

class TabRepository(private val tabDao: TabDao) {

    // Add a method to get the unread count for a specific tab.
    suspend fun getUnreadCountForTab(tabId: String): Int {
        return tabDao.getUnreadCount(tabId)
    }

    // Add a method to increment the unread count for a specific tab.
    suspend fun incrementUnreadCountForTab(tabId: String) {
        tabDao.incrementTabUnreadCount(tabId)
    }

    // Add a method to decrement the unread count for a specific tab.
    suspend fun decrementUnreadCountForTab(tabId: String) {
        tabDao.decrementTabUnreadCount(tabId)
    }

    suspend fun clearAll(){
        tabDao.deleteAll()
    }

}