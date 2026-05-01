package com.dogecoding.android_embedded.inertia.components.log.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.dogecoding.android_embedded.databinding.ViewLogGroupBinding
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord

class LogView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewLogGroupBinding =
        ViewLogGroupBinding.inflate(LayoutInflater.from(context), this)

    private val adapter = LogAdapter()
    private var currentDisplayItems = listOf<LogListItem>()

    init {
        binding.logRecycler.adapter = adapter
    }

    /**
     * Set the text size of the log entries in pixels.
     */
    fun setTextSize(sizePx: Float) {
        adapter.setTextSize(sizePx)
    }

    /**
     * Set whether the internal recycler can be scrolled or interacted with.
     * Useful for small preview logs that should act as a single button.
     */
    fun setInteractionEnabled(enabled: Boolean) {
        binding.logRecycler.isClickable = enabled
        binding.logRecycler.isFocusable = enabled
        binding.logRecycler.layoutManager = object : androidx.recyclerview.widget.LinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean = enabled
        }
        // Ensure touches pass through to parent if disabled
        binding.logRecycler.setOnTouchListener(if (enabled) null else { _, _ -> false })
    }

    fun setLogs(logs: List<LogDbRecord>) {
        if (logs.isEmpty()) {
            currentDisplayItems = emptyList()
            adapter.submitList(emptyList())
            return
        }

        // Rebuild the display list to ensure correct order across boots and handle replacements
        val displayItems = mutableListOf<LogListItem>()
        var lastSessionId: Long? = null

        // Sort ascending for UI (Oldest at top, Newest at bottom)
        val sortedLogs = logs.sortedWith(compareBy({ it.bootId }, { it.recordId }))

        sortedLogs.forEach { log ->
            if (log.bootId != lastSessionId) {
                displayItems.add(LogListItem.SessionHeader(log.bootId))
                lastSessionId = log.bootId
            }
            displayItems.add(LogListItem.Entry(log))
        }

        val wasAtBottom = isAtBottom()
        currentDisplayItems = displayItems

        adapter.submitList(displayItems) {
            if (wasAtBottom && adapter.itemCount > 0) {
                binding.logRecycler.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    private fun isAtBottom(): Boolean {
        return !binding.logRecycler.canScrollVertically(1)
    }

    fun setIsDumping(isDumping: Boolean) {
        binding.progressBar.visibility = if (isDumping) VISIBLE else GONE
    }
}
