package com.dogecoding.android_embedded.inertia.components.ahrs.reefwing.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter

class ReefwingAhrsLogFormatter : TagLogFormatter(Model.LOG_TAG) {

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
