package com.dogecoding.android_embedded.inertia.drivers.spi

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.TagLogFormatter
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord

class SpiInterfaceLogFormatter : TagLogFormatter(4242522548L) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[SPI] ")

        val message = when (log.code) {
            else -> "Unknown Code(${log.code})"
        }

        builder.append(message)
        return builder
    }
}