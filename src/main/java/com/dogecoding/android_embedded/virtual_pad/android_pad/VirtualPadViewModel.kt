package com.dogecoding.android_embedded.virtual_pad.android_pad

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.lifecycle.ViewModel
import com.dogecoding.android_embedded.virtual_pad.VirtualPad

class VirtualPadViewModel : ViewModel() {
    private val mapper = VirtualPadMapper()

    fun processMotionEvent(ev: MotionEvent): Boolean {
        return mapper.onGenericMotionEvent(ev)
    }

    fun processKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        return when (event.action) {
            KeyEvent.ACTION_DOWN -> mapper.onKeyDownEvent(keyCode, event)
            KeyEvent.ACTION_UP -> mapper.onKeyUpEvent(keyCode, event)
            else -> false
        }
    }

    fun getVirtualPadNow(target: VirtualPad) {
        mapper.getVirtualPadNow(target)
    }
}