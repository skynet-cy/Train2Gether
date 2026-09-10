package com.example.train2gether.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object DatabaseKeyManager {

    private const val KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "train2gether_db_master_key"
    private const val PREFS = "database_key_prefs"
    private const val PREF_KEY = "encrypted_db_key"
    private const val PREF_IV = "db_key_iv"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    @Synchronized
    fun getOrCreateDatabaseKey(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val encryptedKey = prefs.getString(PREF_KEY, null)
        val savedIv = prefs.getString(PREF_IV, null)
        val masterKey = getOrCreateMasterKey()

        if (encryptedKey != null && savedIv != null) {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val iv = Base64.decode(savedIv, Base64.NO_WRAP)

            cipher.init(
                Cipher.DECRYPT_MODE,
                masterKey,
                GCMParameterSpec(128, iv)
            )

            return cipher.doFinal(
                Base64.decode(encryptedKey, Base64.NO_WRAP)
            )
        }

        check(encryptedKey == null && savedIv == null) {
            "Dati della chiave database incompleti"
        }

        val databaseKey = ByteArray(32)
        SecureRandom().nextBytes(databaseKey)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)

        val encrypted = cipher.doFinal(databaseKey)

        val salvato = prefs.edit()
            .putString(PREF_KEY, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(PREF_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .commit()

        check(salvato) {
            "Impossibile salvare la chiave cifrata del database"
        }

        return databaseKey
    }

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }

        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let {
            return it
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}