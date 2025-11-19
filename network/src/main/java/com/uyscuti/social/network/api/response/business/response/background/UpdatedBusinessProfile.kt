package com.uyscuti.social.network.api.response.business.response.background

import com.uyscuti.social.network.api.response.business.response.background.BackgroundPhoto
import com.uyscuti.social.network.api.response.business.response.background.BackgroundVideo
import com.uyscuti.social.network.api.response.business.response.background.Contact
import com.uyscuti.social.network.api.response.business.response.background.Location


data class UpdatedBusinessProfile(
    val __v: Int,
    val _id: String,
    val backgroundPhoto: BackgroundPhoto,
    val backgroundVideo: BackgroundVideo,
    val businessCatalogue: List<Any>,
    val businessDescription: String,
    val businessName: String,
    val businessType: String,
    val contact: Contact,
    val createdAt: String,
    val location: Location,
    val owner: String,
    val updatedAt: String
)