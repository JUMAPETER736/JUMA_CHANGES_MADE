package com.uyscuti.social.network.api.response.business.response.background

import com.uyscuti.social.network.api.response.business.response.background.BackgroundPhotoX
import com.uyscuti.social.network.api.response.business.response.background.BackgroundVideo
import com.uyscuti.social.network.api.response.business.response.background.ContactX
import com.uyscuti.social.network.api.response.business.response.background.LocationX


data class UpdatedBusinessProfileX(
    val __v: Int,
    val _id: String,
    val backgroundPhoto: BackgroundPhotoX,
    val backgroundVideo: BackgroundVideo,
    val businessCatalogue: List<Any>,
    val businessDescription: String,
    val businessName: String,
    val businessType: String,
    val contact: ContactX,
    val createdAt: String,
    val location: LocationX,
    val owner: String,
    val updatedAt: String
)