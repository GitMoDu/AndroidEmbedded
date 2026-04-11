package com.dogecoding.android_embedded.serial.interfaces.usb_serial

import android.hardware.usb.UsbDevice
import com.hoho.android.usbserial.driver.UsbSerialDriver

class SerialDevice(
    val device: UsbDevice,
    val port: Int,
    val driver: UsbSerialDriver,
    var permissionsGranted: Boolean? = null
)