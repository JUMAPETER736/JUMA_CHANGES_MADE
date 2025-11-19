package com.uyscuti.social.business.retro.request.create

import com.uyscuti.social.network.api.request.business.create.Contact

data class CreateBusinessProfile(
    val backgroundPhoto: BackgroundPhoto,
    val businessCatalogue: List<BusinessCatalogue>,
    val businessDescription: String,
    val businessName: String,
    val businessType: String,
    val contact: Contact,
    val location: Location
)