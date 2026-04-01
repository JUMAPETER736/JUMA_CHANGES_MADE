package com.uyscuti.social.network.api.request.group

data class StoreGroupKeysRequest(
    val encryptedGroupKeys: List<Map<String, String>>
)