package com.teamisland.zzazz.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.teamisland.zzazz.utils.UnitConverter.float2DP
import com.teamisland.zzazz.utils.UnitConverter.px2dp

/**
 * Zoomable view abstraction.
 */
abstract class ZoomableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * Reserved for child use.
     */
    protected var timeInterval: Int = 0

    /**
     * Duration of the video in ms.
     */
    var videoLength: Int = 17860

    /**
     * Current time in ms. Note that current time will be located at the horizontal center of the view.
     */
    var currentTime: Int = 3770
        set(value) {
            if (value !in 0..videoLength)
                throw IllegalArgumentException("Current time should be between 0 and the duration of the video, inclusive.")
            field = value
            sync()
        }

    /**
     * Distance between start to end time marker in px.
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
        updateTimeInterval()
        invalidate()
    }

    /**
     * Updates [timeInterval].
     */
    protected abstract fun updateTimeInterval()
}