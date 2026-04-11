package com.dogecoding.android_embedded.inertia.drivers.uart.vg6328A

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.TagLogFormatter
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord

class Vg6328aLogFormatter : TagLogFormatter(505949888) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[VG6328A] ")

        val message = when (log.code) {
            0 -> "InvalidDeviceName"
            1 -> "WatchDogDetectedStuckState"
            else -> "Unknown Code(${log.code})"
        }

        builder.append(message)
        return builder
    }
}

