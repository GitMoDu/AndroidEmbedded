package com.dogecoding.android_core.haptic

import android.content.Context
import java.lang.ref.WeakReference

class HapticEngine private constructor(context: Context) : HapticDAC(context) {

    companion object {
        private var instance = WeakReference<HapticEngine?>(null)

        fun getInstance(context: Context): HapticEngine {
            var haptic = instance.get()

            if (haptic == null) {
                haptic = HapticEngine(context)
                instance = WeakReference<HapticEngine?>(haptic)
            }

            return haptic
        }
    }
}