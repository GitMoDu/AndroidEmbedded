package com.dogecoding.android_embedded.inertia.components.boot_counter.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.boot_counter.log.Model
import com.dogecoding.android_embedded.inertia.components.boot_counter.log.Model.LogCodeEnum
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter

class BootCounterLogFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[BootCounter] ")

        val logCode = Model.LogCodeEnum.entries.getOrNull(log.code)
        val message = when (logCode) {
            Model.LogCodeEnum.Booted -> "🚀 Booted (#${log.bootId})"
            Model.LogCodeEnum.ErrorNoCounterSource -> "⚠️ ErrorNoCounterSource"
            null -> "❓ Unknown Code(${log.code})"
        }
        builder.append(message)

        return builder
    }
}