package com.uyscuti.social.network.api.models

import android.content.Context
import android.util.Base64
import android.util.Log
import java.security.SecureRandom
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec



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

        Log.d(TAG, "Signal keys initialized, regId=$registrationId")

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


    fun setMyUserId(userId: String) {
        prefs.edit().putString("my_user_id", userId).apply()
        localAddress = SignalProtocolAddress(userId, DEVICE_ID)
    }

    // Direct Message encryption (Signal Double Ratchet + X3DH Extended Triple Diffie-Hellman)

    fun encryptForDM(plaintext: String, recipient: RecipientPublicKey): EncryptedMessage {
        // If the recipient hasn't uploaded a full Signal bundle yet, fall back to legacy
        // Elliptic Curve Diffie-Hellman so messages still reach them while they upgrade
        if (!recipient.hasSignalBundle()) {
            Log.w(TAG, " ${recipient.userId} has no Signal bundle — using legacy Elliptic Curve Diffie-Hellman fallback")
            return encryptForDMLegacy(plaintext, recipient)
        }

        val store   = protocolStore ?: throw IllegalStateException("Keys not initialized")
        val address = SignalProtocolAddress(recipient.userId, DEVICE_ID)

        // If no session exists yet, perform X3DH Extended Triple Diffie-Hellman to establish one
        if (!store.containsSession(address)) {
            val bundle = buildPreKeyBundle(recipient)
            SessionBuilder(store, address).process(bundle)
            Log.d(TAG, "New Signal session with ${recipient.userId}")
        }

        val cipher    = sessionCiphers.getOrPut(recipient.userId) { SessionCipher(store, address) }
        val cipherMsg = cipher.encrypt(plaintext.toByteArray(Charsets.UTF_8))

        return EncryptedMessage(
            encryptedContent   = Base64.encodeToString(cipherMsg.serialize(), Base64.NO_WRAP),
            iv                 = "",   // Signal Double Ratchet manages its own initialisation vectors internally
            ephemeralPublicKey = "",   // Signal Double Ratchet manages ephemeral keys internally
            messageType        = cipherMsg.type
        )
    }

    fun decryptDM(
        encryptedContent: String,
        iv: String,
        ephemeralPublicKey: String,
        senderId: String,
        messageType: Int = 1
    ): String {
        val store   = protocolStore ?: throw IllegalStateException("Keys not initialized")
        val address = SignalProtocolAddress(senderId, DEVICE_ID)
        val cipher  = sessionCiphers.getOrPut(senderId) { SessionCipher(store, address) }
        val bytes   = Base64.decode(encryptedContent, Base64.NO_WRAP)

        return try {
            val plaintext = when (messageType) {
                // PreKey Signal Message: first message in a session, contains X3DH key material
                CiphertextMessage.PREKEY_TYPE -> cipher.decrypt(PreKeySignalMessage(bytes))
                // Whisper Message: subsequent Double Ratchet messages after session is established
                else                          -> cipher.decrypt(SignalMessage(bytes))
            }
            String(plaintext, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Signal decrypt failed: ${e.javaClass.simpleName}: ${e.message}")
            // Fallback for legacy Elliptic Curve Diffie-Hellman messages from old clients
            if (ephemeralPublicKey.isNotEmpty() && iv.isNotEmpty()) {
                decryptLegacyDM(encryptedContent, iv, ephemeralPublicKey)
            } else {
                throw e
            }
        }
    }

   // Group encryption (AES-256-GCM with per-group symmetric key)

    fun setupGroupKeys(recipients: List<RecipientPublicKey>): List<Map<String, String>> {
        // Generate a random 256-bit Advanced Encryption Standard key for the group
        val groupKeyBytes = ByteArray(32).also { random.nextBytes(it) }
        return recipients.mapNotNull { recipient ->
            try {
                // Wrap the group key for each recipient using Elliptic Curve Diffie-Hellman key agreement
                val enc = encryptGroupKeyForRecipient(groupKeyBytes, recipient)
                mapOf(
                    "participantId"      to recipient.userId,
                    "encryptedKey"       to enc.encryptedContent,
                    "nonce"              to enc.iv,
                    "ephemeralPublicKey" to enc.ephemeralPublicKey
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to wrap group key for ${recipient.userId}: ${e.message}")
                null
            }
        }
    }

    fun loadGroupKey(chatId: String, encryptedKey: String, nonce: String, ephemeralPublicKey: String) {
        // Unwrap the group Advanced Encryption Standard key using our local X25519 private key
        val groupKeyBytes = decryptLegacyDMBytes(encryptedKey, nonce, ephemeralPublicKey)
        groupKeyCache[chatId] = SecretKeySpec(groupKeyBytes, "AES")
        Log.d(TAG, "Group key loaded for $chatId")
    }

    fun hasGroupKey(chatId: String) = groupKeyCache.containsKey(chatId)

    fun encryptForGroup(plaintext: String, chatId: String): EncryptedMessage {
        val key = groupKeyCache[chatId] ?: throw IllegalStateException("No group key for $chatId")
        // Generate a fresh 96-bit initialisation vector for each message (required by Galois/Counter Mode)
        val iv  = generateIV()
        return EncryptedMessage(
            encryptedContent = Base64.encodeToString(
                aesGCMEncrypt(plaintext.toByteArray(Charsets.UTF_8), key, iv), Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    fun decryptGroup(encryptedContent: String, iv: String, chatId: String): String {
        val key = groupKeyCache[chatId] ?: throw IllegalStateException("No group key for $chatId")
        return String(
            aesGCMDecrypt(
                Base64.decode(encryptedContent, Base64.NO_WRAP),
                key,
                Base64.decode(iv, Base64.NO_WRAP)
            ), Charsets.UTF_8
        )
    }


}