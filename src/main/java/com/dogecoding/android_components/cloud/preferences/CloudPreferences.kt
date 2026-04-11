package com.dogecoding.android_components.cloud.preferences

import android.content.Context
import androidx.core.content.edit
import com.dogecoding.android_core.shared_preferences.SecurePreferences

class CloudPreferences(context: Context) : SecurePreferences(context, PREFS_NAME) {

    companion object {
        private const val PREFS_NAME = "cloud_preferences"
        private const val KEY_CLOUD_PROVIDER = "cloud_provider"
        private const val KEY_CLOUD_USERNAME = "cloud_username"
        private const val KEY_CLOUD_TOKEN = "cloud_token"
        private const val KEY_CLOUD_PATH = "cloud_path"

        const val PROVIDER_NONE = "none"
        const val PROVIDER_GOOGLE = "google"
        const val PROVIDER_ONEDRIVE = "onedrive"
    }

    fun saveCloudProvider(provider: String) {
        sharedPreferences.edit { putString(KEY_CLOUD_PROVIDER, provider) }
    }

    fun getCloudProvider(): String {
        return sharedPreferences.getString(KEY_CLOUD_PROVIDER, PROVIDER_NONE) ?: PROVIDER_NONE
    }

    fun saveCloudUsername(username: String?) {
        sharedPreferences.edit { putString(KEY_CLOUD_USERNAME, username) }
    }

    fun getCloudUsername(): String? {
        return sharedPreferences.getString(KEY_CLOUD_USERNAME, null)
    }

    fun saveCloudToken(token: String?) {
        sharedPreferences.edit { putString(KEY_CLOUD_TOKEN, token) }
    }

    fun getCloudToken(): String? {
        return sharedPreferences.getString(KEY_CLOUD_TOKEN, null)
    }

    fun saveCloudPath(path: String?) {
        sharedPreferences.edit { putString(KEY_CLOUD_PATH, path) }
    }

    fun getCloudPath(): String? {
        return sharedPreferences.getString(KEY_CLOUD_PATH, null)
    }

    fun clearCloudSession() {
        sharedPreferences.edit {
            remove(KEY_CLOUD_PROVIDER)
            remove(KEY_CLOUD_USERNAME)
            remove(KEY_CLOUD_TOKEN)
            remove(KEY_CLOUD_PATH)
        }
    }
}
