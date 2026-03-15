package com.dogecoding.android_embedded.serial.ble_serial

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.util.Log
import com.dogecoding.android_embedded.serial.ble_serial.manager.BleSerialManager
import com.dogecoding.android_embedded.serial.SerialInterface
import com.dogecoding.android_embedded.serial.SerialListener
import no.nordicsemi.android.ble.ktx.stateAsFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.ktx.state.ConnectionState

@OptIn(ExperimentalUnsignedTypes::class)
class BleSerial(private val device: BluetoothDevice) : SerialInterface {

    companion object {
        private val TAG = BleSerial::class.java.simpleName
    }

    private var manager: BleSerialManager? = null
    private var serialListener: SerialListener? = null
    private var connectionScope: CoroutineScope? = null

    override fun isConnected(): Boolean {
        return manager?.isConnected == true
    }

    override fun isConnecting(): Boolean {
        return manager?.isReady == false && manager?.isConnected == true // Simplified
    }

    override fun connect(activity: Activity, serialListener: SerialListener) {
        this.serialListener = serialListener
        val context = activity.applicationContext
        manager = BleSerialManager(context)
        
        manager?.dataReceivedCallback = { data ->
            serialListener.onNewData(data)
        }

        connectionScope = CoroutineScope(Dispatchers.Main + Job())
        
        manager?.connect(device)
            ?.useAutoConnect(true)
            ?.retry(3, 100)
            ?.enqueue()

        // Monitor state
        manager?.stateAsFlow()?.onEach { state ->
            when (state) {
                is ConnectionState.Ready -> {
                    Log.d(TAG, "BLE Connected and Ready")
                    serialListener.onConnected()
                }
                is ConnectionState.Disconnected -> {
                    Log.d(TAG, "BLE Disconnected")
                    serialListener.onDisconnected()
                    connectionScope?.cancel()
                }
                else -> {}
            }
        }?.launchIn(connectionScope!!)
    }

    override fun disconnect() {
        manager?.disconnect()?.enqueue()
        connectionScope?.cancel()
        manager = null
    }

    override fun serialWrite(value: UByte) {
        manager?.send(byteArrayOf(value.toByte()))
    }

    override fun serialWrite(data: UByteArray, size: Int) {
        val bytes = ByteArray(size)
        for (i in 0 until size) {
            bytes[i] = data[i].toByte()
        }
        manager?.send(bytes)
    }
}