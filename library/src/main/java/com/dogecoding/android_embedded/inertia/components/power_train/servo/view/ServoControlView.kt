package com.dogecoding.android_embedded.inertia.components.power_train.servo.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.dogecoding.android_embedded.R
import com.dogecoding.android_embedded.databinding.ViewServoControlBinding

class ServoControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewServoControlBinding =
        ViewServoControlBinding.inflate(LayoutInflater.from(context), this)

    private var isUpdatingInternal = false

    var onPulseChanged: ((Int) -> Unit)? = null
    var onEnabledChanged: ((Boolean) -> Unit)? = null
    var onChannelChanged: ((Int) -> Unit)? = null

    init {
        val channels = (0..15).toList()
        val adapter = ArrayAdapter(context, R.layout.item_spinner_servo_channel, channels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.servoChannelSpinner.adapter = adapter
        binding.servoChannelSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (!isUpdatingInternal) {
                        onChannelChanged?.invoke(position)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.servoPulseSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val pulse = 1000 + progress
                updatePulseText(pulse)
                if (fromUser) {
                    onPulseChanged?.invoke(pulse)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.servoEnabledCheck.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingInternal) {
                onEnabledChanged?.invoke(isChecked)
            }
        }

        binding.marker1000.setOnClickListener { setPulse(1000, true) }
        binding.marker1500.setOnClickListener { setPulse(1500, true) }
        binding.marker2000.setOnClickListener { setPulse(2000, true) }

        // Initialize label
        val initialPulse = 1000 + binding.servoPulseSeekbar.progress
        updatePulseText(initialPulse)
    }

    private fun updatePulseText(pulse: Int) {
        binding.servoPulseLabel.text = "Pulse: ${pulse}us"
    }

    fun setPulse(pulse: Int, fromUser: Boolean = false) {
        val progress = (pulse - 1000).coerceIn(0, 1000)
        isUpdatingInternal = true
        binding.servoPulseSeekbar.progress = progress
        isUpdatingInternal = false
        updatePulseText(pulse)
        if (fromUser) {
            onPulseChanged?.invoke(pulse)
        }
    }

    fun setEnabledState(enabled: Boolean) {
        isUpdatingInternal = true
        binding.servoEnabledCheck.isChecked = enabled
        isUpdatingInternal = false
    }

    fun setChannel(channel: Int) {
        if (channel in 0..15) {
            isUpdatingInternal = true
            binding.servoChannelSpinner.setSelection(channel)
            isUpdatingInternal = false
        }
    }
}
