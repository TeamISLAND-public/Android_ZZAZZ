package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.lb.video_trimmer_library.view.RangeSeekBarView

class RangeSeekBarViewWithProgress(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int
) : RangeSeekBarView(context, attrs, defStyleAttr) {
    private var current: Float = 0.0f

    fun setCurrent(valF: Float) {
        current = valF
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        println(current)
    }
}