package com.dogecoding.android_embedded.inertia.log.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dogecoding.android_embedded.R
import com.dogecoding.android_embedded.databinding.ItemLogEntryBinding
import com.dogecoding.android_embedded.databinding.ItemLogSessionBinding
import com.dogecoding.android_embedded.inertia.log.LogFormatter
import com.dogecoding.android_embedded.inertia.log.model.LogType

class LogAdapter : ListAdapter<LogListItem, RecyclerView.ViewHolder>(LogDiffCallback()) {

    companion object {
        private const val TYPE_SESSION = 0
        private const val TYPE_ENTRY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is LogListItem.SessionHeader -> TYPE_SESSION
            is LogListItem.Entry -> TYPE_ENTRY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SESSION -> {
                val binding = ItemLogSessionBinding.inflate(inflater, parent, false)
                SessionViewHolder(binding)
            }

            TYPE_ENTRY -> {
                val binding = ItemLogEntryBinding.inflate(inflater, parent, false)
                LogViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, position, emptyList())
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>
    ) {
        val item = getItem(position)
        if (payloads.isEmpty()) {
            when {
                holder is SessionViewHolder && item is LogListItem.SessionHeader -> holder.bind(item)
                holder is LogViewHolder && item is LogListItem.Entry -> holder.bind(item)
            }
        } else {
            // Handle partial updates if needed
            if (holder is LogViewHolder && item is LogListItem.Entry) {
                holder.bind(item)
            }
        }
    }

    class SessionViewHolder(private val binding: ItemLogSessionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: LogListItem.SessionHeader) {
            binding.tvSessionId.text = "SESSION: ${header.sessionId}"
        }
    }

    class LogViewHolder(private val binding: ItemLogEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entryItem: LogListItem.Entry) {
            val entry = entryItem.logDbRecord
            binding.tvMessage.text = LogFormatter.formatLogEntry(itemView.context, entry)

            val colorRes = when (LogType.Companion.fromUByte(entry.type.toUByte())) {
                LogType.Error -> R.color.log_text_error
                LogType.Warning -> R.color.log_text_warning
                LogType.Info -> R.color.log_text_info
                LogType.Debug -> R.color.log_text_debug
            }
            binding.tvMessage.setTextColor(ContextCompat.getColor(itemView.context, colorRes))
        }
    }

    private class LogDiffCallback : DiffUtil.ItemCallback<LogListItem>() {
        override fun areItemsTheSame(oldItem: LogListItem, newItem: LogListItem): Boolean {
            return when {
                oldItem is LogListItem.SessionHeader && newItem is LogListItem.SessionHeader -> oldItem.sessionId == newItem.sessionId

                oldItem is LogListItem.Entry && newItem is LogListItem.Entry -> oldItem.logDbRecord.id == newItem.logDbRecord.id

                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: LogListItem, newItem: LogListItem): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: LogListItem, newItem: LogListItem): Any? {
            if (oldItem is LogListItem.Entry && newItem is LogListItem.Entry) {
                if (oldItem.logDbRecord.id == newItem.logDbRecord.id) {
                    return newItem.logDbRecord
                }
            }
            return super.getChangePayload(oldItem, newItem)
        }
    }
}