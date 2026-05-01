package com.dogecoding.android_embedded.inertia.drivers.optical_flow.mtf0x.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter

class Mtf0xLogFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[MTF0X] ")

        val message = Model.LogCodeEnum.fromInt(log.code)?.name ?: "Unknown Code(${log.code})"
        builder.append(message)

        if (log.value != 0) {
            builder.append(" (Value: ${log.value})")
        }

        return builder
    }
}
