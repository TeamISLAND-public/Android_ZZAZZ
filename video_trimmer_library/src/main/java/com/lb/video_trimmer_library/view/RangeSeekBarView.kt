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
package com.lb.video_trimmer_library.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Environment
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.lb.video_trimmer_library.interfaces.OnRangeSeekBarListener
import kotlin.math.absoluteValue

/**
 * Ranged seekbar with current position bar.
 */
@Suppress("LeakingThis")
open class RangeSeekBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {
    enum class ThumbType(val index: Int) {
        LEFT(0), RIGHT(1)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    private val edgePaint = Paint()
    private val scaleRangeMax: Float = 100f
    private val shadowPaint = Paint()
    private val strokePaint = Paint()
    private val thumbTouchExtraMultiplier = initThumbTouchExtraMultiplier()
    private val thumbs = arrayOf(Thumb(ThumbType.LEFT.index), Thumb(ThumbType.RIGHT.index))
    private var currentThumb = ThumbType.LEFT.index
    private var firstRun: Boolean = true
    private var listeners = HashSet<OnRangeSeekBarListener>()
    private var maxWidth: Float = 0.toFloat()
    private var pixelRangeMax: Float = 0.toFloat()
    private var pixelRangeMin: Float = 0.toFloat()
    private var viewWidth: Int = 0

    /**
     * Thumb width.
     */
    val thumbWidth: Int = initThumbWidth(context)

    /**
     * Current video position in percentage.
     */
    var currentPos: Float = 0F


    init {
        isFocusable = true
        isFocusableInTouchMode = true

        shadowPaint.isAntiAlias = true
        shadowPaint.color = initShadowColor()

        strokePaint.isAntiAlias = true
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                3f,
                context.resources.displayMetrics
            )
        strokePaint.color = 0xff44FF9A.toInt()

        edgePaint.isAntiAlias = true
        edgePaint.color = 0xffffffff.toInt()
        edgePaint.strokeWidth =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                2f,
                context.resources.displayMetrics
            )
    }

    @ColorInt
    open fun initShadowColor(): Int = 0xB1000000.toInt()

    open fun initThumbTouchExtraMultiplier(): Float = 1.0f

    open fun initThumbWidth(context: Context): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            33f,
            context.resources.displayMetrics
        ).toInt().coerceAtLeast(1)

    fun initMaxWidth() {
        maxWidth = thumbs[ThumbType.RIGHT.index].pos - thumbs[ThumbType.LEFT.index].pos
        onSeekStop(this, ThumbType.LEFT.index, thumbs[ThumbType.LEFT.index].value)
        onSeekStop(this, ThumbType.RIGHT.index, thumbs[ThumbType.RIGHT.index].value)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = measuredWidth
        pixelRangeMin = 0f
        pixelRangeMax = (viewWidth - thumbWidth).toFloat()
        if (firstRun) {
            for ((index, thumb) in thumbs.withIndex()) {
                thumb.value = scaleRangeMax * index
                thumb.pos = pixelRangeMax * index
            }
            // Fire listener callback
            onCreate(this, currentThumb, getThumbValue(currentThumb))
            firstRun = false
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (thumbs.isEmpty())
            return
        // draw shadows outside of selected range
        val hOffset = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            27f,
            context.resources.displayMetrics
        )
        val hOffset2 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            13f,
            context.resources.displayMetrics
        )
        val hOffset3 =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                14f,
                context.resources.displayMetrics
            )
        val wOffset =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                3f,
                context.resources.displayMetrics
            )
        for (thumb in thumbs) {
            if (thumb.index == ThumbType.LEFT.index) {
                val x = thumb.pos + paddingLeft
                if (x > pixelRangeMin)
                    canvas.drawRect(
                        thumbWidth.toFloat(),
                        hOffset,
                        (x + thumbWidth),
                        height.toFloat(),
                        shadowPaint
                    )
            } else {
                val x = thumb.pos - paddingRight
                if (x < pixelRangeMax)
                    canvas.drawRect(
                        x,
                        hOffset,
                        (viewWidth - thumbWidth).toFloat(),
                        height.toFloat(),
                        shadowPaint
                    )
            }
        }
        //draw stroke around selected range
        val currentMarker = (width.toFloat() - 2 * thumbWidth) * currentPos + thumbWidth

        canvas.drawLine(currentMarker, hOffset2, currentMarker, height.toFloat(), edgePaint)

        canvas.drawRect(
            (thumbs[ThumbType.LEFT.index].pos + paddingLeft + thumbWidth),
            hOffset,
            thumbs[ThumbType.RIGHT.index].pos - paddingRight,
            height.toFloat() - hOffset3,
            strokePaint
        )

        //left
        canvas.drawRect(
            (thumbs[ThumbType.LEFT.index].pos + paddingLeft + thumbWidth) - wOffset,
            hOffset,
            (thumbs[ThumbType.LEFT.index].pos + paddingLeft + thumbWidth),
            height.toFloat() - hOffset3,
            strokePaint
        )
        //right
        canvas.drawRect(
            thumbs[ThumbType.RIGHT.index].pos - paddingRight,
            hOffset,
            thumbs[ThumbType.RIGHT.index].pos - paddingRight + wOffset,
            height.toFloat() - hOffset3,
            strokePaint
        )
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val mThumb: Thumb
        val mThumb2: Thumb
        val coordinate = ev.x
        val action = ev.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                // Remember where we started
                currentThumb = getClosestThumb(coordinate)
                if (currentThumb == -1)
                    return false
                mThumb = thumbs[currentThumb]
                mThumb.lastTouchX = coordinate
                onSeekStart(this, currentThumb, mThumb.value)
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (currentThumb == -1)
                    return false
                mThumb = thumbs[currentThumb]
                onSeekStop(this, currentThumb, mThumb.value)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                mThumb = thumbs[currentThumb]
                mThumb2 =
                    thumbs[if (currentThumb == ThumbType.LEFT.index) ThumbType.RIGHT.index else ThumbType.LEFT.index]
                // Calculate the distance moved
                val dx = coordinate - mThumb.lastTouchX
                val newX = mThumb.pos + dx
                when {
                    currentThumb == 0 -> when {
                        newX + thumbWidth >= mThumb2.pos -> mThumb.pos = mThumb2.pos - thumbWidth
                        newX <= pixelRangeMin -> mThumb.pos = pixelRangeMin
                        else -> {
                            //Check if thumb is not out of max width
                            checkPositionThumb(mThumb, mThumb2, dx, true)
                            // Move the object
                            mThumb.pos = mThumb.pos + dx
                            // Remember this touch position for the next move event
                            mThumb.lastTouchX = coordinate
                        }
                    }
                    newX <= mThumb2.pos + thumbWidth -> mThumb.pos = mThumb2.pos + thumbWidth
                    newX >= pixelRangeMax -> mThumb.pos = pixelRangeMax
                    else -> {
                        //Check if thumb is not out of max width
                        checkPositionThumb(mThumb2, mThumb, dx, false)
                        // Move the object
                        mThumb.pos = mThumb.pos + dx
                        // Remember this touch position for the next move event
                        mThumb.lastTouchX = coordinate
                    }
                }
                setThumbPos(currentThumb, mThumb.pos)
                // Invalidate to request a redraw
                invalidate()
                return true
            }
        }
        return false
    }

    private fun checkPositionThumb(
        thumbLeft: Thumb,
        thumbRight: Thumb,
        dx: Float,
        isLeftMove: Boolean
    ) {
        if (isLeftMove && dx < 0) {
            if (thumbRight.pos - (thumbLeft.pos + dx) > maxWidth) {
                thumbRight.pos = thumbLeft.pos + dx + maxWidth
                setThumbPos(ThumbType.RIGHT.index, thumbRight.pos)
            }
        } else if (!isLeftMove && dx > 0) {
            if (thumbRight.pos + dx - thumbLeft.pos > maxWidth) {
                thumbLeft.pos = thumbRight.pos + dx - maxWidth
                setThumbPos(ThumbType.LEFT.index, thumbLeft.pos)
            }
        }
    }

    private fun pixelToScale(index: Int, pixelValue: Float): Float {
        val scale = pixelValue * 100 / pixelRangeMax
        return if (index == 0) {
            val pxThumb = scale * thumbWidth / 100
            scale + pxThumb * 100 / pixelRangeMax
        } else {
            val pxThumb = (100 - scale) * thumbWidth / 100
            scale - pxThumb * 100 / pixelRangeMax
        }
    }

    private fun scaleToPixel(index: Int, scaleValue: Float): Float {
        val px = scaleValue * pixelRangeMax / 100
        return if (index == 0) {
            val pxThumb = scaleValue * thumbWidth / 100
            px - pxThumb
        } else {
            val pxThumb = (100 - scaleValue) * thumbWidth / 100
            px + pxThumb
        }
    }

    private fun calculateThumbValue(index: Int) {
        if (index < thumbs.size && !thumbs.isEmpty()) {
            val th = thumbs[index]
            th.value = pixelToScale(index, th.pos)
            onSeek(this, index, th.value)
        }
    }

    private fun calculateThumbPos(index: Int) {
        if (index < thumbs.size && !thumbs.isEmpty()) {
            val th = thumbs[index]
            th.pos = scaleToPixel(index, th.value)
        }
    }

    private fun getThumbValue(index: Int): Float {
        return thumbs[index].value
    }

    fun setThumbValue(index: Int, value: Float) {
        thumbs[index].value = value
        calculateThumbPos(index)
        // Tell the view we want a complete redraw
        invalidate()
    }

    private fun setThumbPos(index: Int, pos: Float) {
        thumbs[index].pos = pos
        calculateThumbValue(index)
        // Tell the view we want a complete redraw
        invalidate()
    }

    private fun getClosestThumb(xPos: Float): Int {
        if (thumbs.isEmpty())
            return -1
        var closest = -1
        var minDistanceFound = Float.MAX_VALUE
        val x = xPos - thumbWidth//+ paddingLeft
//        Log.d("AppLog", "xPos:$xPos -> x: $x")
        for (thumb in thumbs) {
            val thumbPos =
                if (thumb.index == ThumbType.LEFT.index) thumb.pos else thumb.pos - thumbWidth
//            Log.d("AppLog", "thumb ${thumb.index} pos: $thumbPos")
            // Find thumb closest to x coordinate
            val xMin = thumbPos - thumbWidth * thumbTouchExtraMultiplier
            val xMax = thumbPos + thumbWidth * thumbTouchExtraMultiplier
            if (x in xMin..xMax) {
                val distance = (thumbPos - x).absoluteValue
                if (distance < minDistanceFound) {
                    closest = thumb.index
//                    Log.d("AppLog", "x: $x distance: $distance selectedThumb:$closest")
                    minDistanceFound = distance
                }
            }
        }
        return closest
    }

    fun addOnRangeSeekBarListener(listener: OnRangeSeekBarListener) {
        listeners.add(listener)
    }

    private fun onCreate(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
        listeners.forEach { item -> item.onCreate(rangeSeekBarView, index, value) }
    }

    private fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
        listeners.forEach { item -> item.onSeek(rangeSeekBarView, index, value) }
    }

    private fun onSeekStart(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
        listeners.forEach { item -> item.onSeekStart(rangeSeekBarView, index, value) }
    }

    private fun onSeekStop(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
        listeners.forEach { item -> item.onSeekStop(rangeSeekBarView, index, value) }
    }

    class Thumb(val index: Int = 0) {
        var value: Float = 0f
        var pos: Float = 0f
        var lastTouchX: Float = 0f
    }

}
