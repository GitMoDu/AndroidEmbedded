package com.dogecoding.android_embedded.common

import android.os.SystemClock

class TimestampSource {

    companion object {
        fun getMillis(): Long {
            return SystemClock.elapsedRealtime()
        }

        fun getMicros() : Long {
            return SystemClock.elapsedRealtimeNanos() / 1000
        }
    }
}