package com.dogecoding.android_core.shared_preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

abstract class SecurePreferences(context: Context, fileName: String) {

    protected val sharedPreferences: SharedPreferences = try {
        createEncryptedSharedPreferences(context, fileName)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create EncryptedSharedPreferences for $fileName, resetting", e)
        try {
            // Attempt to recover by deleting the corrupted preferences file
            context.deleteSharedPreferences(fileName)
            createEncryptedSharedPreferences(context, fileName)
        } catch (e2: Exception) {
            Log.e(
                TAG,
                "Still failing to create EncryptedSharedPreferences for $fileName, falling back to plain",
                e2
            )
            // Ultimate fallback to prevent crash: use unencrypted preferences
            context.getSharedPreferences("${fileName}_plain", Context.MODE_PRIVATE)
        }
    }

    private fun createEncryptedSharedPreferences(
        context: Context,
        fileName: String
    ): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            fileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val TAG = "SecurePreferences"
    }
}