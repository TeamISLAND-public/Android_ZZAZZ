package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style.STROKE
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.teamisland.zzazz.utils.UnitConverter.float2DP
import com.teamisland.zzazz.video_trimmer_library.view.RangeSeekBarView
import kotlin.math.abs


/**
 * Ranged seekbar with current position bar.
 */
@Suppress("LeakingThis")
open class CurrentPositionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    private val markerPaint = Paint()
    private var markerPos = 0.0
    private val layout = LinearLayout(context)
    private val textView = TextView(context)
    private lateinit var listener: IPositionChangeListener
    private var videoDuration: Int = 0
    private var lastX = 0f
    private var lastPos = 0.0
    private lateinit var range: RangeSeekBarView

    private fun getText(): String {
        val pos = videoDuration * markerPos / 100
        val totalSecond = pos / 1000
        val second = totalSecond % 60
        val minute = totalSecond / 60 % 60
        return java.util.Formatter().format("%02d:%02d", minute.toInt(), second.toInt()).toString()
    }

    fun setRange(range: RangeSeekBarView) {
        this.range = range
    }

    /**
     * Adds listener to the view.
     */
    fun setListener(listener: IPositionChangeListener) {
        this.listener = listener
    }

    /**
     * Sets duration of the video.
     */
    fun setDuration(duration: Int) {
        videoDuration = duration
    }

    private fun getPointInViewWidth(): Float {
        val start = float2DP(20f, resources)
        val end = width - float2DP(20f, resources)
        return (((100.0 - markerPos) * start + markerPos * end) / 100.0).toFloat()
    }

    init {
        markerPaint.color = 0xffffffff.toInt()
        markerPaint.style = STROKE
        markerPaint.strokeWidth = float2DP(5f, resources)
        markerPaint.strokeCap = Paint.Cap.ROUND

        textView.fontFeatureSettings = "@font/archivo"
        textView.textSize = 10F
        textView.visibility = GONE
        textView.setTextColor(0xffffffff.toInt())

        layout.addView(textView)
    }

    /**
     * Sets the position of the marker.
     * @param pos Position of the marker in percentage (0 ~ 100).
     */
    fun setMarkerPos(pos: Double) {
        markerPos = pos
        invalidate()
    }

    /**
     * Draws the view.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        println(getPointInViewWidth())

        canvas.drawLine(
            getPointInViewWidth(),
            height - float2DP(47.5f, resources),
            getPointInViewWidth(),
            height.toFloat() - float2DP(2.5f, resources),
            markerPaint
        )

        layout.measure(width, height)
        layout.layout(0, 0, width, height)
        textView.text = getText()
        // To place the text view somewhere specific:
        canvas.translate(getPointInViewWidth() - textView.width / 2, 0f)
        layout.draw(canvas)
    }

    private fun isClicked(pos: Float): Boolean {
        return abs(getPointInViewWidth() - pos) < float2DP(5f, resources)
    }

    /**
     * Touch event handler
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val coordinate = event.x
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!isClicked(coordinate)) {
                    return false
                }
                lastX = coordinate
                lastPos = markerPos
                textView.visibility = VISIBLE
                return true
            }
            MotionEvent.ACTION_UP -> {
                textView.visibility = GONE
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // Calculate the distance moved
                val dx = coordinate - lastX
                markerPos = lastPos + dx.toDouble() * 100 / (width - 2 * float2DP(20f, resources))
                if (markerPos < range.getStart() * 100.0 / videoDuration)
                    markerPos = range.getStart() * 100.0 / videoDuration
                if (markerPos > range.getEnd() * 100.0 / videoDuration)
                    markerPos = range.getEnd() * 100.0 / videoDuration
                if (markerPos < 0.0) markerPos = 0.0
                if (markerPos > 100.0) markerPos = 100.0
                listener.onChange(markerPos)
                // Invalidate to request a redraw
                invalidate()
                return true
            }
        }
        return false
    }
}
