package com.dogecoding.android_embedded.serial.ble_serial.manager

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.content.Context
import android.os.ParcelUuid
import java.util.UUID

class NusBleSerialManager(context: Context) : AbstractBleSerialManager(context) {
    companion object {
        // Nordic UART Service (NUS)
        val NUS_SERVICE_UUID: UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        // NUS TX Characteristic (Write)
        private val NUS_TX_CHARACTERISTIC_UUID: UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")

        val DEFAULT_FILTER: ScanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(NUS_SERVICE_UUID))
            .build()
    }

    override val serviceUuid: UUID = NUS_SERVICE_UUID

    override fun getScanFilter(): ScanFilter = DEFAULT_FILTER

    override fun isPreferredTxCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean {
        return characteristic.uuid == NUS_TX_CHARACTERISTIC_UUID
    }
}
