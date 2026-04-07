package com.uyscuti.social.network.api.models

data class DeviceKeys(
    val x25519PublicKey: String,
    val ed25519PublicKey: String,
    val keySignature: String,
    val registrationId: Int = 0,
    val signedPreKeyId: Int = 0,
    val signedPreKey: String = "",
    val signedPreKeySignature: String = "",
    val oneTimePreKeys: List<Map<String, String>> = emptyList()
)