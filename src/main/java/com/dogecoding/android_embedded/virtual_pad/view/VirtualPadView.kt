package com.dogecoding.android_embedded.virtual_pad.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.dogecoding.android_embedded.virtual_pad.VirtualPad

class VirtualPadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var virtualPad: VirtualPad? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = 40f
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.GRAY
        strokeWidth = 2f
    }

    fun setVirtualPad(pad: VirtualPad) {
        this.virtualPad = pad
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pad = virtualPad ?: return

        val width = width.toFloat()
        val height = height.toFloat()

        // Draw Joysticks
        drawJoystick(canvas, 150f, height / 2, pad.getJoy1X(), pad.getJoy1Y(), "L")
        drawJoystick(canvas, width - 150f, height / 2, pad.getJoy2X(), pad.getJoy2Y(), "R")

        // Draw D-Pad
        drawDPad(canvas, 350f, height / 2, pad)

        // Draw Face Buttons
        drawFaceButtons(canvas, width - 350f, height / 2, pad)

        // Draw Triggers
        drawTriggers(canvas, width, pad)
    }

    private fun drawJoystick(canvas: Canvas, cx: Float, cy: Float, x: Short, y: Short, label: String) {
        val radius = 100f
        canvas.drawCircle(cx, cy, radius, strokePaint)

        val jx = (x.toFloat() / Short.MAX_VALUE) * radius
        val jy = (y.toFloat() / Short.MAX_VALUE) * radius

        paint.color = Color.RED
        canvas.drawCircle(cx + jx, cy + jy, 20f, paint)

        paint.color = Color.BLACK
        canvas.drawText(label, cx - 10f, cy + radius + 40f, paint)
    }

    private fun drawDPad(canvas: Canvas, cx: Float, cy: Float, pad: VirtualPad) {
        val size = 40f
        paint.color = if (pad.getDPadUp()) Color.GREEN else Color.DKGRAY
        canvas.drawRect(cx - size/2, cy - size*1.5f, cx + size/2, cy - size/2, paint)

        paint.color = if (pad.getDPadDown()) Color.GREEN else Color.DKGRAY
        canvas.drawRect(cx - size/2, cy + size/2, cx + size/2, cy + size*1.5f, paint)

        paint.color = if (pad.getDPadLeft()) Color.GREEN else Color.DKGRAY
        canvas.drawRect(cx - size*1.5f, cy - size/2, cx - size/2, cy + size/2, paint)

        paint.color = if (pad.getDPadRight()) Color.GREEN else Color.DKGRAY
        canvas.drawRect(cx + size/2, cy - size/2, cx + size*1.5f, cy + size/2, paint)
    }

    private fun drawFaceButtons(canvas: Canvas, cx: Float, cy: Float, pad: VirtualPad) {
        val radius = 30f
        val offset = 60f

        // Y (Top)
        paint.color = if (pad.getY()) Color.YELLOW else Color.DKGRAY
        canvas.drawCircle(cx, cy - offset, radius, paint)

        // A (Bottom)
        paint.color = if (pad.getA()) Color.GREEN else Color.DKGRAY
        canvas.drawCircle(cx, cy + offset, radius, paint)

        // X (Left)
        paint.color = if (pad.getX()) Color.BLUE else Color.DKGRAY
        canvas.drawCircle(cx - offset, cy, radius, paint)

        // B (Right)
        paint.color = if (pad.getB()) Color.RED else Color.DKGRAY
        canvas.drawCircle(cx + offset, cy, radius, paint)
    }

    private fun drawTriggers(canvas: Canvas, width: Float, pad: VirtualPad) {
        paint.color = if (pad.getL1()) Color.CYAN else Color.DKGRAY
        canvas.drawRect(50f, 20f, 150f, 60f, paint)

        paint.color = if (pad.getR1()) Color.CYAN else Color.DKGRAY
        canvas.drawRect(width - 150f, 20f, width - 50f, 60f, paint)

        // Analog L2/R2
        val l2Height = (pad.getL2().toFloat() / 65535f) * 100f
        paint.color = Color.MAGENTA
        canvas.drawRect(50f, 70f, 150f, 70f + l2Height, paint)

        val r2Height = (pad.getR2().toFloat() / 65535f) * 100f
        canvas.drawRect(width - 150f, 70f, width - 50f, 70f + r2Height, paint)
    }
}