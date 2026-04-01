package com.uyscuti.social.network.api.request.group

data class RecipientKeyData(
    val x25519PublicKey: String?,
    val ed25519PublicKey: String?,
    val keySignature: String?,
    val registrationId: Int = 0,
    val signedPreKeyId: Int = 0,
    val signedPreKey: String = "",
    val signedPreKeySignature: String = "",
    val oneTimePreKeyId: Int = 0,
    val oneTimePreKey: String? = null
)