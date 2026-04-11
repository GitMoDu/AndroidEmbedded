package com.dogecoding.android_embedded.serial.interfaces.ble_serial.preferences

import android.content.Context
import androidx.core.content.edit
import com.dogecoding.android_core.shared_preferences.SecurePreferences

class BleSerialPreferences(context: Context) : SecurePreferences(context, PREF_FILE_NAME) {

    companion object {
        private const val PREF_FILE_NAME = "secure_prefs"
        private const val KEY_LAST_DEVICE_ADDRESS = "last_device_address"
    }

    fun saveLastDeviceAddress(address: String?) {
        sharedPreferences.edit { putString(KEY_LAST_DEVICE_ADDRESS, address) }
    }

    fun getLastDeviceAddress(): String? {
        return sharedPreferences.getString(KEY_LAST_DEVICE_ADDRESS, null)
    }

    fun clearDevice() {
        sharedPreferences.edit {
            remove(KEY_LAST_DEVICE_ADDRESS)
        }
    }
}
