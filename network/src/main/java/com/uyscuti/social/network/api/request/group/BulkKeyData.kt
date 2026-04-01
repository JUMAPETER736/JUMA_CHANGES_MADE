package com.uyscuti.social.network.api.request.group

data class BulkKeyData(
    val keyMap: Map<String, RecipientKeyData>,
    val missingUsers: List<String>
)