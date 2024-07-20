package com.dogecoding.embedded.uart_interface.usb_serial

import android.os.SystemClock

class TimestampSource {

    companion object {
        fun getMillis(): Long {
            return SystemClock.elapsedRealtime()
        }
    }
}