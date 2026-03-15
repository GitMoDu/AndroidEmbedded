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
import android.os.ParcelUuid
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dogecoding.android_embedded.serial.ble_serial.manager.BleSerialManager

@SuppressLint("MissingPermission")
class BleSerialViewModel(application: Application) : AndroidViewModel(application) {

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

    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD: Long = 10000

    private val stopScanRunnable = Runnable { stopScan() }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
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
            _isScanning.postValue(false)
            handler.removeCallbacks(stopScanRunnable)
        }
    }

    fun startScan() {
        if (_isScanning.value == true) return

        _discoveredDevices.value = emptyList()
        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(BleSerialManager.NUS_SERVICE_UUID))
                .build()
        )

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

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

    fun connect(device: BluetoothDevice): BleSerial {
        _connectedDevice.value = device
        return BleSerial(device)
    }

    fun disconnect() {
        stopScan()
        _connectedDevice.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }
}
