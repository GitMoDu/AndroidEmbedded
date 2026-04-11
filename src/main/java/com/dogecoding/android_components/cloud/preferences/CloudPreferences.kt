package com.dogecoding.android_components.cloud.preferences

import android.content.Context
import androidx.core.content.edit
import com.dogecoding.android_core.shared_preferences.TinkSharedPreferences

class CloudPreferences(context: Context) : TinkSharedPreferences(context, PREFS_NAME) {

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
        edit().putString(KEY_CLOUD_PROVIDER, provider).apply()
    }

    fun getCloudProvider(): String {
        return getString(KEY_CLOUD_PROVIDER, PROVIDER_NONE) ?: PROVIDER_NONE
    }

    fun saveCloudUsername(username: String?) {
        edit().putString(KEY_CLOUD_USERNAME, username).apply()
    }

    fun getCloudUsername(): String? {
        return getString(KEY_CLOUD_USERNAME, null)
    }

    fun saveCloudToken(token: String?) {
        edit().putString(KEY_CLOUD_TOKEN, token).apply()
    }

    fun getCloudToken(): String? {
        return getString(KEY_CLOUD_TOKEN, null)
    }

    fun saveCloudPath(path: String?) {
        edit().putString(KEY_CLOUD_PATH, path).apply()
    }

    fun getCloudPath(): String? {
        return getString(KEY_CLOUD_PATH, null)
    }

    fun clearCloudSession() {
        edit().apply {
            remove(KEY_CLOUD_PROVIDER)
            remove(KEY_CLOUD_USERNAME)
            remove(KEY_CLOUD_TOKEN)
            remove(KEY_CLOUD_PATH)
        }.apply()
    }
}
