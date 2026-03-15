package com.dogecoding.android_embedded.serial.ble_serial.manager

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import java.util.*

class BleSerialManager(context: Context) : BleManager(context) {
    companion object {
        private val TAG = BleSerialManager::class.java.simpleName
        val NUS_SERVICE_UUID: UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        private val NUS_RX_CHARACTERISTIC_UUID: UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
        private val NUS_TX_CHARACTERISTIC_UUID: UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
    }

    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null

    var dataReceivedCallback: ((ByteArray) -> Unit)? = null

    override fun getGattCallback(): BleManagerGattCallback = object : BleManagerGattCallback() {
        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(NUS_SERVICE_UUID)
            if (service != null) {
                rxCharacteristic = service.getCharacteristic(NUS_RX_CHARACTERISTIC_UUID)
                txCharacteristic = service.getCharacteristic(NUS_TX_CHARACTERISTIC_UUID)
            }
            return rxCharacteristic != null && txCharacteristic != null
        }

        override fun initialize() {
            setNotificationCallback(txCharacteristic).with { _, data ->
                data.value?.let { dataReceivedCallback?.invoke(it) }
            }
            enableNotifications(txCharacteristic).enqueue()
        }

        override fun onServicesInvalidated() {
            rxCharacteristic = null
            txCharacteristic = null
        }
    }

    fun send(data: ByteArray) {
        if (rxCharacteristic != null) {
            writeCharacteristic(
                rxCharacteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            ).enqueue()
        }
    }

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
    }
}