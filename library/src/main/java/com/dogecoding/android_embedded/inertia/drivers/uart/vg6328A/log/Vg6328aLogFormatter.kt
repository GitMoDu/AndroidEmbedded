package com.dogecoding.android_embedded.inertia.drivers.uart.vg6328A.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter
import com.dogecoding.android_embedded.inertia.drivers.uart.vg6328A.log.Model

class Vg6328aLogFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[VG6328A] ")

        val message =
            Model.LogCodeEnum.entries.getOrNull(log.code)?.name ?: "Unknown Code(${log.code})"

        builder.append(message)

        return builder
    }
}