package com.dogecoding.android_embedded.inertia.drivers.servo.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.dogecoding.android_embedded.databinding.ViewServoControlBinding

class ServoControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewServoControlBinding =
        ViewServoControlBinding.inflate(LayoutInflater.from(context), this)

    var onPulseChanged: ((Int) -> Unit)? = null
    var onEnabledChanged: ((Boolean) -> Unit)? = null

    init {
        binding.pulseSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val pulse = 1000 + progress
                binding.pulseLabel.text = "Pulse: ${pulse}us"
                if (fromUser) {
                    onPulseChanged?.invoke(pulse)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.servoEnabled.setOnCheckedChangeListener { _, isChecked ->
            onEnabledChanged?.invoke(isChecked)
        }

        // Initialize label
        val initialPulse = 1000 + binding.pulseSeekBar.progress
        binding.pulseLabel.text = "Pulse: ${initialPulse}us"
    }

    fun setPulse(pulse: Int) {
        val progress = (pulse - 1000).coerceIn(0, 1000)
        binding.pulseSeekBar.progress = progress
    }

    fun setEnabledState(enabled: Boolean) {
        binding.servoEnabled.isChecked = enabled
    }
}
