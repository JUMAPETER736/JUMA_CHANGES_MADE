package com.uyscuti.social.network.api.models

import android.content.Context
import android.util.Base64
import android.util.Log
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.SessionBuilder
import org.signal.libsignal.protocol.SessionCipher
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.ecc.Curve
import org.signal.libsignal.protocol.message.CiphertextMessage
import org.signal.libsignal.protocol.message.PreKeySignalMessage
import org.signal.libsignal.protocol.message.SignalMessage
import org.signal.libsignal.protocol.state.PreKeyBundle
import org.signal.libsignal.protocol.state.PreKeyRecord
import org.signal.libsignal.protocol.state.SignedPreKeyRecord
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
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

    // Signal key generation (libsignal-android API)

    private fun generateRegistrationId(): Int =
        (random.nextInt(16380) + 1) // Valid Signal Protocol registration ID range: 1–16380

    private fun loadOrGenerateIdentityKeyPair(): IdentityKeyPair {
        val pubB64  = prefs.getString(PREF_IDENTITY_KEY_PUB,  null)
        val privB64 = prefs.getString(PREF_IDENTITY_KEY_PRIV, null)

        return if (pubB64 != null && privB64 != null) {
            try {
                val pubBytes  = Base64.decode(pubB64,  Base64.NO_WRAP)
                val privBytes = Base64.decode(privB64, Base64.NO_WRAP)
                val publicKey  = IdentityKey(pubBytes, 0)
                val privateKey = Curve.decodePrivatePoint(privBytes)
                IdentityKeyPair(publicKey, privateKey)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load identity keys, regenerating: ${e.message}")
                generateAndSaveIdentityKeyPair()
            }
        } else {
            generateAndSaveIdentityKeyPair()
        }
    }

    private fun generateAndSaveIdentityKeyPair(): IdentityKeyPair {
        // Generate a Curve25519 elliptic curve key pair for long-term identity
        val ecKeyPair   = Curve.generateKeyPair()
        val identityKey = IdentityKey(ecKeyPair.publicKey)
        val idKeyPair   = IdentityKeyPair(identityKey, ecKeyPair.privateKey)

        prefs.edit()
            .putString(PREF_IDENTITY_KEY_PUB,
                Base64.encodeToString(identityKey.serialize(), Base64.NO_WRAP))
            .putString(PREF_IDENTITY_KEY_PRIV,
                Base64.encodeToString(ecKeyPair.privateKey.serialize(), Base64.NO_WRAP))
            .apply()

        return idKeyPair
    }



    private fun generateSignedPreKey(identityKeyPair: IdentityKeyPair, id: Int): SignedPreKeyRecord {
        // Generate a Curve25519 key pair and sign its public key with the identity private key
        val keyPair   = Curve.generateKeyPair()
        val timestamp = System.currentTimeMillis()
        val signature = Curve.calculateSignature(
            identityKeyPair.privateKey,
            keyPair.publicKey.serialize()
        )
        return SignedPreKeyRecord(id, timestamp, keyPair, signature)
    }

    private fun generateOneTimePreKeys(start: Int, count: Int): List<PreKeyRecord> =
        // Each one-time pre-key is a single-use Curve25519 key pair consumed during X3DH
        (start until start + count).map { id ->
            PreKeyRecord(id, Curve.generateKeyPair())
        }

    private fun buildPreKeyBundle(recipient: RecipientPublicKey): PreKeyBundle {
        // Assemble the recipient's public key material needed for X3DH Extended Triple Diffie-Hellman
        val identityKey  = IdentityKey(Base64.decode(recipient.x25519PublicKey, Base64.NO_WRAP), 0)
        val signedPubKey = Curve.decodePoint(Base64.decode(recipient.signedPreKey, Base64.NO_WRAP), 0)
        val signature    = Base64.decode(recipient.signedPreKeySignature, Base64.NO_WRAP)

        return if (recipient.oneTimePreKey != null) {
            val otpk = Curve.decodePoint(Base64.decode(recipient.oneTimePreKey, Base64.NO_WRAP), 0)
            PreKeyBundle(
                recipient.registrationId, DEVICE_ID,
                recipient.oneTimePreKeyId, otpk,
                recipient.signedPreKeyId, signedPubKey, signature,
                identityKey
            )
        } else {
            // No one-time pre-key available — X3DH proceeds with 3 Diffie-Hellman exchanges instead of 4
            PreKeyBundle(
                recipient.registrationId, DEVICE_ID,
                -1, null,
                recipient.signedPreKeyId, signedPubKey, signature,
                identityKey
            )
        }
    }

    private fun loadJavaX25519KeyPair(): java.security.KeyPair {
        val pubB64  = prefs.getString("x25519_public",  null)
        val privB64 = prefs.getString("x25519_private", null)

        // Generate and persist a new X25519 key pair if none is stored
        if (pubB64 == null || privB64 == null) {
            val kp = generateJavaX25519KeyPair()
            prefs.edit()
                .putString("x25519_public",  Base64.encodeToString(kp.public.encoded,  Base64.NO_WRAP))
                .putString("x25519_private", Base64.encodeToString(kp.private.encoded, Base64.NO_WRAP))
                .apply()
            return kp
        }

        val pubBytes  = Base64.decode(pubB64,  Base64.NO_WRAP)
        val privBytes = Base64.decode(privB64, Base64.NO_WRAP)
        return try {
            // Decode as X25519 SubjectPublicKeyInfo / PKCS#8 encoded keys
            val kf = java.security.KeyFactory.getInstance("X25519")
            java.security.KeyPair(
                kf.generatePublic(java.security.spec.X509EncodedKeySpec(pubBytes)),
                kf.generatePrivate(java.security.spec.PKCS8EncodedKeySpec(privBytes))
            )
        } catch (e: Exception) {
            // Fall back to Elliptic Curve P-256 if X25519 is unavailable on this device
            val kf = java.security.KeyFactory.getInstance("EC")
            java.security.KeyPair(
                kf.generatePublic(java.security.spec.X509EncodedKeySpec(pubBytes)),
                kf.generatePrivate(java.security.spec.PKCS8EncodedKeySpec(privBytes))
            )
        }
    }


    // Legacy Elliptic Curve Diffie-Hellman (backward compatibility for old clients)

    fun encryptForDMLegacy(plaintext: String, recipient: RecipientPublicKey): EncryptedMessage {
        val ephemeral    = generateJavaX25519KeyPair()
        val recipientPub = decodeJavaX25519PublicKey(recipient.x25519PublicKey)
        val sharedSecret = performJavaECDH(ephemeral.private, recipientPub)
        val aesKey       = deriveAESKeyHKDF(sharedSecret, "DM_ENCRYPTION")
        val iv           = generateIV()
        return EncryptedMessage(
            encryptedContent   = Base64.encodeToString(
                aesGCMEncrypt(plaintext.toByteArray(Charsets.UTF_8), aesKey, iv), Base64.NO_WRAP),
            iv                 = Base64.encodeToString(iv, Base64.NO_WRAP),
            ephemeralPublicKey = Base64.encodeToString(ephemeral.public.encoded, Base64.NO_WRAP)
        )
    }

    private fun decryptLegacyDM(encryptedContent: String, iv: String, ephemeralPublicKey: String): String {
        val myKeyPair    = loadJavaX25519KeyPair()
        val ephemeralPub = decodeJavaX25519PublicKey(ephemeralPublicKey)
        val sharedSecret = performJavaECDH(myKeyPair.private, ephemeralPub)
        val aesKey       = deriveAESKeyHKDF(sharedSecret, "DM_ENCRYPTION")
        return String(
            aesGCMDecrypt(
                Base64.decode(encryptedContent, Base64.NO_WRAP),
                aesKey,
                Base64.decode(iv, Base64.NO_WRAP)
            ), Charsets.UTF_8
        )
    }


    private fun decryptLegacyDMBytes(encryptedContent: String, iv: String, ephemeralPublicKey: String): ByteArray {
        // Used to unwrap group Advanced Encryption Standard keys encrypted with legacy Elliptic Curve Diffie-Hellman
        val myKeyPair    = loadJavaX25519KeyPair()
        val ephemeralPub = decodeJavaX25519PublicKey(ephemeralPublicKey)
        val sharedSecret = performJavaECDH(myKeyPair.private, ephemeralPub)
        val aesKey       = deriveAESKeyHKDF(sharedSecret, "GROUP_KEY_WRAP")
        return aesGCMDecrypt(
            Base64.decode(encryptedContent, Base64.NO_WRAP),
            aesKey,
            Base64.decode(iv, Base64.NO_WRAP)
        )
    }

    private fun encryptGroupKeyForRecipient(groupKeyBytes: ByteArray, recipient: RecipientPublicKey): EncryptedMessage {
        // Wrap the group Advanced Encryption Standard key using ephemeral X25519 Elliptic Curve Diffie-Hellman
        val ephemeral    = generateJavaX25519KeyPair()
        val recipientPub = decodeJavaX25519PublicKey(recipient.x25519PublicKey)
        val sharedSecret = performJavaECDH(ephemeral.private, recipientPub)
        val wrapKey      = deriveAESKeyHKDF(sharedSecret, "GROUP_KEY_WRAP")
        val iv           = generateIV()
        return EncryptedMessage(
            encryptedContent   = Base64.encodeToString(aesGCMEncrypt(groupKeyBytes, wrapKey, iv), Base64.NO_WRAP),
            iv                 = Base64.encodeToString(iv, Base64.NO_WRAP),
            ephemeralPublicKey = Base64.encodeToString(ephemeral.public.encoded, Base64.NO_WRAP)
        )
    }


    // HKDF Hash-based Key Derivation Function (RFC 5869)

    private fun deriveAESKeyHKDF(sharedSecret: ByteArray, info: String): SecretKey {
        val salt = "CircuitApp-E2EE-v1".toByteArray(Charsets.UTF_8)
        val hmac = Mac.getInstance("HmacSHA256")

        // Extract phase: compress the Diffie-Hellman shared secret into a pseudorandom key
        hmac.init(SecretKeySpec(salt, "HmacSHA256"))
        val prk = hmac.doFinal(sharedSecret)

        // Expand phase: stretch the pseudorandom key into a 256-bit Advanced Encryption Standard key
        hmac.init(SecretKeySpec(prk, "HmacSHA256"))
        val okm = hmac.doFinal(info.toByteArray(Charsets.UTF_8) + byteArrayOf(0x01))

        return SecretKeySpec(okm.copyOf(32), "AES")
    }

    // Java X25519 helpers (used for group key wrapping and legacy Elliptic Curve Diffie-Hellman)

    private fun generateJavaX25519KeyPair(): java.security.KeyPair =
        try {
            // Prefer X25519 (Curve25519 Diffie-Hellman); fall back to P-256 on older Android versions
            java.security.KeyPairGenerator.getInstance("X25519").generateKeyPair()
        } catch (e: Exception) {
            java.security.KeyPairGenerator.getInstance("EC").apply {
                initialize(256, random)
            }.generateKeyPair()
        }



    private fun decodeJavaX25519PublicKey(base64: String): java.security.PublicKey {
        val bytes = Base64.decode(base64, Base64.NO_WRAP)
        return try {
            java.security.KeyFactory.getInstance("X25519")
                .generatePublic(java.security.spec.X509EncodedKeySpec(bytes))
        } catch (e: Exception) {
            // Fall back to Elliptic Curve P-256 if X25519 decoding fails
            java.security.KeyFactory.getInstance("EC")
                .generatePublic(java.security.spec.X509EncodedKeySpec(bytes))
        }
    }

    private fun performJavaECDH(
        privateKey: java.security.PrivateKey,
        publicKey: java.security.PublicKey
    ): ByteArray = try {
        // Perform X25519 Elliptic Curve Diffie-Hellman key agreement to produce a shared secret
        javax.crypto.KeyAgreement.getInstance("X25519").apply {
            init(privateKey); doPhase(publicKey, true)
        }.generateSecret()
    } catch (e: Exception) {
        // Fall back to ECDH Elliptic Curve Diffie-Hellman on P-256 if X25519 is unavailable
        javax.crypto.KeyAgreement.getInstance("ECDH").apply {
            init(privateKey); doPhase(publicKey, true)
        }.generateSecret()
    }


    // Advanced Encryption Standard — Galois/Counter Mode (AES-256-GCM)

    private fun generateIV() = ByteArray(GCM_IV_LENGTH).also { random.nextBytes(it) }

    private fun aesGCMEncrypt(plaintext: ByteArray, key: SecretKey, iv: ByteArray): ByteArray =
        // Encrypt using Advanced Encryption Standard 256-bit in Galois/Counter Mode with 128-bit authentication tag
        Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            doFinal(plaintext)
        }

    private fun aesGCMDecrypt(ciphertext: ByteArray, key: SecretKey, iv: ByteArray): ByteArray =
        // Decrypt and verify the 128-bit Galois/Counter Mode authentication tag
        Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            doFinal(ciphertext)
        }

}