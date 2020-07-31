package com.teamisland.zzazz.ui.project

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.util.Range
import androidx.annotation.ColorInt
import com.teamisland.zzazz.utils.UnitConverter.float2DP
import com.teamisland.zzazz.utils.ZoomableView

class EffectRangeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ZoomableView(context, attrs, defStyleAttr) {

    private val effectRange: Array<Range<Int>> = arrayOf()
    private val paint = Paint()

    init {
        paint.style = Paint.Style.FILL
        paint.color = 0xffff0000.toInt()
    }

    @ColorInt
    private val colorArray = intArrayOf(0xffff0000.toInt(), 0xff00ff00.toInt(), 0xff0000ff.toInt())

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) {
            Log.w("EffectRangeView", "canvas is null")
            return
        }
        for ((index, range) in effectRange.withIndex()) {
            paint.color = colorArray[index % colorArray.size]
            val startPos = getPositionOfTime(range.lower)
            val endPos = getPositionOfTime(range.upper)
            canvas.drawRect(startPos, 0f, endPos, float2DP(4f, resources), paint)
        }
    }
}