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
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension
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

    private lateinit var range: RangeSeekBarView
    internal var xPos = 0f

    fun setRange(rangeSeekBarView: RangeSeekBarView) {
        this.range = rangeSeekBarView
        range.addOnRangeSeekBarListener(object : OnRangeSeekBarListener {
            override fun onCreate(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) = Unit

            override fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                xPos =
                    rangeSeekBarView.thumbs[index].pos + rangeSeekBarView.thumbWidth * (index + 1f / 2)
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
        val applyDimension =
            applyDimension(COMPLEX_UNIT_DIP, 3f, context.resources.displayMetrics)
        canvas.drawCircle(
            xPos,
            applyDimension,
            applyDimension,
            markerPaint
        )
    }
}
