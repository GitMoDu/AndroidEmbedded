package com.dogecoding.android_embedded.serial.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.dogecoding.android_embedded.databinding.ViewSerialDebugBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SerialDebugView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewSerialDebugBinding = ViewSerialDebugBinding.inflate(
        LayoutInflater.from(context), this
    )

    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val maxLogLines = 200
    private val logLines = mutableListOf<String>()

    /**
     * Binds this view to a SerialDebugViewModel.
     * It handles the UI state and event observation.
     */
    fun bindViewModel(viewModel: SerialDebugViewModel) {
        val lifecycleOwner = findViewTreeLifecycleOwner() ?: return
        val scope = lifecycleOwner.lifecycleScope

        // Sync Checkboxes with ViewModel State
        viewModel.loopbackEnabled.onEach { 
            if (binding.checkLoopback.isChecked != it) binding.checkLoopback.isChecked = it 
        }.launchIn(scope)

        viewModel.periodicSendEnabled.onEach { 
            if (binding.checkPeriodic.isChecked != it) binding.checkPeriodic.isChecked = it 
        }.launchIn(scope)

        // UI -> ViewModel
        binding.checkLoopback.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleLoopback(isChecked)
        }

        binding.checkPeriodic.setOnCheckedChangeListener { _, isChecked ->
            viewModel.togglePeriodicSend(isChecked)
        }

        // Observe Log Events
        viewModel.logEvents.onEach { event ->
            when (event) {
                is SerialDebugViewModel.LogEvent.Data -> logData(event.data, event.isRx)
                is SerialDebugViewModel.LogEvent.Message -> logMessage(event.message)
            }
        }.launchIn(scope)
    }

    fun isLoopbackEnabled(): Boolean = binding.checkLoopback.isChecked
    
    fun isPeriodicSendEnabled(): Boolean = binding.checkPeriodic.isChecked

    fun logData(data: ByteArray, isRx: Boolean) {
        val prefix = if (isRx) "RX: " else "TX: "
        val hexString = data.joinToString(" ") { String.format("%02X", it) }
        val timestamp = dateFormat.format(Date())
        
        appendLog("[$timestamp] $prefix$hexString")
    }

    fun logMessage(message: String) {
        val timestamp = dateFormat.format(Date())
        appendLog("[$timestamp] $message")
    }

    private fun appendLog(line: String) {
        logLines.add(line)
        if (logLines.size > maxLogLines) {
            logLines.removeAt(0)
        }
        
        binding.tvLog.text = logLines.joinToString("\n")
        
        // Auto-scroll to bottom without stealing focus from parent views
        binding.logScroll.post {
            val scrollAmount = binding.tvLog.height - binding.logScroll.height
            if (scrollAmount > 0) {
                binding.logScroll.scrollTo(0, scrollAmount)
            }
        }
    }
}
