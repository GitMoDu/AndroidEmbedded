package com.dogecoding.embedded.uart_interface.usb_serial

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.io.IOException

@OptIn(ExperimentalUnsignedTypes::class)
class UsbSerial(private val baudRate: Int) : SerialInputOutputManager.Listener {

    companion object {
        val TAG: String = UsbSerial::class.java.name
        const val WRITE_WAIT_MILLIS: Int = 1000

        private fun INTENT_ACTION_GRANT_USB(appId: String): String {
            return appId + ".GRANT_USB"
        }
    }

    private var usbSerialListener: UsbSerialListener? = null

    private var usbIoManager: SerialInputOutputManager? = null
    private var usbSerialPort: UsbSerialPort? = null
    private val synchronizationToken = Any()

    private val singleArray = ByteArray(1)

    private var connected = false
    private var connecting = false
    private var serialDevice: SerialDevice? = null

    private var _usbManager: UsbManager? = null

    private fun getFirstDevice(context: Context): SerialDevice? {
        val usbManager = getUsbManager(context)
        val usbDefaultProber = UsbSerialProber.getDefaultProber()
        val usbCustomProber: UsbSerialProber = CustomProber.getCustomProber()

        for (device in usbManager.deviceList.values) {
            var driver: UsbSerialDriver? = usbDefaultProber.probeDevice(device)
            if (driver == null) {
                driver = usbCustomProber.probeDevice(device)
            }
            if (driver != null) {
                return SerialDevice(
                    device = device,
                    port = driver.ports.first().portNumber,
                    driver = driver
                )
            }
        }

        return null
    }

    private fun getUsbManager(context: Context): UsbManager {
        if (_usbManager == null) {
            _usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        }

        return _usbManager!!
    }

    private fun status(str: String) {
        Log.d(TAG, str)
    }

    fun isConnected(): Boolean {
        return connected
    }

    fun isConnecting(): Boolean {
        return connecting
    }

    fun connect(activity: Activity, usbSerialListener: UsbSerialListener) {
        if (!connecting) {
            connecting = true
        }

        this.usbSerialListener = usbSerialListener
        val context: Context = activity.applicationContext

        val usbManager = getUsbManager(context)
        serialDevice = getFirstDevice(context)

        if (serialDevice == null) {
            status("connection failed: no device")

            connecting = false
            return
        }

        val driver = serialDevice!!.driver

        usbSerialPort = driver.ports[serialDevice!!.port]
        val usbConnection = usbManager.openDevice(driver.device)
        if (usbConnection == null
            && serialDevice!!.permissionsGranted == null
            && !usbManager.hasPermission(driver.device)
        ) {
            serialDevice!!.permissionsGranted = false
            val flags =
                PendingIntent.FLAG_MUTABLE
            val intent: Intent =
                Intent(INTENT_ACTION_GRANT_USB(activity.application.packageName))
            intent.setPackage(context.packageName)
            val usbPermissionIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
            usbManager.requestPermission(driver.device, usbPermissionIntent)

            connecting = false

            return
        }

        if (usbConnection == null) {
            if (!usbManager.hasPermission(driver.device)) status("connection failed: permission denied")
            else status("connection failed: open failed")
            connecting = false

            return
        }

        try {
            usbSerialPort!!.open(usbConnection)

            try {
                usbSerialPort!!.setParameters(baudRate, 8, 1, UsbSerialPort.PARITY_NONE)
                usbSerialPort!!.rts = false
                usbSerialPort!!.dtr = true
            } catch (e: UnsupportedOperationException) {
                status("unsupport setparameters")
            }
            usbIoManager = SerialInputOutputManager(usbSerialPort, this)
            usbIoManager!!.start()

            status("connected")
            synchronized(synchronizationToken) {
                connected = true
            }
            usbSerialListener.onConnected()

        } catch (e: Exception) {
            status("connection failed: " + e.message)
            disconnect()
        }
    }

    fun disconnect() {
        val previousState: Boolean
        synchronized(synchronizationToken) {
            previousState = connected
        }
        if (previousState) {
            status("disconnected")

            synchronized(synchronizationToken) {
                connected = false
                connecting = false
            }
            if (usbIoManager != null) {
                usbIoManager?.listener = null
                usbIoManager?.stop()
            }
            usbIoManager = null
            try {
                usbSerialPort?.close()
            } catch (ignored: IOException) {
            }
            usbSerialPort = null

            usbSerialListener?.onDisconnected()
        }
    }

    override fun onNewData(data: ByteArray?) {
        if (connected
            && data != null
        ) {
            usbSerialListener?.onNewData(data)
        }
    }

    override fun onRunError(e: Exception?) {
        if (connected && e != null) {
            usbSerialListener?.onRunError(e)
        }

        status("connection error: " + e?.message)
        disconnect()
    }

    fun serialWrite(value: UByte, waitMillis: Int = WRITE_WAIT_MILLIS) {
        if (usbSerialPort?.isOpen == true) {
            try {
                singleArray[0] = value.toByte()
                usbSerialPort!!.write(
                    singleArray,
                    1,
                    WRITE_WAIT_MILLIS
                )
            } catch (e: java.lang.Exception) {
                onRunError(e)
            }
        } else {
            onRunError(Exception("Port is closed"))
        }
    }

    fun serialWrite(data: UByteArray, size: Int, waitMillis: Int = WRITE_WAIT_MILLIS) {
        if (usbSerialPort?.isOpen == true) {
            val copy = ByteArray(size)
            for (i in 0 until size) {
                copy[i] = data[i].toByte()
            }

            try {
                usbSerialPort!!.write(
                    copy,
                    size,
                    waitMillis
                )
            } catch (e: java.lang.Exception) {
                onRunError(e)
            }
        } else {
            onRunError(Exception("Port is closed"))
        }
    }
}