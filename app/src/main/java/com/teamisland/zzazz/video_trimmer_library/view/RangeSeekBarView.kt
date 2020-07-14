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
import android.graphics.Paint.Style.*
import android.graphics.Path
import android.util.AttributeSet
import android.util.Range
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.annotation.ColorInt
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
) :
    View(context, attrs, defStyleAttr) {

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
    private val edgePaint = Paint()
    private val shadowPaint = Paint()
    private val strokePaint = Paint()
    private val trianglePaint = Paint()
    private val strokeBoxPaint = Paint()
    private val thumbs = arrayOf(Thumb(LEFT.index), Thumb(RIGHT.index))
    private var firstRun: Boolean = true
    private var listeners = HashSet<OnRangeSeekBarListener>()
    private var maxWidth: Float = 0.toFloat()
    private var pixelRangeMax: Float = 0.toFloat()
    private var pixelRangeMin: Float = 0.toFloat()
    private var viewWidth: Int = 0
    private lateinit var frameAdvance: Button
    private lateinit var frameRetreat: Button
    private var currentThumb: Int = -1
    private var videoDuration: Int = 0
    private var videoFpsMillisecond: Int = 0
    private val leftTriangle = Path()
    private val rightTriangle = Path()

    /**
     * Video duration in ms.
     */
    fun getDuration(): Int = videoDuration

    /**
     * Thumb width.
     */
    val thumbWidth: Int = initThumbWidth(context)

    /**
     * Get start point in ms.
     */
    fun getStart(): Int = thumbs[LEFT.index].value

    /**
     * Get endpoint in ms.
     */
    fun getEnd(): Int = thumbs[RIGHT.index].value

    /**
     * Get range selected in ms.
     */
    fun getRange() = Range(thumbs[LEFT.index].value, thumbs[RIGHT.index].value)

    private fun float2DP(float: Float): Float {
        return TypedValue.applyDimension(COMPLEX_UNIT_DIP, float, context.resources.displayMetrics)
    }

    /**
     * Sets the duration of the video.
     */
    fun setDuration(duration: Int) {
        videoDuration = duration
        thumbs[LEFT.index].value = 0
        thumbs[RIGHT.index].value = duration
    }

    /**
     * Sets the fps of the video.
     */
    fun setFPS(fps: Int) {
        videoFpsMillisecond = 1000 / fps
    }

    private fun setStrokePaint() {
        strokePaint.isAntiAlias = true
        strokePaint.style = FILL
        strokePaint.strokeWidth = float2DP(3f)
        strokePaint.color = 0xffe6e6e6.toInt()
    }

    private fun setShadowPaint() {
        shadowPaint.isAntiAlias = true
        shadowPaint.color = initShadowColor()
    }

    private fun setStrokeBoxPaint() {
        strokeBoxPaint.isAntiAlias = true
        strokeBoxPaint.style = STROKE
        strokeBoxPaint.strokeWidth = float2DP(4f)
        strokeBoxPaint.color = 0xffe6e6e6.toInt()
    }

    private fun setEdgePaint() {
        edgePaint.isAntiAlias = true
        edgePaint.color = 0xffffffff.toInt()
        edgePaint.strokeWidth = float2DP(2f)
    }

    private fun setTrianglePaint() {
        trianglePaint.color = 0xff070707.toInt()
        trianglePaint.isAntiAlias = true
        trianglePaint.style = FILL_AND_STROKE
    }

    init {
        isFocusable = true
        isFocusableInTouchMode = true

        setStrokePaint()
        setShadowPaint()
        setStrokeBoxPaint()
        setEdgePaint()
        setTrianglePaint()
    }

    /**
     * Sets frame move button.
     */
    fun setButtons(advance: Button, retreat: Button) {
        frameAdvance = advance
        frameRetreat = retreat
        setButtonVisibility()
        advance.setOnClickListener {
            incrementThumbPos(currentThumb, videoFpsMillisecond)
        }
        retreat.setOnClickListener {
            incrementThumbPos(currentThumb, -videoFpsMillisecond)
        }
    }

    private fun setButtonVisibility() {
        when (currentThumb) {
            -1 -> {
                frameAdvance.visibility = GONE
                frameRetreat.visibility = GONE
            }
            LEFT.index -> {
                frameAdvance.visibility =
                    if (thumbs[LEFT.index].value + videoFpsMillisecond < thumbs[RIGHT.index].value) VISIBLE else INVISIBLE
                frameRetreat.visibility =
                    if (thumbs[LEFT.index].value - videoFpsMillisecond >= 0) VISIBLE else INVISIBLE
            }
            RIGHT.index -> {
                frameAdvance.visibility =
                    if (thumbs[RIGHT.index].value + videoFpsMillisecond <= videoDuration) VISIBLE else INVISIBLE
                frameRetreat.visibility =
                    if (thumbs[LEFT.index].value < thumbs[RIGHT.index].value - videoFpsMillisecond) VISIBLE else INVISIBLE
            }
        }
    }

    /**
     * Color of the shadow.
     */
    @ColorInt
    open fun initShadowColor(): Int = 0xB3070707.toInt()

    /**
     * Thumb width.
     */
    open fun initThumbWidth(context: Context): Int = float2DP(20f).toInt().coerceAtLeast(1)

    /**
     * Initialize maxWidth.
     */
    fun initMaxWidth() {
        maxWidth = thumbs[RIGHT.index].pos - thumbs[LEFT.index].pos
        onSeekStop(this, LEFT.index, thumbs[LEFT.index].value)
        onSeekStop(this, RIGHT.index, thumbs[RIGHT.index].value)
    }

    /**
     * [View.onMeasure]
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = measuredWidth
        pixelRangeMin = 0f
        pixelRangeMax = (viewWidth - 2 * thumbWidth).toFloat()
        if (firstRun) {
            for ((index, thumb) in thumbs.withIndex()) {
                thumb.pos = (pixelRangeMax - thumbWidth) * index
            }
            // Fire listener callback
            onCreate(this, currentThumb, getThumbValue(currentThumb))
            firstRun = false
        }
    }

    /**
     * [View.onDraw]
     * Draws the view.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (thumbs.isEmpty())
            return
        // draw shadows outside of selected range
        for (thumb in thumbs) {
            if (thumb.index == LEFT.index) {
                val x = thumb.pos
                if (x > pixelRangeMin)
                    canvas.drawRect(
                        thumbWidth.toFloat(),
                        0f,
                        (x + thumbWidth),
                        height.toFloat(),
                        shadowPaint
                    )
            } else {
                val x = thumb.pos
                if (x < pixelRangeMax)
                    canvas.drawRect(
                        x + thumbWidth,
                        0f,
                        (viewWidth - thumbWidth).toFloat(),
                        height.toFloat(),
                        shadowPaint
                    )
            }
        }
        //draw stroke around selected range
        val leftPosStart = thumbs[LEFT.index].pos
        val leftPosEnd = leftPosStart + thumbWidth
        val rightPosStart = thumbs[RIGHT.index].pos + thumbWidth
        val rightPosEnd = thumbs[RIGHT.index].pos + thumbWidth * 2

        strokePaint.color = 0xff474747.toInt()
        strokeBoxPaint.color = 0xff474747.toInt()

        //shadowed selector
        canvas.drawRect(
            pixelRangeMin,
            0f,
            pixelRangeMax + 2 * thumbWidth,
            height.toFloat(),
            strokeBoxPaint
        )
        //left
        canvas.drawRect(
            0f,
            0f,
            thumbWidth.toFloat(),
            height.toFloat(),
            strokePaint
        )
        //right
        canvas.drawRect(
            pixelRangeMax + thumbWidth,
            0f,
            pixelRangeMax + 2 * thumbWidth,
            height.toFloat(),
            strokePaint
        )

        if (getStart() == 0 && getEnd() == videoDuration) {
            strokePaint.color = 0xff474747.toInt()
            strokeBoxPaint.color = 0xff474747.toInt()
            trianglePaint.color = 0xfffdfdfd.toInt()
        } else {
            strokePaint.color = 0xffe6e6e6.toInt()
            strokeBoxPaint.color = 0xffe6e6e6.toInt()
            trianglePaint.color = 0xff070707.toInt()
        }

        //selector
        canvas.drawRect(
            leftPosStart,
            0f,
            rightPosEnd,
            height.toFloat(),
            strokeBoxPaint
        )
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
        leftTriangle.reset()
        leftTriangle.moveTo(leftPosStart + float2DP(15f), float2DP(15f))
        leftTriangle.lineTo(leftPosStart + float2DP(15f), float2DP(25f))
        leftTriangle.lineTo(leftPosStart + float2DP(6f), float2DP(20f))
        leftTriangle.close()

        rightTriangle.reset()
        rightTriangle.moveTo(rightPosStart + float2DP(5f), float2DP(15f))
        rightTriangle.lineTo(rightPosStart + float2DP(5f), float2DP(25f))
        rightTriangle.lineTo(rightPosStart + float2DP(14f), float2DP(20f))
        rightTriangle.close()

        canvas.drawPath(leftTriangle, trianglePaint)
        canvas.drawPath(rightTriangle, trianglePaint)
    }

    /**
     * [View.onTouchEvent]
     * Moves thumb.
     */
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val mThumb: Thumb
        val mThumb2: Thumb
        val coordinate = ev.x
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                // Remember where we started
                currentThumb = getClosestThumb(coordinate)
                setButtonVisibility()
                if (currentThumb == -1) {
                    onDeselect(this)
                    return false
                }
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
                    thumbs[if (currentThumb == LEFT.index) RIGHT.index else LEFT.index]
                // Calculate the distance moved
                val dx = coordinate - mThumb.lastTouchX
                val newX = mThumb.pos + dx
                when {
                    currentThumb == 0 -> when {
                        newX >= mThumb2.pos -> mThumb.pos =
                            mThumb2.pos
                        newX <= pixelRangeMin -> mThumb.pos = pixelRangeMin
                        else -> {
                            //Check if thumb is not out of max width
//                            checkPositionThumb(mThumb, mThumb2, dx, true)
                            // Move the object
                            mThumb.pos = mThumb.pos + dx
                            // Remember this touch position for the next move event
                            mThumb.lastTouchX = coordinate
                        }
                    }
                    newX <= mThumb2.pos -> mThumb.pos = mThumb2.pos
                    newX >= pixelRangeMax -> mThumb.pos = pixelRangeMax
                    else -> {
                        //Check if thumb is not out of max width
//                        checkPositionThumb(mThumb2, mThumb, dx, false)
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

//    private fun checkPositionThumb(
//        thumbLeft: Thumb,
//        thumbRight: Thumb,
//        dx: Float,
//        isLeftMove: Boolean
//    ) {
//        if (isLeftMove && dx < 0) {
//            if (thumbRight.pos - (thumbLeft.pos + dx) > maxWidth) {
//                thumbRight.pos = thumbLeft.pos + dx + maxWidth
//                setThumbPos(RIGHT.index, thumbRight.pos)
//            }
//        } else if (!isLeftMove && dx > 0) {
//            if (thumbRight.pos + dx - thumbLeft.pos > maxWidth) {
//                thumbLeft.pos = thumbRight.pos + dx - maxWidth
//                setThumbPos(LEFT.index, thumbLeft.pos)
//            }
//        }
//    }

    private fun pixelToScale(pixelValue: Float): Int {
        return (pixelValue * videoDuration / pixelRangeMax).toInt()
    }

    private fun scaleToPixel(scaleValue: Int): Float {
        return scaleValue * pixelRangeMax / videoDuration
    }

    private fun calculateThumbValue(index: Int) {
        if (index < thumbs.size && thumbs.isNotEmpty()) {
            val th = thumbs[index]
            th.value = pixelToScale(th.pos)
            onSeek(this, index, th.value)
            setButtonVisibility()
        }
    }

    private fun calculateThumbPos(index: Int) {
        if (index < thumbs.size && thumbs.isNotEmpty()) {
            val th = thumbs[index]
            th.pos = scaleToPixel(th.value)
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

    private fun incrementThumbPos(index: Int, millisecond: Int) {
        setThumbValue(index, thumbs[index].value + millisecond)
    }

    private fun setThumbPos(index: Int, pos: Float) {
        if (index == -1) return
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
        //        Log.d("AppLog", "xPos:$xPos -> x: $x")
        for (thumb in thumbs) {
            val thumbPos =
                if (thumb.index == LEFT.index) thumb.pos + thumbWidth / 2 else thumb.pos + thumbWidth * 3 / 2
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
        listeners.forEach { item -> item.onCreate(rangeSeekBarView, index, value) }
    }

    private fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
        listeners.forEach { item -> item.onSeek(rangeSeekBarView, index, value) }
    }

    private fun onSeekStart(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
        listeners.forEach { item -> item.onSeekStart(rangeSeekBarView, index, value) }
    }

    private fun onSeekStop(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
        listeners.forEach { item -> item.onSeekStop(rangeSeekBarView, index, value) }
    }

    private fun onDeselect(rangeSeekBarView: RangeSeekBarView) {
        listeners.forEach { item -> item.onDeselect(rangeSeekBarView) }
    }

    /**
     * Thumb data container class.
     * @property index Index of the thumb.
     */
    class Thumb(val index: Int = 0) {
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
