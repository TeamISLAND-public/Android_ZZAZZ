package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.UnitConverter.float2SP
import java.util.*
import kotlin.math.*

/**
 * Time index view.
 */
class TimeIndexView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private val textSize = float2SP(9f, resources)
    private var timeInterval = 0

    init {
        paint.typeface = resources.getFont(R.font.archivo_regular)
        paint.textSize = textSize
        paint.color = 0x66ffffff
    }

    /**
     * Duration of the video in ms.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var videoLength: Int = 1786578

    /**
     * Current time in ms. Note that current time will be located at the horizontal center of the view.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var currentTime: Int = 63
        set(value) {
            if (value !in 0..videoLength) {
                throw IllegalArgumentException("Current time should be between 0 and the duration of the video, inclusive.")
            }
            field = value
            invalidate()
        }

    /**
     * Distance between start to end time marker in px.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var pixelInterval: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Pixel per millisecond.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var pxPerMs: Float = 0f
        set(value) {
            field = value
            pixelInterval = pxPerMs * videoLength
            invalidate()
        }

    private fun updateTimeInterval() {
        val pixelInterval2 = if (pixelInterval != 0f) pixelInterval else float2SP(1500f, resources)
        val max = float2SP(120f, resources)

        pxPerMs = pixelInterval2 / videoLength
        val downPower = 2f
        timeInterval = downPower.pow(floor(log(max, downPower) - log(pxPerMs, downPower))).toInt()
            .coerceAtLeast(1)
    }

    private fun obtainTimeText(ms: Int, doPrintDecimals: Boolean = false): String {
        val totalSec = ms / 1000
        val millisec = ms % 1000
        val sec = totalSec % 60
        val min = totalSec / 60 % 60
        return if (doPrintDecimals)
            Formatter().format("%02d:%02d.%03d", min, sec, millisec).toString()
        else {
            Formatter().format("%02d:%02d", min, sec).toString()
        }
    }

    /**
     * Measures the view.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            float2SP(11f, resources).toInt()
        )
    }

    /**
     * Draws the view.
     */
    override fun onDraw(canvas: Canvas) {
        updateTimeInterval()
        val originAt = -currentTime * pxPerMs + width / 2
        val startTime = max(0, currentTime - 2 * timeInterval)
        val endTime = min(videoLength, currentTime + 2 * timeInterval)
        val decimal = timeInterval < 1000
        for (i in startTime..endTime step timeInterval) {
            canvas.drawText(obtainTimeText(i, decimal), i * pxPerMs + originAt, textSize, paint)
        }
    }
}
