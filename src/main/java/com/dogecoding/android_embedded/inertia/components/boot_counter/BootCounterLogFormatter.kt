package com.dogecoding.android_embedded.inertia.components.boot_counter

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.TagLogFormatter
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord

class BootCounterLogFormatter : TagLogFormatter(890053290) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[BootCounter] ")

        val message = when (log.code) {
            0 -> "Booted"
            1 -> "ErrorNoCounterSource"
            else -> "Unknown Code(${log.code})"
        }

        builder.append(message)
        builder.append(" Counter: ")
        builder.append(log.bootId.toString())

        return builder
    }
}