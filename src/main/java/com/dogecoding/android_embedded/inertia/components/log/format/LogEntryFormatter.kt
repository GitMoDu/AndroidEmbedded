package com.dogecoding.android_embedded.inertia.components.log.format

import android.content.Context
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord

/**
 * Interface for custom log formatting based on tags.
 */
interface LogEntryFormatter {
    /**
     * Returns true if this formatter can handle the given log record.
     */
    fun canFormat(log: LogDbRecord): Boolean

    /**
     * Formats the log record into a CharSequence.
     */
    fun format(context: Context, log: LogDbRecord): CharSequence
}
