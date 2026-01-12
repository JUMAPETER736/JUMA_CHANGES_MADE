package com.uyscuti.social.network.api.response.business.response.profile

import com.uyscuti.social.network.api.response.business.response.profile.LiveLocationInfo

data class WalkingBillboard(
    val enabled: Boolean,
    val liveLocationInfo: LiveLocationInfo
)