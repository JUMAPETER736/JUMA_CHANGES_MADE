package com.uyscuti.social.network.api.request.business.create

data class CreateBusinessProfile(
    val backgroundPhoto: BackgroundPhoto,
    val businessCatalogue: List<BusinessCatalogue>,
    val businessDescription: String,
    val businessName: String,
    val businessType: String,
    val contact: Contact,
    val location: Location
)