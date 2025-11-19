package com.uyscuti.social.core.common.data.room.repository

import androidx.lifecycle.LiveData
import com.uyscuti.social.core.common.data.room.dao.FollowListDao
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList


class ShortsFollowListRepository (private val followListItemDao: FollowListDao) {

    val allFollowListItems: LiveData<List<ShortsEntityFollowList>> = followListItemDao.getAllFollowListItemsLiveData()
    val allFollowShortsList: List<ShortsEntityFollowList> = followListItemDao.getAllFollowListItems()

    suspend fun insertFollowListItems(followList: List<ShortsEntityFollowList>) {
        followListItemDao.storeFollowListItems(followList)
    }

    suspend fun getFollowListItem(followersId: Long): ShortsEntityFollowList? {
        return followListItemDao.getFollowListItem(followersId)
    }

    suspend fun getAllFollowListItems(): List<ShortsEntityFollowList> {
        return followListItemDao.getAllFollowListItems()
    }

    suspend fun deleteFollowListItem(followersId: String) {
        followListItemDao.deleteFollowListItem(followersId)
    }
    suspend fun deleteFollowById(userId: String): Boolean {
        val rowsDeleted = followListItemDao.deleteFollowListItem(userId)
        return rowsDeleted > 0
    }
    suspend fun deleteAllFollowListItems() {
        followListItemDao.deleteAllFollowListItems()
    }

    suspend fun insertOrUpdateEntity(entity: ShortsEntityFollowList) {
        val existingEntity = followListItemDao.getEntityById(entity.followersId)

        if (existingEntity == null) {
            followListItemDao.storeFollowListItems(listOf(entity) )
        } else {
            followListItemDao.updateEntity(entity)
        }
    }

    suspend fun getEntityById(id: String): ShortsEntityFollowList? {
        return followListItemDao.getEntityById(id)
    }


}