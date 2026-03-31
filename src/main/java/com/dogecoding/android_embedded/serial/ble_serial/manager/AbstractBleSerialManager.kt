package com.dogecoding.android_embedded.serial.ble_serial.manager

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.content.Context
import android.os.ParcelUuid
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ConnectionPriorityRequest
import java.util.UUID

abstract class AbstractBleSerialManager(context: Context) : BleManager(context) {

    protected var txCharacteristic: BluetoothGattCharacteristic? = null
    protected val rxCharacteristics = mutableListOf<BluetoothGattCharacteristic>()

    var dataReceivedCallback: ((ByteArray) -> Unit)? = null
    var rssiCallback: ((Int) -> Unit)? = null

    abstract val serviceUuid: UUID

    open fun getScanFilter(): ScanFilter {
        return ScanFilter.Builder().setServiceUuid(ParcelUuid(serviceUuid)).build()
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        rxCharacteristics.clear()
        txCharacteristic = null

        val service = gatt.getService(serviceUuid)
        service?.characteristics?.forEach { char ->
            val props = char.properties

            // Dynamic RX discovery: capture any characteristic that supports Notify or Indicate
            if (props and (BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                rxCharacteristics.add(char)
            }

            // Dynamic TX discovery: identify a characteristic for writing
            if (props and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                if (txCharacteristic == null || isPreferredTxCharacteristic(char)) {
                    txCharacteristic = char
                }
            }
        }

        return txCharacteristic != null || rxCharacteristics.isNotEmpty()
    }

    protected open fun isPreferredTxCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean = false

    override fun initialize() {
        requestConnectionPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH).enqueue()

        rxCharacteristics.forEach { char ->
            setNotificationCallback(char).with { _, data ->
                data.value?.let { dataReceivedCallback?.invoke(it) }
            }

            if (char.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                enableNotifications(char).enqueue()
            } else {
                enableIndications(char).enqueue()
            }
        }
    }

    override fun onServicesInvalidated() {
        txCharacteristic = null
        rxCharacteristics.clear()
    }

    fun send(data: ByteArray) {
        txCharacteristic?.let { char ->
            val writeType = if (char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            } else {
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            }
            writeCharacteristic(char, data, writeType).enqueue()
        }
    }

    fun requestRssi() {
        readRssi().with { _, rssi ->
            rssiCallback?.invoke(rssi)
        }.enqueue()
    }
}
