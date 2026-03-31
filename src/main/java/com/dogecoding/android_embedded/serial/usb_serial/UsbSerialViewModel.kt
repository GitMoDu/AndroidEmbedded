package com.dogecoding.android_embedded.serial.usb_serial

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dogecoding.android_embedded.serial.SerialListener
import com.hoho.android.usbserial.driver.UsbSerialProber

class UsbSerialViewModel(application: Application) : AndroidViewModel(application) {

    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED
    }

    private val usbManager = application.getSystemService(Context.USB_SERVICE) as UsbManager

    private val _discoveredDevices = MutableLiveData<List<SerialDevice>>(emptyList())
    val discoveredDevices: LiveData<List<SerialDevice>> = _discoveredDevices

    private val _connectionState = MutableLiveData(ConnectionState.DISCONNECTED)
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val _connectedDevice = MutableLiveData<SerialDevice?>(null)
    val connectedDevice: LiveData<SerialDevice?> = _connectedDevice

    private var usbSerial: UsbSerial? = null
    val currentUsbSerial: UsbSerial? get() = usbSerial

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    discoverDevices()
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }
                    if (device != null && _connectedDevice.value?.device?.deviceName == device.deviceName) {
                        disconnect()
                    }
                    discoverDevices()
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            application.registerReceiver(usbReceiver, filter)
        }
        discoverDevices()
    }

    fun discoverDevices() {
        val usbDefaultProber = UsbSerialProber.getDefaultProber()
        val usbCustomProber = CustomProber.getCustomProber()
        val devices = mutableListOf<SerialDevice>()

        val deviceList = usbManager.deviceList
        for (device in deviceList.values) {
            var driver = usbDefaultProber.probeDevice(device)
            if (driver == null) {
                driver = usbCustomProber.probeDevice(device)
            }
            if (driver != null) {
                for (port in driver.ports.indices) {
                    devices.add(SerialDevice(device, port, driver))
                }
            }
        }
        _discoveredDevices.postValue(devices)
    }

    fun connect(activity: Activity, serialDevice: SerialDevice, baudRate: Int = 115200) {
        if (_connectionState.value != ConnectionState.DISCONNECTED) {
            disconnect()
        }

        _connectionState.value = ConnectionState.CONNECTING
        _connectedDevice.value = serialDevice

        val newUsbSerial = UsbSerial(baudRate)
        usbSerial = newUsbSerial

        newUsbSerial.connect(activity, object : SerialListener {
            override fun onConnected() {
                _connectionState.postValue(ConnectionState.CONNECTED)
            }

            override fun onDisconnected() {
                _connectionState.postValue(ConnectionState.DISCONNECTED)
                _connectedDevice.postValue(null)
                usbSerial = null
            }

            override fun onNewData(data: ByteArray) {
                // Handled by consumers of currentUsbSerial
            }

            override fun onRunError(e: Exception) {
                Log.e("UsbSerialViewModel", "Run error: ${e.message}")
                disconnect()
            }
        })
    }

    fun disconnect() {
        usbSerial?.disconnect()
        usbSerial = null
        _connectionState.postValue(ConnectionState.DISCONNECTED)
        _connectedDevice.postValue(null)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(usbReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
        disconnect()
    }
}