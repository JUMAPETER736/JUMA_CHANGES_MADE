package com.uyscuti.social.business.retro.response.profile

import com.uyscuti.social.business.retro.response.profile.LiveLocationInfo

data class WalkingBillboard(
    val enabled: Boolean,
    val liveLocationInfo: LiveLocationInfo
)