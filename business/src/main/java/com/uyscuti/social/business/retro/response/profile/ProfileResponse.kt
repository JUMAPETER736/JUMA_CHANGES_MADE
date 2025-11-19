package com.uyscuti.social.business.retro.response.profile

import com.uyscuti.social.business.retro.response.profile.BackgroundPhoto
import com.uyscuti.social.business.retro.response.profile.BackgroundVideo
import com.uyscuti.social.business.retro.response.profile.Contact
import com.uyscuti.social.business.retro.response.profile.Location

data class ProfileResponse(
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