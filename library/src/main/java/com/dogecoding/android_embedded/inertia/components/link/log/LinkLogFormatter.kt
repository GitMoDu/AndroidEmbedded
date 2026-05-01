package com.dogecoding.android_embedded.inertia.components.link.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter

class LinkLogFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[LINK] ")

        val logCode = Model.LogCodeEnum.entries.getOrNull(log.code)
        val message = when (logCode) {
            Model.LogCodeEnum.StateChange -> {
                val state = Model.LinkEnum.entries.getOrNull(log.value)
                "State: ${state?.name ?: "Unknown(${log.value})"}"
            }
            null -> "❓ Unknown Code(${log.code})"
        }

        builder.append(message)
        return builder
    }
}
