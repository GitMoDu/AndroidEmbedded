package com.dogecoding.android_embedded.inertia.components.log.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter

class LogMetaFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[LOG] ")

        val message =
            Model.LogCodeEnum.entries.getOrNull(log.code)?.name ?: "Unknown Code(${log.code})"

        builder.append(message)
        return builder
    }
}