package com.uyscuti.social.business.retro.response.background

import com.uyscuti.social.business.retro.response.background.BackgroundPhoto
import com.uyscuti.social.business.retro.response.background.BackgroundVideo
import com.uyscuti.social.business.retro.response.background.Contact
import com.uyscuti.social.business.retro.response.background.Location

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