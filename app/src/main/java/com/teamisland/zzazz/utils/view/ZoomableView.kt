package com.teamisland.zzazz.utils.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.teamisland.zzazz.utils.UnitConverter.float2DP
import com.teamisland.zzazz.utils.UnitConverter.px2dp
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
    var currentTime: Int = 0
        set(value) {
            if (value !in 0..videoLength)
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
     * DP per millisecond.
     */
    var dpPerMs: Float
        get() = px2dp(pixelInterval / videoLength, resources)
        set(value) {
            pixelInterval = float2DP(value, resources) * videoLength
        }

    /**
     * Pixel per millisecond.
     */
    var pxPerMs: Float
        get() = pixelInterval / videoLength
        set(value) {
            pixelInterval = value * videoLength
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
            val start = width / 2 - currentTime * pxPerMs
            val end = start + pixelInterval
            return start..end
        }

    /**
     * Range of valid time.
     */
    val timeRange: IntRange
        get() = 0..videoLength

    /**
     * Returns desired position of the input time.
     * @param ms Time to be converted.
     * @return Float value of pixel.
     * @exception IllegalArgumentException When the input is out of range.
     */
    fun getPositionOfTime(ms: Int): Float {
        if (ms !in timeRange) throw IllegalArgumentException("Invalid time: out of range.")
        return (width / 2f + (ms - currentTime) * pxPerMs).coerceIn(pixelRange)
    }

    /**
     * Returns time equivalent to the input position.
     * @param px Pixel to be converted.
     * @return Int value of corresponding millisecond.
     * @exception IllegalArgumentException When the input is out of range.
     */
    fun getTimeOfPosition(px: Float): Int {
        if (px !in pixelRange) throw IllegalArgumentException("Invalid position: out of range.")
        return (currentTime + (px - width / 2f) / pxPerMs).roundToInt().coerceIn(timeRange)
    }

    /**
     * When timestamp and/or zooming level change, this view syncs its graphic with them.
     * And before redrawing, this function will be executed.
     * This function is called right before [invalidate].
     */
    protected open fun updateOnSync(): Unit = Unit
}