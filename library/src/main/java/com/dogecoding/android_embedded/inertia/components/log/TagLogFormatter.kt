package com.dogecoding.android_embedded.inertia.components.log

import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.LogEntryFormatter

/**
 * Base class for log formatters that target a specific tag.
 */
abstract class TagLogFormatter(val targetTag: Long) : LogEntryFormatter {
    override fun canFormat(log: LogDbRecord): Boolean = log.tag == targetTag
}