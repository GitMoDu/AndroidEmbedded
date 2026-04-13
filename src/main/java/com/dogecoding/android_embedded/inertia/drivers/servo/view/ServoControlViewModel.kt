package com.dogecoding.android_embedded.inertia.drivers.servo.view

import androidx.lifecycle.ViewModel
import com.dogecoding.android_embedded.inertia.drivers.servo.uart.ServoUartRequest
import com.dogecoding.android_embedded.serial.model.SerialInterface

@OptIn(ExperimentalUnsignedTypes::class)
class ServoControlViewModel : ViewModel() {

    private var serialInterface: SerialInterface? = null
    private var servoIndex: Byte = 0

    fun setSerialInterface(serial: SerialInterface?, index: Int) {
        this.serialInterface = serial
        this.servoIndex = index.toByte()
    }

    fun updateServo(enabled: Boolean, pulseUs: Int) {
        val serial = serialInterface ?: return
        if (!serial.isConnected()) return

        val request = ServoUartRequest(
            index = servoIndex,
            enabled = enabled,
            pulseNs = pulseUs * 1000
        )

        val data = request.toByteArray()
        serial.serialWrite(data.toUByteArray(), data.size)
    }
}
