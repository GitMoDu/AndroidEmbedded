package com.dogecoding.android_embedded.inertia.drivers.serial

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.TagLogFormatter
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord

class SerialInterfaceLogFormatter : TagLogFormatter(2116068916L) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[Serial] ")

        val message = when (log.code) {
            else -> "Unknown Code(${log.code})"
        }

        builder.append(message)
        return builder
    }
}