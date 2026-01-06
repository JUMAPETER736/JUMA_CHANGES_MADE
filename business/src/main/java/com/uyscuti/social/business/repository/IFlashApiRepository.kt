package com.uyscuti.social.business.repository

import com.uyscuti.social.business.model.business.BusinessProfile
import com.uyscuti.social.network.api.response.business.response.post.Post

interface IFlashApiRepository {
    suspend fun getAllCatalogues(page: String): Result<List<Post>>
    suspend fun getBusinessProfile(): Result<BusinessProfile>

    suspend fun getUnreadNotifications(): Int

}