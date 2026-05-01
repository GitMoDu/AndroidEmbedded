package com.dogecoding.android_embedded.virtual_pad.android_pad

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dogecoding.android_embedded.virtual_pad.VirtualPad
import com.dogecoding.android_embedded.virtual_pad.android_pad.model.ControllerInfo


class VirtualPadViewModel : ViewModel() {
    private val mapper = VirtualPadMapper()
    private val tempPad = VirtualPad()

    val gamepadInfo = MutableLiveData<ControllerInfo?>(null)

    fun processMotionEvent(ev: MotionEvent): Boolean {
        val handled = mapper.onGenericMotionEvent(ev)
        updateControllerStatus()
        return handled
    }

    fun processKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        val handled = when (event.action) {
            KeyEvent.ACTION_DOWN -> mapper.onKeyDownEvent(keyCode, event)
            KeyEvent.ACTION_UP -> mapper.onKeyUpEvent(keyCode, event)
            else -> false
        }
        updateControllerStatus()
        return handled
    }

    fun isConnected() : Boolean{
        return tempPad.getConnected()
    }

    private fun updateControllerStatus() {
        mapper.getVirtualPadNow(tempPad)
        if (!tempPad.getConnected()) {
            if (gamepadInfo.value != null) {
                gamepadInfo.postValue(null)
            }
        }
    }

    fun getVirtualPadNow(target: VirtualPad) {
        mapper.getVirtualPadNow(target)
    }

    fun updateControllerInfo(info: ControllerInfo?) {
        if (gamepadInfo.value != info) {
            gamepadInfo.postValue(info)
            mapper.updateConnectionStatus(info != null)
        }
    }
}
