package com.dogecoding.android_embedded.inertia.components.uart_interface.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter
import com.dogecoding.android_embedded.inertia.components.uart_interface.log.Model

class UartInterfaceLogFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[UART] ")

        val message = Model.LogCodeEnum.entries.getOrNull(log.code)?.name ?: "Unknown Code(${log.code})"

        builder.append(message)

        return builder
    }
}