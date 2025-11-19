package com.uyscuti.social.network.api.response.business.response.get

data class GetMyBusinessProfile(
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