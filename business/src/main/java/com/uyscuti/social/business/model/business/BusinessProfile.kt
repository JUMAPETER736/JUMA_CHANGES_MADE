package com.uyscuti.social.business.model.business


data class BusinessProfile(
    val _id: String,
    val businessDescription: String,
    var businessName: String,
    val businessType: String,
    val owner: String,
)
