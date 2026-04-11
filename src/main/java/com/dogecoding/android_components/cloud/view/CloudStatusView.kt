package com.dogecoding.android_components.cloud.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import com.dogecoding.android_embedded.R
import com.dogecoding.android_embedded.databinding.ViewCloudStatusBinding

class CloudStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val ALPHA_FULL = 1.0f
        private const val ALPHA_SYNCED = 0.6f
        private const val ALPHA_UNSYNCED = 0.5f
        private const val ALPHA_PULSE_MIN = 0.2f
        private const val ALPHA_PULSE_MAX = 0.6f
        private const val ALPHA_HIDDEN = 0.0f

        private const val DURATION_TRANSITION = 300L
        private const val DURATION_PULSE = 2000L
        private const val DURATION_POP_IN = 100L
        private const val DURATION_POP_OUT = 150L

        private const val SCALE_POP = 1.4f
        private const val SCALE_IDLE = 1.0f
    }

    enum class CloudStatus {
        ENABLED,
        UPLOADING,
        DISABLED
    }

    private val binding: ViewCloudStatusBinding =
        ViewCloudStatusBinding.inflate(LayoutInflater.from(context), this, true)

    private var currentStatus: CloudStatus = CloudStatus.DISABLED
    private var currentSyncCount: Int = 0
    private var pulseAnimator: ValueAnimator? = null

    init {
        // Ensure all views are visible so alpha can manage them
        binding.cloudIndicator.visibility = View.VISIBLE
        binding.forbiddenIndicator.visibility = View.VISIBLE
        binding.cloudProgress.visibility = View.VISIBLE
        binding.syncCountText.visibility = View.VISIBLE

        refreshUi(animate = false)
        startPulseAnimation()
    }

    fun setSyncCount(count: Int) {
        val prevCount = currentSyncCount
        currentSyncCount = count

        updateSyncText()
        updateCloudIcon()
        refreshUi(animate = true)

        if (count != prevCount && currentStatus == CloudStatus.UPLOADING) {
            animateCloudPop()
        }
    }

    fun setStatus(status: CloudStatus, animate: Boolean = true) {
        val prevStatus = currentStatus
        if (currentStatus == status) return
        currentStatus = status

        updateSyncText()
        updateCloudIcon()
        stopAnimations()

        if (status == CloudStatus.DISABLED) {
            startPulseAnimation()
        }

        if (prevStatus == CloudStatus.UPLOADING && status == CloudStatus.ENABLED) {
            animateCloudPop()
        }

        refreshUi(animate)
    }

    private fun animateCloudPop() {
        binding.cloudIndicator.animate()
            .scaleX(SCALE_POP)
            .scaleY(SCALE_POP)
            .setDuration(DURATION_POP_IN)
            .withEndAction {
                binding.cloudIndicator.animate()
                    .scaleX(SCALE_IDLE)
                    .scaleY(SCALE_IDLE)
                    .setDuration(DURATION_POP_OUT)
                    .start()
            }
            .start()
    }

    private fun updateSyncText() {
        binding.syncCountText.text = when {
            currentSyncCount < 1 -> ""
            currentSyncCount > 999 -> "999+"
            currentStatus == CloudStatus.UPLOADING -> currentSyncCount.toString()
            else -> ""
        }
    }

    private fun updateCloudIcon() {
        val isSynced = currentStatus == CloudStatus.ENABLED && currentSyncCount == 0
        val iconRes = if (isSynced) R.drawable.cloud_check else R.drawable.cloud_full
        binding.cloudIndicator.setImageResource(iconRes)
    }

    private fun refreshUi(animate: Boolean = true) {
        val isUploading = currentStatus == CloudStatus.UPLOADING
        val isEnabled = currentStatus == CloudStatus.ENABLED
        val isDisabled = currentStatus == CloudStatus.DISABLED
        val isUnsynced = isEnabled && currentSyncCount > 0

        // Global alpha for the entire view (dimmed when unsynced)
        val targetGlobalAlpha = if (isUnsynced) ALPHA_UNSYNCED else ALPHA_FULL

        // Internal component alphas
        val cloudAlpha = when {
            isDisabled -> ALPHA_PULSE_MIN
            isEnabled && !isUnsynced -> ALPHA_SYNCED
            else -> ALPHA_FULL
        }
        val forbiddenAlpha = if (isDisabled) ALPHA_PULSE_MAX else ALPHA_HIDDEN
        val progressAlpha = if (isUploading) ALPHA_FULL else ALPHA_HIDDEN
        val textAlpha = if (isUploading && !isDisabled) ALPHA_FULL else ALPHA_HIDDEN

        if (animate) {
            this.animate().alpha(targetGlobalAlpha).setDuration(DURATION_TRANSITION).start()
            if (!isDisabled) {
                binding.cloudIndicator.animate().alpha(cloudAlpha).setDuration(DURATION_TRANSITION).start()
                binding.forbiddenIndicator.animate().alpha(forbiddenAlpha).setDuration(DURATION_TRANSITION).start()
            }
            binding.cloudProgress.animate().alpha(progressAlpha).setDuration(DURATION_TRANSITION).start()
            binding.syncCountText.animate().alpha(textAlpha).setDuration(DURATION_TRANSITION).start()
        } else {
            this.animate().cancel()
            this.alpha = targetGlobalAlpha

            if (!isDisabled) {
                binding.cloudIndicator.animate().cancel()
                binding.cloudIndicator.alpha = cloudAlpha

                binding.forbiddenIndicator.animate().cancel()
                binding.forbiddenIndicator.alpha = forbiddenAlpha
            }

            binding.cloudProgress.animate().cancel()
            binding.cloudProgress.alpha = progressAlpha

            binding.syncCountText.animate().cancel()
            binding.syncCountText.alpha = textAlpha
        }

        binding.syncCountText.bringToFront()
    }

    private fun startPulseAnimation() {
        if (currentStatus != CloudStatus.DISABLED) return

        pulseAnimator = ValueAnimator.ofFloat(ALPHA_PULSE_MIN, ALPHA_PULSE_MAX).apply {
            duration = DURATION_PULSE
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                if (currentStatus == CloudStatus.DISABLED) {
                    // Cloud icon and forbidden sign pulse with opposite phases
                    binding.cloudIndicator.alpha = value
                    binding.forbiddenIndicator.alpha = (ALPHA_PULSE_MAX + ALPHA_PULSE_MIN) - value
                }
            }
            start()
        }
    }

    private fun stopAnimations() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        binding.cloudIndicator.animate().cancel()
        binding.forbiddenIndicator.animate().cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimations()
    }
}