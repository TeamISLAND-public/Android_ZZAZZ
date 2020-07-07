package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import com.lb.video_trimmer_library.view.RangeSeekBarView

/**
 * Expansion of RangeSeekBarView.
 * Current position marker is added.
 */
class RangeSeekBarViewWithProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : RangeSeekBarView(context, attrs, defStyleAttr) {
    private var current: Float = 0.0f
    private var strokePaint = Paint()

    /**
     * Sets current position in percentage.
     * @param valF The value to set.
     */
    fun setCurrent(valF: Float) {
        current = valF
    }

    init {
        strokePaint.isAntiAlias = true
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                2f,
                context.resources.displayMetrics
            )
        strokePaint.color = 0xffffffff.toInt()
    }

    /**
     * Draws the view.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val startPos = 95f
        val nowPos = (measuredWidth - thumbWidth - startPos) * current + paddingLeft + startPos
        canvas.drawLine(nowPos, 0F, nowPos, height.toFloat(), strokePaint)
        println(nowPos)
    }
}