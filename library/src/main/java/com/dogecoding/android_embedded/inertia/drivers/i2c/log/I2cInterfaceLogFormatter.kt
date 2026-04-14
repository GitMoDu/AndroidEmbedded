package com.dogecoding.android_embedded.inertia.drivers.i2c.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter
import com.dogecoding.android_embedded.inertia.drivers.i2c.log.Model

class I2cInterfaceLogFormatter : TagLogFormatter(Model.LOG_TAG) {

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