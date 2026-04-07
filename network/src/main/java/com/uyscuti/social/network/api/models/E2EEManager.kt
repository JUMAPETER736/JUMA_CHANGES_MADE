package com.uyscuti.social.network.api.models

import android.content.Context
import android.util.Base64
import android.util.Log
import java.security.SecureRandom
import javax.crypto.SecretKey

class E2EEManager private constructor(
    private val context: Context) {


    companion object {
        @Volatile
        private var instance: E2EEManager? = null

        fun getInstance(context: Context): E2EEManager =
            instance ?: synchronized(this) {
                instance ?: E2EEManager(context.applicationContext).also { instance = it }
            }

        private const val PREF_REGISTRATION_ID = "signal_registration_id"
        private const val PREF_IDENTITY_KEY_PUB = "signal_identity_pub"
        private const val PREF_IDENTITY_KEY_PRIV = "signal_identity_priv"
        private const val PREF_SIGNED_PREKEY_ID = "signal_signed_prekey_id"
        private const val GCM_TAG_LENGTH =
            128   // Galois/Counter Mode authentication tag length in bits
        private const val GCM_IV_LENGTH =
            12    // Galois/Counter Mode initialisation vector length in bytes
        private const val NUM_ONE_TIME_PREKEYS = 100
        private const val DEVICE_ID = 1
    }


    private val TAG = "E2EEManager"
    private val prefs = context.getSharedPreferences("e2ee_keys_v2", Context.MODE_PRIVATE)
    private val random = SecureRandom()

    private var protocolStore: InMemorySignalProtocolStore? = null
    private var localAddress: SignalProtocolAddress? = null
    private val groupKeyCache = mutableMapOf<String, SecretKey>()
    private val sessionCiphers = mutableMapOf<String, SessionCipher>()


    fun initializeKeys(): DeviceKeys {
        // 1. Registration ID
        val registrationId = prefs.getInt(PREF_REGISTRATION_ID, -1).let { stored ->
            if (stored != -1) stored
            else generateRegistrationId().also {
                prefs.edit().putInt(PREF_REGISTRATION_ID, it).apply()
            }
        }

        // 2. Identity key pair (Curve25519 elliptic curve Diffie-Hellman key pair)
        val identityKeyPair = loadOrGenerateIdentityKeyPair()

        // 3. Signed pre-key (a medium-term Curve25519 key pair signed by the identity key)
        val signedPreKeyId = prefs.getInt(PREF_SIGNED_PREKEY_ID, 1)
        val signedPreKey   = generateSignedPreKey(identityKeyPair, signedPreKeyId)

        // 4. One-time pre-keys (single-use Curve25519 key pairs for X3DH Extended Triple Diffie-Hellman key agreement)
        val otpkStart      = prefs.getInt("otpk_start", 1)
        val oneTimePreKeys = generateOneTimePreKeys(otpkStart, NUM_ONE_TIME_PREKEYS)
        prefs.edit().putInt("otpk_start", otpkStart + NUM_ONE_TIME_PREKEYS).apply()

        // 5. Build in-memory Signal Protocol store (holds identity, signed pre-key, and one-time pre-keys)
        protocolStore = InMemorySignalProtocolStore(identityKeyPair, registrationId).apply {
            storeSignedPreKey(signedPreKeyId, signedPreKey)
            oneTimePreKeys.forEach { storePreKey(it.id, it) }
        }

        localAddress = SignalProtocolAddress(
            prefs.getString("my_user_id", "local") ?: "local", DEVICE_ID
        )

        // 6. Serialize keys to Base64 for server upload
        val identityPubB64 = Base64.encodeToString(
            identityKeyPair.publicKey.serialize(), Base64.NO_WRAP)
        val signedPubB64   = Base64.encodeToString(
            signedPreKey.keyPair.publicKey.serialize(), Base64.NO_WRAP)
        val signatureb64   = Base64.encodeToString(
            signedPreKey.signature, Base64.NO_WRAP)

        val otpkList = oneTimePreKeys.map { pk ->
            mapOf(
                "keyId"     to pk.id.toString(),
                "publicKey" to Base64.encodeToString(pk.keyPair.publicKey.serialize(), Base64.NO_WRAP)
            )
        }

        Log.d(TAG, "✅ Signal keys initialized, regId=$registrationId")

        return DeviceKeys(
            x25519PublicKey       = identityPubB64,
            ed25519PublicKey      = identityPubB64,
            keySignature          = signatureb64,
            registrationId        = registrationId,
            signedPreKeyId        = signedPreKeyId,
            signedPreKey          = signedPubB64,
            signedPreKeySignature = signatureb64,
            oneTimePreKeys        = otpkList
        )
    }


}