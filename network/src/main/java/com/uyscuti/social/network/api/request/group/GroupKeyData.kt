package com.uyscuti.social.network.api.request.group

data class GroupKeyData(
    val participantId: String,
    val encryptedKey: String,
    val nonce: String,
    val ephemeralPublicKey: String
)