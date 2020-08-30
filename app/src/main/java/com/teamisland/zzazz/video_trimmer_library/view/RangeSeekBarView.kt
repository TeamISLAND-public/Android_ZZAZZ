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
package com.teamisland.zzazz.video_trimmer_library.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.FILL_AND_STROKE
import android.graphics.Path
import android.util.AttributeSet
import android.util.Range
import android.view.MotionEvent
import android.view.View
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.view.CurrentPositionView
import com.teamisland.zzazz.utils.ITrimmingData
import com.teamisland.zzazz.utils.view.SelectedThumbView
import com.teamisland.zzazz.utils.UnitConverter.float2DP
import kotlin.math.abs
import kotlin.math.round

/**
 * Ranged seekbar with current position bar.
 */
@Suppress("LeakingThis")
open class RangeSeekBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val leftThumb = Thumb(0)
    private val rightThumb = Thumb(1)

    private val currentThumb: Thumb?
        get() {
            return when (currentThumbIndex) {
                0 -> leftThumb
                1 -> rightThumb
                else -> null
            }
        }

    // shadow rectangle paint of ends
    private val thumbLimitPaint = Paint().apply {
        isAntiAlias = true
        color = 0xff2b2b2b.toInt()
    }

    // shadow rectangle on untrimmed range
    private val outsideThumbShadow = Paint().apply {
        color = 0xb2070707.toInt()
    }

    // paint of thumbs
    private val thumbPaint = Paint().apply {
        isAntiAlias = true
        style = FILL
        strokeWidth = float2DP(3f, resources)
        color = resources.getColor(R.color.PointColor, null)
    }

    // paint of triangles in thumbs
    private val trianglePaint = Paint().apply {
        color = 0xff2a2a2a.toInt()
        isAntiAlias = true
        style = FILL_AND_STROKE
    }

    // Path of thumb triangles.
    private val leftTriangle by lazy {
        Path().apply {
            val dp5 = float2DP(5f, resources)
            val dp4 = float2DP(4f, resources)
            val dp10 = float2DP(10f, resources)
            moveTo(dp5, 0f)
            lineTo(dp10, dp4)
            lineTo(dp10, -dp4)
            close()
        }
    }
    private val rightTriangle by lazy {
        Path().apply {
            val dp4 = float2DP(4f, resources)
            val dp11 = float2DP(11f, resources)
            moveTo(-float2DP(6f, resources), 0f)
            lineTo(-dp11, dp4)
            lineTo(-dp11, -dp4)
            close()
        }
    }

    internal var currentThumbIndex: Int = -1

    internal lateinit var bindData: ITrimmingData

    internal lateinit var currentPositionView: CurrentPositionView

    internal lateinit var selectedThumbView: SelectedThumbView

    /**
     * Thumb width.
     */
    internal val thumbWidth: Int = float2DP(16f, resources).coerceAtLeast(1f).toInt()

    /**
     *
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedThumbView.visibility = GONE

                currentThumbIndex = getClosestThumb(event.x)

                if (currentThumbIndex != -1) {
                    val currentThumb1 = currentThumb!!
                    currentThumb1.lastTouchX = event.x - currentThumb1.position
                    currentPositionView.visibleTrimCurrent()
                }

                if (currentThumbIndex == -1 && event.x in leftThumb.position..rightThumb.position)
                    return false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (currentThumbIndex == -1) return true

                var newPos = event.x - currentThumb!!.lastTouchX
                if (currentThumbIndex == 1)
                    newPos -= thumbWidth

                val fl = width - 2f * thumbWidth
                val pos3 = Range(0f, fl).clamp(newPos - x)

                val snapPosition = round(pos3 * bindData.frameCount / fl).toLong()

                if (currentThumbIndex == 0) bindData.rangeStartIndex = snapPosition
                else bindData.rangeExclusiveEndIndex = snapPosition
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (currentThumbIndex == -1) return true

                selectedThumbView.visibility = VISIBLE
                currentPositionView.visibleMarkerCurrent()
                bindData.updateUI()
                return true
            }
        }
        return false
    }

    /**
     *
     */
    override fun invalidate() {
        super.invalidate()

        selectedThumbView.xPos = (currentThumb ?: return).position + thumbWidth / 2
        selectedThumbView.invalidate()
    }

    /**
     * [View.onDraw]
     * Draws the view.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val heightF = height.toFloat()
        val widthF = width.toFloat()
        val thumbWidthF = thumbWidth.toFloat()
        val widthSubThumb = widthF - thumbWidthF

        // Thumb shadow.
        canvas.drawRect(0f, 0f, 0f + thumbWidth, heightF, thumbLimitPaint)
        canvas.drawRect(widthF, 0f, widthSubThumb, heightF, thumbLimitPaint)

        if (bindData.rangeStartIndex == 0L && bindData.rangeExclusiveEndIndex == bindData.frameCount) {
            thumbPaint.color = 0xff2b2b2b.toInt()
            trianglePaint.color = resources.getColor(R.color.PointColor, null)
        } else {
            thumbPaint.color = resources.getColor(R.color.PointColor, null)
            trianglePaint.color = 0xff2a2a2a.toInt()
        }

        val leftPosEnd = leftThumb.position + thumbWidth
        val rightPosEnd = rightThumb.position + thumbWidth

        // Shadow at the outside of thumbs.
        if (bindData.rangeStartIndex > 0L)
            canvas.drawRect(thumbWidthF, 0f, leftThumb.position, heightF, outsideThumbShadow)
        if (bindData.rangeExclusiveEndIndex < bindData.frameCount)
            canvas.drawRect(rightPosEnd, 0f, widthSubThumb, heightF, outsideThumbShadow)

        // Draw trimming thumbs.
        canvas.drawRect(leftThumb.position, 0f, leftPosEnd, heightF, thumbPaint)
        canvas.drawRect(rightThumb.position, 0f, rightPosEnd, heightF, thumbPaint)

        // Draw triangles!
        canvas.save()
        canvas.translate(leftThumb.position, heightF / 2)
        canvas.drawPath(leftTriangle, trianglePaint)
        canvas.translate(rightPosEnd - leftThumb.position, 0f)
        canvas.drawPath(rightTriangle, trianglePaint)
        canvas.restore()
    }

    private fun getClosestThumb(xPos: Float): Int {
        val acceptDistance = thumbWidth

        val thumbWidthHalf = thumbWidth / 2f
        val leftDistance = abs(leftThumb.position + thumbWidthHalf - xPos)
        val rightDistance = abs(rightThumb.position + thumbWidthHalf - xPos)
        when {
            rightDistance < leftDistance -> if (rightDistance < acceptDistance) return 1
            leftDistance < acceptDistance -> return 0
        }
        return -1
    }

    internal inner class Thumb(private val index: Int) {
        internal val position: Float
            get() {
                return when (index) {
                    0 -> {
                        (width.toFloat() - 2 * thumbWidth) * bindData.rangeStartIndex / bindData.frameCount
                    }
                    1 -> {
                        (width.toFloat() - 2 * thumbWidth) * bindData.rangeExclusiveEndIndex / bindData.frameCount + thumbWidth
                    }
                    else -> 0f
                }
            }
        internal var lastTouchX: Float = 0f
    }
}
