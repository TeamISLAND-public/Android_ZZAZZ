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
import android.util.AttributeSet
import android.view.View
import com.teamisland.zzazz.R
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
) : View(context, attrs, defStyleAttr) {

    private lateinit var range: RangeSeekBarView
    internal var xPos = 0f

    /**
     * Sets [range].
     */
    fun setRange(rangeSeekBarView: RangeSeekBarView) {
        range = rangeSeekBarView
        range.addOnRangeSeekBarListener(object : OnRangeSeekBarListener {
            override fun onCreate(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) = Unit

            override fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                xPos = rangeSeekBarView.thumbs[index].pos + rangeSeekBarView.thumbWidth * 1f / 2
                invalidate()
            }

            override fun onSeekStart(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                visibility = GONE
                onSeek(rangeSeekBarView, index, value)
            }

            override fun onSeekStop(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                visibility = VISIBLE
                onSeek(rangeSeekBarView, index, value)
            }

            override fun onDeselect(rangeSeekBarView: RangeSeekBarView) {
                visibility = GONE
            }
        })
    }

    init {
        visibility = GONE
        isClickable = false
    }

    /**
     * Draws the view.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val arrow = resources.getDrawable(R.drawable.selected_thumb, null)
        arrow.setBounds(
                (xPos - arrow.intrinsicWidth / 2).toInt(),
                0,
                (xPos + arrow.intrinsicWidth / 2).toInt(),
                arrow.intrinsicHeight
        )
        arrow.draw(canvas)
    }
}
