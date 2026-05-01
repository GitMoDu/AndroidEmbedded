package com.dogecoding.android_embedded.inertia.components.power_train.pwm.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.dogecoding.android_embedded.R
import com.dogecoding.android_embedded.databinding.ViewPwmControlBinding

class PwmControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewPwmControlBinding =
        ViewPwmControlBinding.inflate(LayoutInflater.from(context), this)

    private var isUpdatingInternal = false

    var onValueChanged: ((Int) -> Unit)? = null
    var onEnabledChanged: ((Boolean) -> Unit)? = null
    var onChannelChanged: ((Int) -> Unit)? = null

    init {
        val channels = (0..15).toList()
        val adapter = ArrayAdapter(context, R.layout.item_spinner_servo_channel, channels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.pwmChannelSpinner.adapter = adapter
        binding.pwmChannelSpinner.onItemSelectedListener =
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

        binding.pwmValueSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateValueText(progress)
                if (fromUser) {
                    onValueChanged?.invoke(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.pwmEnabledCheck.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingInternal) {
                onEnabledChanged?.invoke(isChecked)
            }
        }

        binding.marker0.setOnClickListener { setValue(0, true) }
        binding.marker32768.setOnClickListener { setValue(32768, true) }
        binding.marker65535.setOnClickListener { setValue(65535, true) }

        // Initialize label
        updateValueText(binding.pwmValueSeekbar.progress)
    }

    private fun updateValueText(value: Int) {
        val percentage = (value.toFloat() / 65535f * 100f)
        binding.pwmValueLabel.text = String.format("Value: %d\n(%.1f%%)", value, percentage)
    }

    fun setValue(value: Int, fromUser: Boolean = false) {
        val coerced = value.coerceIn(0, 65535)
        isUpdatingInternal = true
        binding.pwmValueSeekbar.progress = coerced
        isUpdatingInternal = false
        updateValueText(coerced)
        if (fromUser) {
            onValueChanged?.invoke(coerced)
        }
    }

    fun setPercentage(percentage: Float, fromUser: Boolean = false) {
        val value = (percentage.coerceIn(0f, 100f) / 100f * 65535f).toInt()
        setValue(value, fromUser)
    }

    fun setEnabledState(enabled: Boolean) {
        isUpdatingInternal = true
        binding.pwmEnabledCheck.isChecked = enabled
        isUpdatingInternal = false
    }

    fun setChannel(channel: Int) {
        if (channel in 0..15) {
            isUpdatingInternal = true
            binding.pwmChannelSpinner.setSelection(channel)
            isUpdatingInternal = false
        }
    }
}
