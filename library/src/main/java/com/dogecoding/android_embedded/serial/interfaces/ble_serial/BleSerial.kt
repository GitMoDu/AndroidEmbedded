package com.dogecoding.android_embedded.serial.interfaces.ble_serial

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.dogecoding.android_embedded.serial.model.SerialInterface
import com.dogecoding.android_embedded.serial.model.SerialListener
import com.dogecoding.android_embedded.serial.interfaces.ble_serial.manager.AbstractBleSerialManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import java.util.Collections

@OptIn(ExperimentalUnsignedTypes::class)
class BleSerial<T : AbstractBleSerialManager>(
    private val device: BluetoothDevice,
    val manager: T,
    private val onStateChanged: ((isConnected: Boolean, isConnecting: Boolean) -> Unit)? = null,
    private val onRssiChanged: ((Int) -> Unit)? = null,
    private val onDisconnectedCallback: (() -> Unit)? = null
) : SerialInterface {

    companion object {
        private val TAG = BleSerial::class.java.simpleName
    }

    private val listeners = Collections.synchronizedSet(mutableSetOf<SerialListener>())
    private var connectionScope: CoroutineScope? = null

    override fun isConnected(): Boolean {
        return manager.isReady
    }

    override fun isConnecting(): Boolean {
        val s = manager.getConnectionState()
        return !manager.isReady && (s == BluetoothProfile.STATE_CONNECTING || s == BluetoothProfile.STATE_CONNECTED)
    }

    override fun connect(context: Context, serialListener: SerialListener) {
        listeners.add(serialListener)
        
        // Ensure the new listener gets an immediate 'connected' callback if we are already ready
        if (isConnected()) {
            serialListener.onConnected()
        }

        if (isConnected() || isConnecting()) {
            Log.d(TAG, "Connect ignored: already connected or connecting")
            // Ensure state is synced even if connection is already established
            onStateChanged?.invoke(isConnected(), isConnecting())
            return
        }
        
        manager.dataReceivedCallback = { data ->
            synchronized(listeners) {
                listeners.forEach { it.onNewData(data) }
            }
        }
        manager.rssiCallback = { rssi ->
            onRssiChanged?.invoke(rssi)
        }

        connectionScope?.cancel()
        connectionScope = CoroutineScope(Dispatchers.Main + Job())
        
        Log.d(TAG, "Connecting to ${device.address}")
        manager.connect(device)
            .retry(3, 500)
            .enqueue()

        // Notify immediate state change (will likely be 'Connecting')
        onStateChanged?.invoke(isConnected(), isConnecting())

        // Monitor state
        manager.stateAsFlow().onEach { state ->
            onStateChanged?.invoke(isConnected(), isConnecting())
            
            when (state) {
                is ConnectionState.Ready -> {
                    Log.d(TAG, "BLE Connected and Ready")
                    synchronized(listeners) {
                        listeners.forEach { it.onConnected() }
                    }
                }
                is ConnectionState.Disconnected -> {
                    Log.d(TAG, "BLE Disconnected")
                    synchronized(listeners) {
                        listeners.forEach { it.onDisconnected() }
                    }
                    onDisconnectedCallback?.invoke()
                    connectionScope?.cancel()
                }
                else -> {}
            }
        }.launchIn(connectionScope!!)
    }

    override fun removeListener(serialListener: SerialListener) {
        listeners.remove(serialListener)
    }

    override fun disconnect() {
        Log.d(TAG, "Disconnecting from ${device.address}")
        manager.let {
            it.disconnect().enqueue()
            it.close()
        }
        connectionScope?.cancel()
        onStateChanged?.invoke(false, false)
        
        // We notify and clear listeners on explicit disconnect
        synchronized(listeners) {
            listeners.forEach { it.onDisconnected() }
            listeners.clear()
        }
    }

    override fun serialWrite(value: UByte) {
        manager.send(byteArrayOf(value.toByte()))
    }

    override fun serialWrite(data: UByteArray, size: Int) {
        val bytes = ByteArray(size)
        for (i in 0 until size) {
            bytes[i] = data[i].toByte()
        }
        manager.send(bytes)
    }

    fun requestRssi() {
        manager.requestRssi()
    }
}
