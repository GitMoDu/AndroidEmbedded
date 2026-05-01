package com.dogecoding.android_embedded.inertia.components.core.lifecycle.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter

class LifecycleLogFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[LIFECYCLE] ")

        val logCode = Model.LogCodeEnum.entries.getOrNull(log.code)
        val message = when (logCode) {
            Model.LogCodeEnum.Booted -> "🚀 Booted"
            Model.LogCodeEnum.ErrorNoCounterSource -> "⚠️ ErrorNoCounterSource"
            null -> "❓ Unknown Code(${log.code})"
        }

        builder.append(message)

        if (log.value != 0) {
            builder.append(" (Val: ${log.value})")
        }

        return builder
    }
}
