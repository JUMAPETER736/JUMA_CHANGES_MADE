package com.uyscuti.social.network.api.request.business.create

data class WalkingBillboard(
    val enabled: Boolean,
    val liveLocationInfo: LocationInformation?
)