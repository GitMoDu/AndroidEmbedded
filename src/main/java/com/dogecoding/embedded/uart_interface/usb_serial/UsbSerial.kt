package com.dogecoding.embedded.uart_interface.usb_serial

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Process
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
        const val WRITE_WAIT_MILLIS: Int = 20

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
                    device = device, port = driver.ports.first().portNumber, driver = driver
                )
            }
        }

        return null
    }

    private fun getUsbManager(context: Context): UsbManager {
        return context.getSystemService(Context.USB_SERVICE) as UsbManager
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
            Log.d(TAG, "connection failed: no device")

            connecting = false
            return
        }

        val driver = serialDevice!!.driver
        usbSerialPort = driver.ports[serialDevice!!.port]
        val usbConnection = usbManager.openDevice(driver.device)
        if (usbConnection == null && serialDevice!!.permissionsGranted == null && !usbManager.hasPermission(
                driver.device
            )
        ) {
            serialDevice!!.permissionsGranted = false
            val flags = PendingIntent.FLAG_MUTABLE
            val intent: Intent = Intent(INTENT_ACTION_GRANT_USB(activity.application.packageName))
            intent.setPackage(context.packageName)
            val usbPermissionIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
            usbManager.requestPermission(driver.device, usbPermissionIntent)

            connecting = false

            return
        }

        if (usbConnection == null) {
            if (!usbManager.hasPermission(driver.device)) {
                Log.d(TAG, "connection failed: permission denied")
            } else {
                Log.d(TAG, "connection failed: open failed")
            }
            connecting = false

            return
        }

        try {
            usbSerialPort!!.open(usbConnection)

            try {
                usbSerialPort!!.setParameters(baudRate, 8, 1, UsbSerialPort.PARITY_NONE)
                usbSerialPort!!.rts = false
                usbSerialPort!!.dtr = true
                usbSerialPort!!.flowControl = UsbSerialPort.FlowControl.NONE

            } catch (e: UnsupportedOperationException) {
                Log.d(TAG, "unsupport setparameters")
            }
            usbIoManager = SerialInputOutputManager(usbSerialPort, this)
            usbIoManager!!.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            usbIoManager!!.readTimeout = 10
            usbIoManager!!.writeTimeout = 10
            usbIoManager!!.start()


            Log.d(TAG, "connected")
            synchronized(synchronizationToken) {
                connected = true
            }
            usbSerialListener.onConnected()

        } catch (e: Exception) {
            Log.d(TAG, "connection failed: " + e.message)
            disconnect()
        }
    }

    fun disconnect() {
        val previousState: Boolean
        synchronized(synchronizationToken) {
            previousState = connected or connecting
        }
        if (previousState) {
            Log.d(TAG, "disconnected")

            synchronized(synchronizationToken) {
                connected = false
                connecting = false
            }
            try {
                if (usbIoManager != null) {
                    usbIoManager?.listener = null
                    usbIoManager?.stop()
                }
                usbIoManager = null

                usbSerialPort?.dtr = false
                usbSerialPort?.controlLines?.clear()
                usbSerialPort?.close()
            } catch (ex: Exception) {
                Log.e(TAG, "Error closing USB serial ${ex.message}")
            }
            usbSerialPort = null

            usbSerialListener?.onDisconnected()
        }
    }

    override fun onNewData(data: ByteArray?) {
        if (connected && data != null) {
            usbSerialListener?.onNewData(data)
        }
    }

    override fun onRunError(e: Exception?) {
        if (connected && e != null) {
            usbSerialListener?.onRunError(e)
        }

        Log.d(TAG, "connection error: " + e?.message)
        disconnect()
    }

    fun serialWrite(value: UByte) {
        if (usbSerialPort?.isOpen == true) {
            try {
                singleArray[0] = value.toByte()
                usbSerialPort!!.write(
                    singleArray, 1, WRITE_WAIT_MILLIS
                )
            } catch (e: java.lang.Exception) {
                onRunError(e)
            }
        } else {
            onRunError(Exception("Port is closed"))
        }
    }

    fun serialWrite(data: UByteArray, size: Int) {
        if (usbSerialPort?.isOpen == true) {
            val copy = ByteArray(size)
            for (i in 0 until size) {
                copy[i] = data[i].toByte()
            }

            try {
                usbSerialPort!!.write(
                    copy, size, WRITE_WAIT_MILLIS
                )
            } catch (e: java.lang.Exception) {
                onRunError(e)
            }
        } else {
            onRunError(Exception("Port is closed"))
        }
    }
}