package com.dogecoding.embedded.i_controller.model

import android.view.KeyEvent
import android.view.MotionEvent

interface ControllerInputListener {
    fun onGenericMotionEvent(event: MotionEvent): Boolean
    fun onKeyDownEvent(keyCode: Int, event: KeyEvent): Boolean
    fun onKeyUpEvent(keyCode: Int, event: KeyEvent): Boolean
}