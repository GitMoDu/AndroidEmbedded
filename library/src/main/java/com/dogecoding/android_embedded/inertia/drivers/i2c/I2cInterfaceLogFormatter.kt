package com.dogecoding.android_embedded.inertia.drivers.i2c

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.TagLogFormatter
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord

class I2cInterfaceLogFormatter : TagLogFormatter(1033721212) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[I2C] ")

        val message = when (log.code) {
            0 -> "ErrorTimeout"
            1 -> "RecoveryAttempt"
            else -> "Unknown Code(${log.code})"
        }

        builder.append(message)
        return builder
    }
}