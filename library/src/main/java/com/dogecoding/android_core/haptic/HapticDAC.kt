package com.dogecoding.android_core.haptic

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import com.dogecoding.android_core.haptic.HapticConstants.Companion.BASE_DURATION_LONG
import com.dogecoding.android_core.haptic.HapticConstants.Companion.LOW_AMPLITUDE
import com.dogecoding.android_core.haptic.HapticConstants.Companion.MEDIUM_AMPLITUDE
import com.dogecoding.android_core.haptic.HapticConstants.Companion.MODERN_SHORT_MILLIS
import com.dogecoding.android_core.haptic.HapticConstants.Companion.SIGNAL_END_DURATION
import com.dogecoding.android_core.haptic.HapticConstants.Companion.SIGNAL_START_DURATION
import com.dogecoding.android_core.haptic.HapticConstants.Companion.VOICE_COUNT
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

abstract class HapticDAC(context: Context) {

    companion object {
        private fun getLongDuration(): Long {
            return BASE_DURATION_LONG + MODERN_SHORT_MILLIS
        }

        private fun getTick(): Any {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            } else {
                VibrationEffect.createOneShot(
                    MODERN_SHORT_MILLIS, LOW_AMPLITUDE
                )
            }
        }

        private fun getClick(): Any {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            } else {
                VibrationEffect.createOneShot(
                    MODERN_SHORT_MILLIS, LOW_AMPLITUDE
                )
            }
        }

        private fun getShort(): Any {
            return VibrationEffect.createOneShot(
                MODERN_SHORT_MILLIS, LOW_AMPLITUDE
            )
        }

        private fun getLong(): Any {
            return VibrationEffect.createOneShot(
                getLongDuration(), MEDIUM_AMPLITUDE
            )
        }

        private fun getStart(): Any {
            return VibrationEffect.createWaveform(longArrayOf(0, SIGNAL_START_DURATION), -1)
        }

        private fun getEnd(): Any {
            return VibrationEffect.createWaveform(longArrayOf(0, SIGNAL_END_DURATION), -1)
        }
    }

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val tickEffect: Any = getTick()
    private val clickEffect: Any = getClick()
    private val shortEffect: Any = getShort()
    private val longEffect: Any = getLong()
    private val startEffect: Any = getStart()
    private val endEffect: Any = getEnd()

    private val scheduledTaskExecutor: ScheduledExecutorService =
        Executors.newScheduledThreadPool(VOICE_COUNT)

    private val hasVibrator = vibrator.hasVibrator()

    protected var isEnabled: Boolean = true

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun onStop() {
        vibrator.cancel()
    }

    fun getHapticFeedbackEnabled(): Boolean {
        return isEnabled
    }

    fun setHapticFeedbackEnabled(on: Boolean) {
        isEnabled = on
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrateShort() {
        if (hasVibrator && isEnabled) {
            scheduledTaskExecutor.execute {
                vibrator.vibrate(shortEffect as VibrationEffect)
            }
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrateLong() {
        if (hasVibrator && isEnabled) {
            scheduledTaskExecutor.execute {
                vibrator.vibrate(longEffect as VibrationEffect)
            }
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrateStartSignal() {
        if (hasVibrator && isEnabled) {
            scheduledTaskExecutor.execute {
                vibrator.vibrate(startEffect as VibrationEffect)
            }
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrateEndSignal() {
        if (hasVibrator && isEnabled) {
            scheduledTaskExecutor.execute {
                vibrator.vibrate(endEffect as VibrationEffect)
            }
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrateClick() {
        if (hasVibrator && isEnabled) {
            scheduledTaskExecutor.execute {
                vibrator.vibrate(clickEffect as VibrationEffect)
            }
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrateTick() {
        if (hasVibrator && isEnabled) {
            scheduledTaskExecutor.execute {
                vibrator.vibrate(tickEffect as VibrationEffect)
            }
        }
    }
}
