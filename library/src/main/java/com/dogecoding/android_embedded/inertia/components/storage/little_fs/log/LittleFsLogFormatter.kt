package com.dogecoding.android_embedded.inertia.components.storage.little_fs.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter

class LittleFsLogFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[LittleFs] ")

        val logCode = Model.LogCodeEnum.entries.getOrNull(log.code)
        val message = when (logCode) {
            Model.LogCodeEnum.BeginFailed -> "❌ BeginFailed"
            Model.LogCodeEnum.FormatFailed -> "❌ FormatFailed"
            Model.LogCodeEnum.Format -> "🧹 Format"
            Model.LogCodeEnum.BeginAfterFormatFailed -> "❌ BeginAfterFormatFailed"
            Model.LogCodeEnum.Mounted -> "💾 Mounted"
            Model.LogCodeEnum.Unmounted -> "🔌 Unmounted"
            null -> "❓ Unknown Code(${log.code})"
        }

        builder.append(message)
        return builder
    }
}
