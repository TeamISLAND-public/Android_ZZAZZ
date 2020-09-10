package com.teamisland.zzazz.utils.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.teamisland.zzazz.utils.objects.UnitConverter.float2DP
import com.teamisland.zzazz.utils.objects.UnitConverter.px2dp
import kotlin.math.roundToInt
import kotlin.properties.Delegates

/**
 * Zoomable view abstraction.
 */
abstract class ZoomableView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * Duration of the video in ms.
     */
    var videoLength: Int by Delegates.notNull()

    /**
     * Frame Count of the video.
     */
    var frameCount: Int = 0

    /**
     * Current frame. Note that current frame will be located at the horizontal center of the view.
     * @exception IllegalArgumentException When the input is out of range : [0, [frameCount]]
     */
    var currentFrame: Int = 0
        set(value) {
            if (value !in 0 until frameCount)
                throw IllegalArgumentException("Current time should be between 0 and the duration of the video, inclusive.")
            field = value
            sync()
        }

    /**
     * Distance between start to end frame marker in px.
     */
    var pixelInterval: Float = 0f
        set(value) {
            field = value
            sync()
        }

    /**
     * DP per frame.
     */
    var dpPerFrame: Float
        get() = px2dp(pixelInterval / frameCount, resources)
        set(value) {
            pixelInterval = float2DP(value, resources) * frameCount
        }

    /**
     * Pixel per frame.
     */
    var pxPerFrame: Float
        get() = pixelInterval / frameCount
        set(value) {
            pixelInterval = value * frameCount
        }

    private fun sync() {
        updateOnSync()
        invalidate()
    }

    /**
     * Range of pixels where this view can be drawn.
     */
    private val pixelRange: ClosedFloatingPointRange<Float>
        get() {
            val start = width / 2 - currentFrame * pxPerFrame
            val end = start + pixelInterval
            return start..end
        }

    /**
     * Range of valid time.
     */
    private val frameRange: IntRange
        get() = 0 until frameCount

    /**
     * Returns desired position of the input time.
     * @param frame Time to be converted.
     * @return Float value of pixel.
     * @exception IllegalArgumentException When the input is out of range.
     */
    fun getPositionOfFrame(frame: Int): Float {
        if (frame !in frameRange) throw IllegalArgumentException("Invalid time: out of range.")
        return (width / 2f + (frame - currentFrame) * pxPerFrame).coerceIn(pixelRange)
    }

    /**
     * Returns time equivalent to the input position.
     * @param px Pixel to be converted.
     * @return Int value of corresponding millisecond.
     * @exception IllegalArgumentException When the input is out of range.
     */
    fun getFrameOfPosition(px: Float): Int {
        if (px !in pixelRange) throw IllegalArgumentException("Invalid position: out of range.")
        return (currentFrame + (px - width / 2f) / pxPerFrame).roundToInt().coerceIn(frameRange)
    }

    /**
     * When timestamp and/or zooming level change, this view syncs its graphic with them.
     * And before redrawing, this function will be executed.
     * This function is called right before [invalidate].
     */
    protected open fun updateOnSync(): Unit = Unit
}