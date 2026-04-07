package com.uyscuti.social.network.api.models

data class RecipientPublicKey(
    val userId: String,
    val x25519PublicKey: String,
    val ed25519PublicKey: String = "",
    val keySignature: String = "",
    val registrationId: Int = 0,
    val signedPreKeyId: Int = 0,
    val signedPreKey: String = "",
    val signedPreKeySignature: String = "",
    val oneTimePreKeyId: Int = 0,
    val oneTimePreKey: String? = null
) {
    // Returns true only when the recipient has uploaded a full Signal Protocol key bundle.
    // If signedPreKey is missing the server hasn't received their Signal keys yet
    // and we must fall back to legacy Elliptic Curve Diffie-Hellman encryption.
    fun hasSignalBundle(): Boolean =
        signedPreKey.isNotEmpty() && signedPreKeySignature.isNotEmpty() && registrationId > 0
}