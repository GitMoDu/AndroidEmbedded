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

        val message = when (log.code) {
            LogCodeEnum.Booted.ordinal -> "${LogCodeEnum.Booted.name}(%${log.bootId})"
            LogCodeEnum.ErrorNoCounterSource.ordinal -> LogCodeEnum.ErrorNoCounterSource.name
            else -> "Unknown Code(${log.code})"
        }
        builder.append(message)

        return builder
    }
}