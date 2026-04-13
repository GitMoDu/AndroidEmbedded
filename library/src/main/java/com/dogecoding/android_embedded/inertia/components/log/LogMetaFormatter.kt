package com.dogecoding.android_embedded.inertia.components.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord

class LogMetaFormatter : TagLogFormatter(985820946) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[Inertia Log] ")

        val message = when (log.code) {
            0 -> "ErrorRepositoryInvalidEntry"
            1 -> "ErrorCorruptedEntry"
            else -> "Unknown Code(${log.code})"
        }

        builder.append(message)
        return builder
    }
}