package com.dogecoding.android_embedded.virtual_pad.controller

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View

class NativeControllerCaptureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var inputListener: ControllerInputListener? = null

    fun setControllerInputListener(listener: ControllerInputListener?) {
        this.inputListener = listener
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (inputListener != null
            && event != null
        ) {
            return inputListener!!.onGenericMotionEvent(event)
        } else {
            return super.onGenericMotionEvent(event)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (inputListener != null
            && event != null
        ) {
            return inputListener!!.onKeyDownEvent(keyCode, event)
        } else {
            return super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (inputListener != null
            && event != null
        ) {
            return inputListener!!.onKeyUpEvent(keyCode, event)
        } else {
            return super.onKeyUp(keyCode, event)
        }
    }
}
