package com.uyscuti.social.network.api.models

import android.content.Context
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


}