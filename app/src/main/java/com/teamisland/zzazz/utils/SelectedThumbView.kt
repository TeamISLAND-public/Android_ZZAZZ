/*
 * MIT License
 *
 * Copyright (c) 2016 Knowledge, education for life.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.util.AttributeSet
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View
import com.teamisland.zzazz.video_trimmer_library.interfaces.OnRangeSeekBarListener
import com.teamisland.zzazz.video_trimmer_library.view.RangeSeekBarView


/**
 * Ranged seekbar with current position bar.
 */
@Suppress("LeakingThis")
open class SelectedThumbView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    private val markerPaint = Paint()

    internal var markerPos: Double = 0.0
    private lateinit var range: RangeSeekBarView
    internal var xPos = 0f

    fun setRange(rangeSeekBarView: RangeSeekBarView) {
        this.range = rangeSeekBarView
        range.addOnRangeSeekBarListener(object : OnRangeSeekBarListener {
            override fun onCreate(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
            }

            override fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                markerPos = value * 100.0 / rangeSeekBarView.getDuration()
                xPos = getPointInViewWidth(index)
                invalidate()
            }

            override fun onSeekStart(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                visibility = VISIBLE
                onSeek(rangeSeekBarView, index, value)
            }

            override fun onSeekStop(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                onSeek(rangeSeekBarView, index, value)
            }

            override fun onDeselect(rangeSeekBarView: RangeSeekBarView) {
                visibility = GONE
            }
        })
    }

    private fun float2DP(float: Float): Float {
        return TypedValue.applyDimension(COMPLEX_UNIT_DIP, float, context.resources.displayMetrics)
    }

    internal fun getPointInViewWidth(index: Int): Float {
        var start = float2DP(20f) / 2
        var end = width - float2DP(20f) * 3 / 2
        if (index == 1) {
            start += float2DP(20f)
            end += float2DP(20f)
        }
        return (((100.0 - markerPos) * start + markerPos * end) / 100.0).toFloat()
    }

    init {
        visibility = GONE
        markerPaint.color = 0xffffffff.toInt()
        markerPaint.style = FILL
        isClickable = false
    }

    /**
     * Draws the view.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(xPos, float2DP(3f), float2DP(3f), markerPaint)
    }
}
