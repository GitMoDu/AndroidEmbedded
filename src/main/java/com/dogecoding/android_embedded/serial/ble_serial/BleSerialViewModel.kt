package com.dogecoding.android_embedded.serial.ble_serial

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dogecoding.android_embedded.serial.ble_serial.manager.AbstractBleSerialManager
import com.dogecoding.android_embedded.serial.ble_serial.manager.NusBleSerialManager
import com.dogecoding.android_embedded.serial.ble_serial.manager.Vollgo6328SerialManager

@SuppressLint("MissingPermission")
class BleSerialViewModel(application: Application) : AndroidViewModel(application) {

    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager =
            application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val _discoveredDevices = MutableLiveData<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: LiveData<List<BluetoothDevice>> = _discoveredDevices

    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning

    private val _connectedDevice = MutableLiveData<BluetoothDevice?>(null)
    val connectedDevice: LiveData<BluetoothDevice?> = _connectedDevice

    private val _connectionState = MutableLiveData(ConnectionState.DISCONNECTED)
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val _rssi = MutableLiveData<Int?>(null)
    val rssi: LiveData<Int?> = _rssi

    private var bleSerial: BleSerial<*>? = null
    val currentBleSerial: BleSerial<*>? get() = bleSerial

    private val handler = Handler(Looper.getMainLooper())

    private val stopScanRunnable = Runnable { stopScan() }

    private val rssiRunnable = object : Runnable {
        override fun run() {
            bleSerial?.requestRssi()
            handler.postDelayed(this, RSSI_UPDATE_INTERVAL)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            Log.d("BleScan", "Found device: ${device.name} [${device.address}]")

            val currentList = _discoveredDevices.value ?: emptyList()
            if (device.name != null && currentList.none { it.address == device.address }) {
                _discoveredDevices.postValue(currentList + device)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            val currentList = _discoveredDevices.value ?: emptyList()
            val newDevices = results.map { it.device }
                .filter { it.name != null && currentList.none { existing -> existing.address == it.address } }
            if (newDevices.isNotEmpty()) {
                _discoveredDevices.postValue(currentList + newDevices)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BleScan", "Scan failed with error: $errorCode")
            _isScanning.postValue(false)
            handler.removeCallbacks(stopScanRunnable)
        }
    }

    fun startScan(filters: List<ScanFilter> = listOf()) {
        if (_isScanning.value == true) return

        _discoveredDevices.value = emptyList()
        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return

        val settings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        _isScanning.value = true
        scanner.startScan(filters, settings, scanCallback)

        handler.postDelayed(stopScanRunnable, SCAN_PERIOD)
    }

    fun stopScan() {
        if (_isScanning.value == false) return

        handler.removeCallbacks(stopScanRunnable)
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        scanner?.stopScan(scanCallback)
        _isScanning.postValue(false)
    }

    private fun updateState() {
        val isConnected = bleSerial?.isConnected() ?: false
        val isConnecting = bleSerial?.isConnecting() ?: false

        val newState = when {
            isConnected -> ConnectionState.CONNECTED
            isConnecting -> ConnectionState.CONNECTING
            else -> ConnectionState.DISCONNECTED
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            _connectionState.value = newState
        } else {
            _connectionState.postValue(newState)
        }

        if (isConnected) {
            startRssiUpdates()
        } else {
            stopRssiUpdates()
            _rssi.postValue(null)
        }
    }

    private fun startRssiUpdates() {
        handler.removeCallbacks(rssiRunnable)
        handler.post(rssiRunnable)
    }

    private fun stopRssiUpdates() {
        handler.removeCallbacks(rssiRunnable)
    }

    fun connect(device: BluetoothDevice): BleSerial<*> {
        val manager = createManager(device)
        return connect(device, manager)
    }

    fun <T : AbstractBleSerialManager> connect(
        device: BluetoothDevice,
        manager: T
    ): BleSerial<T> {
        // Clean up any existing connection
        bleSerial?.disconnect()

        val newBleSerial = BleSerial(
            device = device,
            manager = manager,
            onStateChanged = { _, _ -> updateState() },
            onRssiChanged = { rssi -> _rssi.postValue(rssi) },
            onDisconnectedCallback = {
                if (_connectedDevice.value?.address == device.address) {
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        _connectedDevice.value = null
                    } else {
                        _connectedDevice.postValue(null)
                    }
                    bleSerial = null
                    updateState()
                }
            }
        )

        bleSerial = newBleSerial
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _connectedDevice.value = device
        } else {
            _connectedDevice.postValue(device)
        }
        updateState()

        return newBleSerial
    }

    private fun createManager(device: BluetoothDevice): AbstractBleSerialManager {
        val uuids = device.uuids
        val name = device.name

        return if (uuids?.any { it.uuid == NusBleSerialManager.NUS_SERVICE_UUID } == true ||
            name?.contains("UART", ignoreCase = true) == true ||
            name?.contains("Nordic", ignoreCase = true) == true) {
            NusBleSerialManager(getApplication())
        } else {
            Vollgo6328SerialManager(getApplication())
        }
    }

    fun disconnect() {
        stopScan()
        bleSerial?.disconnect()
        bleSerial = null
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _connectedDevice.value = null
        } else {
            _connectedDevice.postValue(null)
        }
        updateState()
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    companion object {
        private val SCAN_PERIOD: Long = 10000
        private val RSSI_UPDATE_INTERVAL: Long = 2000
    }
}
