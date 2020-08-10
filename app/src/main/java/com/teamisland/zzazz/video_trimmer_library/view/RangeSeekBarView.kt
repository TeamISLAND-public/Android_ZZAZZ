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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.FILL_AND_STROKE
import android.graphics.Path
import android.util.AttributeSet
import android.util.Range
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View
import android.widget.Button
import androidx.annotation.ColorInt
import com.teamisland.zzazz.R
import com.teamisland.zzazz.video_trimmer_library.interfaces.OnRangeSeekBarListener
import com.teamisland.zzazz.video_trimmer_library.view.RangeSeekBarView.ThumbType.LEFT
import com.teamisland.zzazz.video_trimmer_library.view.RangeSeekBarView.ThumbType.RIGHT
import kotlin.math.absoluteValue

/**
 * Ranged seekbar with current position bar.
 */
@Suppress("LeakingThis")
open class RangeSeekBarView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * ThumbType.
     * @property index Index of thumb
     */
    enum class ThumbType(val index: Int) {
        /**
         * Left thumb; start
         */
        LEFT(0),

        /**
         * Right thumb; end
         */
        RIGHT(1)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    // shadow rectangle paint of ends
    private val shadowPaint = Paint()

    // shadow rectangle on untrimmed range
    private val unTrimmedPaint = Paint()

    // paint of thumbs
    private val strokePaint = Paint()

    // paint of triangles in thumbs
    private val trianglePaint = Paint()

    /**
     * Array of thumbs
     */
    val thumbs: Array<Thumb> = arrayOf(Thumb(LEFT.index), Thumb(RIGHT.index))
    private var listeners = HashSet<OnRangeSeekBarListener>()
    internal var viewWidth: Int = 0
    private lateinit var frameAdvance: Button
    private lateinit var frameRetreat: Button
    internal var currentThumb: Int = -1
    private var videoFrameCount: Int = 0
    private val leftTriangle = Path()
    private val rightTriangle = Path()

    /**
     * Duration of target video.
     */
    var videoDuration: Int = 0

    /**
     * Thumb width.
     */
    val thumbWidth: Int = initThumbWidth(context)

    /**
     * Get start point in ms.
     */
    fun getStart(): Int =
            (thumbs[LEFT.index].value.toDouble() / (videoFrameCount - 1) * videoDuration).toInt()

    /**
     * Get endpoint in ms.
     */
    fun getEnd(): Int =
            (thumbs[RIGHT.index].value.toDouble() / (videoFrameCount - 1) * videoDuration).toInt()

    /**
     * Get start frame.
     */
    fun getFrameStart(): Int = thumbs[LEFT.index].value

    /**
     * Get end frames.
     */
    fun getFrameEnd(): Int = thumbs[RIGHT.index].value

    /**
     * Get range selected in ms.
     */
    fun getRange(): Range<Int> = Range(getStart(), getEnd())

    internal fun float2DP(float: Float): Float {
        return TypedValue.applyDimension(COMPLEX_UNIT_DIP, float, context.resources.displayMetrics)
    }

    /**
     * Sets the duration of the video.
     */
    fun setFrameCount(count: Int) {
        videoFrameCount = count
        thumbs[LEFT.index].value = 0
        thumbs[RIGHT.index].value = count - 1
    }

    private fun setStrokePaint() {
        strokePaint.isAntiAlias = true
        strokePaint.style = FILL
        strokePaint.strokeWidth = float2DP(3f)
        strokePaint.color = 0xffff3898.toInt()
    }

    private fun setShadowPaint() {
        shadowPaint.isAntiAlias = true
        shadowPaint.color = initShadowColor()
    }

    private fun setUnTrimmedPaint() {
        unTrimmedPaint.color = 0xb2070707.toInt()
    }

    private fun setTrianglePaint() {
        trianglePaint.color = 0xff2a2a2a.toInt()
        trianglePaint.isAntiAlias = true
        trianglePaint.style = FILL_AND_STROKE
    }

    init {
        isFocusable = true
        isFocusableInTouchMode = true

        setStrokePaint()
        setShadowPaint()
        setUnTrimmedPaint()
        setTrianglePaint()
    }

    /**
     * Sets frame move button.
     */
    fun setButtons(advance: Button, retreat: Button) {
        frameAdvance = advance
        frameRetreat = retreat
        setButtonVisibility()
    }

    internal fun setButtonVisibility() {
        when (currentThumb) {
            -1 -> {
                frameAdvance.visibility = INVISIBLE
                frameRetreat.visibility = INVISIBLE
            }
            LEFT.index -> {
                frameAdvance.visibility = VISIBLE
                frameRetreat.visibility = VISIBLE
                if (thumbs[LEFT.index].value + 1 < thumbs[RIGHT.index].value) {
                    frameAdvance.background = resources.getDrawable(R.drawable.ic_button_desel, null)
                    frameAdvance.isClickable = true
                } else {
                    frameAdvance.background = resources.getDrawable(R.drawable.ic_button_sel, null)
                    frameAdvance.isClickable = false
                }
                if (thumbs[LEFT.index].value > 0) {
                    frameRetreat.background = resources.getDrawable(R.drawable.ic_button_desel, null)
                    frameRetreat.isClickable = true
                } else {
                    frameRetreat.background = resources.getDrawable(R.drawable.ic_button_sel, null)
                    frameRetreat.isClickable = false
                }
            }
            RIGHT.index -> {
                frameAdvance.visibility = VISIBLE
                frameRetreat.visibility = VISIBLE
                if (thumbs[RIGHT.index].value < videoFrameCount - 1) {
                    frameAdvance.background = resources.getDrawable(R.drawable.ic_button_desel, null)
                    frameAdvance.isClickable = true
                } else {
                    frameAdvance.background = resources.getDrawable(R.drawable.ic_button_sel, null)
                    frameAdvance.isClickable = false
                }
                if (thumbs[LEFT.index].value < thumbs[RIGHT.index].value - 1) {
                    frameRetreat.background = resources.getDrawable(R.drawable.ic_button_desel, null)
                    frameRetreat.isClickable = true
                } else {
                    frameRetreat.background = resources.getDrawable(R.drawable.ic_button_sel, null)
                    frameRetreat.isClickable = false
                }
            }
        }
    }

    /**
     * Color of the shadow.
     */
    @ColorInt
    open fun initShadowColor(): Int = 0xff2b2b2b.toInt()

    /**
     * Thumb width.
     */
    open fun initThumbWidth(context: Context): Int = float2DP(12f).toInt().coerceAtLeast(1)

    /**
     * Initialize maxWidth.
     */
    fun initMaxWidth() {
        onSeekStop(this, LEFT.index, thumbs[LEFT.index].value)
        onSeekStop(this, RIGHT.index, thumbs[RIGHT.index].value)
    }

    /**
     * [View.onMeasure]
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = measuredWidth
        for ((index, thumb) in thumbs.withIndex())
            thumb.pos = ((viewWidth - thumbWidth) * index).toFloat()

        // Fire listener callback
        onCreate(this, currentThumb, getThumbValue(currentThumb))
    }

    /**
     * [View.onDraw]
     * Draws the view.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //left
        canvas.drawRect(
                0f,
                0f,
                0f + thumbWidth,
                height.toFloat(),
                shadowPaint
        )
        //right
        canvas.drawRect(
                viewWidth.toFloat(),
                0f,
                (viewWidth - thumbWidth).toFloat(),
                height.toFloat(),
                shadowPaint
        )

        val leftPosStart = thumbs[LEFT.index].pos
        val leftPosEnd = leftPosStart + thumbWidth

        val rightPosStart = thumbs[RIGHT.index].pos
        val rightPosEnd = rightPosStart + thumbWidth

        if (thumbs[LEFT.index].value == 0 && thumbs[RIGHT.index].value == (videoFrameCount - 1)) {
            strokePaint.color = shadowPaint.color
            trianglePaint.color = 0xffff3898.toInt()
        } else {
            strokePaint.color = 0xffff3898.toInt()
            trianglePaint.color = 0xff2a2a2a.toInt()
        }

        //left
        canvas.drawRect(
                leftPosStart,
                0f,
                leftPosEnd,
                height.toFloat(),
                strokePaint
        )
        //right
        canvas.drawRect(
                rightPosStart,
                0f,
                rightPosEnd,
                height.toFloat(),
                strokePaint
        )

        if (thumbs[LEFT.index].value != 0) {
            canvas.drawRect(
                    float2DP(12f),
                    0f,
                    leftPosStart,
                    height.toFloat(),
                    unTrimmedPaint
            )
        }

        if (thumbs[RIGHT.index].value != (videoFrameCount - 1)) {
            canvas.drawRect(
                    rightPosEnd,
                    0f,
                    width - float2DP(12f),
                    height.toFloat(),
                    unTrimmedPaint
            )
        }

        leftTriangle.reset()
        leftTriangle.moveTo(leftPosStart + float2DP(3f), (height / 2).toFloat())
        leftTriangle.lineTo(leftPosStart + float2DP(8f), height / 2 + float2DP(4f))
        leftTriangle.lineTo(leftPosStart + float2DP(8f), height / 2 - float2DP(4f))
        leftTriangle.close()

        rightTriangle.reset()
        rightTriangle.moveTo(rightPosEnd - float2DP(3f), (height / 2).toFloat())
        rightTriangle.lineTo(rightPosEnd - float2DP(8f), height / 2 + float2DP(4f))
        rightTriangle.lineTo(rightPosEnd - float2DP(8f), height / 2 - float2DP(4f))
        rightTriangle.close()

        canvas.drawPath(leftTriangle, trianglePaint)
        canvas.drawPath(rightTriangle, trianglePaint)
    }

    private fun pixelToScale(pixelValue: Float): Int {
        return ((pixelValue - float2DP(12f)) * videoFrameCount / (viewWidth - 2 * float2DP(12f))).toInt()
                .coerceIn(0, videoFrameCount - 1)
    }

    private fun scaleToPixel(scaleValue: Int): Float {
        return (scaleValue * (viewWidth - 2 * float2DP(12f)) / videoFrameCount) + float2DP(12f)
    }

    private fun calculateThumbValue(index: Int) {
        if (index < thumbs.size && thumbs.isNotEmpty()) {
            val th = thumbs[index]
            val pos: Float
            pos = if (index == LEFT.index)
                th.pos + thumbWidth
            else
                th.pos
            th.value = pixelToScale(pos)
            onSeek(this, index, th.value)
            setButtonVisibility()
        }
    }

    private fun calculateThumbPos(index: Int) {
        if (index < thumbs.size && thumbs.isNotEmpty()) {
            val th = thumbs[index]
            th.pos = if (index == LEFT.index)
                scaleToPixel(th.value) - thumbWidth
            else
                scaleToPixel(th.value)
            onSeek(this, index, th.value)
            setButtonVisibility()
        }
    }

    private fun getThumbValue(index: Int): Int {
        if (index == -1) return 0
        return thumbs[index].value
    }

    /**
     * Sets thumb value.
     */
    private fun setThumbValue(index: Int, value: Int) {
        thumbs[index].value = value
        calculateThumbPos(index)
        // Tell the view we want a complete redraw
        invalidate()
    }

    internal fun incrementThumbPos(index: Int, millisecond: Int) {
        setThumbValue(index, thumbs[index].value + millisecond)
    }

    internal fun setThumbPos(index: Int, pos: Float) {
        if (index == -1) return
        thumbs[index].pos = pos
        calculateThumbValue(index)
        // Tell the view we want a complete redraw
        invalidate()
    }

    internal fun getClosestThumb(xPos: Float): Int {
        if (thumbs.isEmpty())
            return -1
        var closest = -1
        var minDistanceFound = Float.MAX_VALUE
        //        Log.d("AppLog", "xPos:$xPos -> x: $x")
        for (thumb in thumbs) {
            val thumbPos = thumb.pos + thumbWidth / 2
//                    if (thumb.index == LEFT.index) thumb.pos + thumbWidth / 2 else thumb.pos + thumbWidth * 3 / 2
//            Log.d("AppLog", "thumb ${thumb.index} pos: $thumbPos")
            // Find thumb closest to x coordinate
            val xMin = thumbPos - thumbWidth / 2
            val xMax = thumbPos + thumbWidth / 2
            if (xPos in xMin..xMax) {
                val distance = (thumbPos - xPos).absoluteValue
                if (distance < minDistanceFound) {
                    closest = thumb.index
//                    Log.d("AppLog", "x: $x distance: $distance selectedThumb:$closest")
                    minDistanceFound = distance
                }
            }
        }
        return closest
    }

    /**
     * Adds [OnRangeSeekBarListener] to the view.
     */
    fun addOnRangeSeekBarListener(listener: OnRangeSeekBarListener) {
        listeners.add(listener)
    }

    private fun onCreate(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
        val position = value * videoDuration / videoFrameCount
        listeners.forEach { item -> item.onCreate(rangeSeekBarView, index, position) }
    }

    private fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
        val position = value * videoDuration / videoFrameCount
        listeners.forEach { item -> item.onSeek(rangeSeekBarView, index, position) }
    }

    internal fun onSeekStart(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
        val position = value * videoDuration / videoFrameCount
        listeners.forEach { item -> item.onSeekStart(rangeSeekBarView, index, position) }
    }

    internal fun onSeekStop(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
        val position = value * videoDuration / videoFrameCount
        listeners.forEach { item -> item.onSeekStop(rangeSeekBarView, index, position) }
    }

    internal fun onDeselect(rangeSeekBarView: RangeSeekBarView) {
        listeners.forEach { item -> item.onDeselect(rangeSeekBarView) }
    }

    /**
     * Thumb data container class.
     * @property index Index of the thumb.
     */
    data class Thumb(val index: Int = 0) {
        /**
         * Value of the thumb.
         */
        var value: Int = 0

        /**
         * Position of the thumb.
         */
        var pos: Float = 0f

        /**
         * Last coordinate X of touch.
         */
        var lastTouchX: Float = 0f
    }
}
