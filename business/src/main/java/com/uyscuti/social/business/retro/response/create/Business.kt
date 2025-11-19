package com.uyscuti.social.business.retro.response.create

import com.uyscuti.social.business.retro.response.get.BackgroundVideo
import com.uyscuti.social.network.api.request.business.create.Contact

data class Business(
    val __v: Int,
    val _id: String,
    val backgroundPhoto: BackgroundPhoto,
    val backgroundVideo: BackgroundVideo,
    val businessCatalogue: List<BusinessCatalogue>,
    val businessDescription: String,
    val businessName: String,
    val businessType: String,
    val contact: Contact,
    val createdAt: String,
    val location: Location,
    val owner: String,
    val updatedAt: String
)