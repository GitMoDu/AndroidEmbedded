package com.dogecoding.android_core.shared_preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager

/**
 * A secure SharedPreferences implementation using Google Tink.
 * Replaces the deprecated EncryptedSharedPreferences.
 */
open class TinkSharedPreferences(
    context: Context,
    fileName: String
) : SharedPreferences {

    companion object {
        private const val TAG = "TinkSharedPreferences"
        private const val KEYSET_NAME = "master_keyset"
        private const val PREFERENCE_FILE = "tink_prefs_storage"
        private const val MASTER_KEY_URI = "android-keystore://tink_master_key"

        init {
            try {
                AeadConfig.register()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register Tink AeadConfig", e)
            }
        }
    }

    private val aead: Aead by lazy {
        AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREFERENCE_FILE)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    protected val delegate: SharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)

    override fun getAll(): Map<String, *> {
        val all = delegate.all
        val decryptedAll = mutableMapOf<String, Any?>()
        for ((key, value) in all) {
            if (value is String) {
                try {
                    val decoded = Base64.decode(value, Base64.DEFAULT)
                    val decrypted = aead.decrypt(decoded, key.toByteArray())
                    decryptedAll[key] = String(decrypted, Charsets.UTF_8)
                } catch (e: Exception) {
                    decryptedAll[key] = value
                }
            } else {
                decryptedAll[key] = value
            }
        }
        return decryptedAll
    }

    override fun getString(key: String, defValue: String?): String? {
        val encrypted = delegate.getString(key, null) ?: return defValue
        return try {
            val decoded = Base64.decode(encrypted, Base64.DEFAULT)
            val decrypted = aead.decrypt(decoded, key.toByteArray())
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed for key: $key", e)
            defValue
        }
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        val encryptedSet = delegate.getStringSet(key, null) ?: return defValues
        return encryptedSet.mapNotNull { encrypted ->
            try {
                val decoded = Base64.decode(encrypted, Base64.DEFAULT)
                val decrypted = aead.decrypt(decoded, key.toByteArray())
                String(decrypted, Charsets.UTF_8)
            } catch (e: Exception) {
                null
            }
        }.toSet()
    }

    override fun getInt(key: String, defValue: Int): Int =
        getString(key, null)?.toIntOrNull() ?: defValue

    override fun getLong(key: String, defValue: Long): Long =
        getString(key, null)?.toLongOrNull() ?: defValue

    override fun getFloat(key: String, defValue: Float): Float =
        getString(key, null)?.toFloatOrNull() ?: defValue

    override fun getBoolean(key: String, defValue: Boolean): Boolean =
        getString(key, null)?.toBoolean() ?: defValue

    override fun contains(key: String): Boolean = delegate.contains(key)

    override fun edit(): SharedPreferences.Editor = TinkEditor(delegate.edit(), aead)

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        delegate.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        delegate.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private class TinkEditor(
        private val delegate: SharedPreferences.Editor,
        private val aead: Aead
    ) : SharedPreferences.Editor {

        private fun encrypt(key: String, value: String): String {
            val encrypted = aead.encrypt(value.toByteArray(Charsets.UTF_8), key.toByteArray())
            return Base64.encodeToString(encrypted, Base64.DEFAULT)
        }

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            if (value == null) delegate.remove(key)
            else delegate.putString(key, encrypt(key, value))
            return this
        }

        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
            if (values == null) delegate.remove(key)
            else delegate.putStringSet(key, values.map { encrypt(key, it) }.toSet())
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor =
            putString(key, value.toString())

        override fun putLong(key: String, value: Long): SharedPreferences.Editor =
            putString(key, value.toString())

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor =
            putString(key, value.toString())

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor =
            putString(key, value.toString())

        override fun remove(key: String): SharedPreferences.Editor {
            delegate.remove(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            delegate.clear()
            return this
        }

        override fun commit(): Boolean = delegate.commit()
        override fun apply() = delegate.apply()
    }
}
