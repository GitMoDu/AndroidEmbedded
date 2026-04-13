package com.dogecoding.android_embedded.serial.interfaces.ble_serial.manager.managers

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.content.Context
import android.os.ParcelUuid
import com.dogecoding.android_embedded.serial.interfaces.ble_serial.manager.AbstractBleSerialManager
import java.util.UUID

/**
 * Serial manager for the VG 6328A dual-mode Bluetooth transparent transmission module.
 *
 * Manufacturer: Shenzhen Vollgo Technology Co., Ltd. (Shenzhen Vollgo Electronics Co., Ltd.)
 * Specification Version: V1.0
 *
 * This module supports dual-mode Bluetooth (Classic + BLE). This manager implementation
 * handles BLE connectivity for transparent data transmission, utilizing the common
 * 0xFFE0 service and 0xFFE1 characteristic (shared with HM-10 and similar modules).
 */
class Vollgo6328SerialManager(context: Context, private val deviceName: String? = null) :
    AbstractBleSerialManager(context) {
    companion object {
        /**
         * Standard Transparent Transmission Service UUID (0xFFE0).
         * Used by VG 6328A for BLE communication.
         */
        val SERIAL_SERVICE_UUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")

        /**
         * Standard Data Characteristic UUID (0xFFE1).
         * Handles both read (Notify) and write operations for transparent data.
         */
        private val SERIAL_DATA_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

        fun getDeviceFilter(deviceName: String? = null): ScanFilter {
            if (deviceName != null) {
                return ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(SERIAL_SERVICE_UUID))
                    .setDeviceName(deviceName)
                    .build()
            } else {
                return ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(SERIAL_SERVICE_UUID))
                    .build()
            }
        }
    }

    override val serviceUuid: UUID = SERIAL_SERVICE_UUID

    override fun getScanFilter(): ScanFilter = getDeviceFilter(deviceName)

    override fun isPreferredTxCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean {
        return characteristic.uuid == SERIAL_DATA_CHARACTERISTIC_UUID
    }
}
