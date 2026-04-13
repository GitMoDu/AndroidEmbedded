package com.dogecoding.android_embedded.inertia.drivers.ahrs.reefwing

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.TagLogFormatter
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord

class ReefwingAhrsLogFormatter : TagLogFormatter(2504544144L) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[AHRS Reefwing] ")

        val message = when (log.code) {
            else -> "Unknown Code(${log.code})"
        }

        builder.append(message)
        return builder
    }
}