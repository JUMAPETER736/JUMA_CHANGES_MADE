package com.uyscuti.social.business.retro.response.background

import com.uyscuti.social.business.retro.response.background.BackgroundPhotoX
import com.uyscuti.social.business.retro.response.background.BackgroundVideo
import com.uyscuti.social.business.retro.response.background.ContactX
import com.uyscuti.social.business.retro.response.background.LocationX


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