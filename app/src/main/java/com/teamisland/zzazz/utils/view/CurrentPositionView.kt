package com.teamisland.zzazz.utils.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style.STROKE
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.ITrimmingData
import com.teamisland.zzazz.utils.UnitConverter.float2DP

/**
 * Ranged seekbar with current position bar.
 */
@Suppress("LeakingThis")
open class CurrentPositionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * Current position marker.
     */
    val markerPaint: Paint by lazy {
        val dp2 = float2DP(2f, resources)
        val paint = Paint()
        paint.color = resources.getColor(R.color.White, null)
        paint.style = STROKE
        paint.strokeWidth = dp2
        paint.strokeCap = Paint.Cap.SQUARE
        paint.setShadowLayer(dp2, dp2, 0f, 0x60000000)
        paint
    }

    // Current position marker when trimming.
    private val trimPaint by lazy {
        val paint = Paint()
        paint.color = resources.getColor(R.color.White, null)
        paint.style = STROKE
        paint.strokeWidth = float2DP(1f, resources)
        paint.strokeCap = Paint.Cap.SQUARE
        paint
    }

    private val layout by lazy {
        val linearLayout = LinearLayout(context)
        linearLayout.addView(textView)
        linearLayout
    }

    internal val textView by lazy {
        val textViewT = TextView(context)
        textViewT.fontFeatureSettings = "@font/archivo_regular"
        textViewT.textSize = 10f
        textViewT.visibility = GONE
        textViewT.setTextColor(0xccffffff.toInt())
        textViewT
    }

    /**
     * Trimming data the view is bound to.
     */
    internal lateinit var bindData: ITrimmingData

    private var showTrimPaint = false
    private var showMarkerPaint = true

    private fun getText(): String {
        if (!isEligible)
            return "Size too long."
        val pos = bindData.currentVideoPosition
        val totalSecond = pos / 1000
        val millisecond = (pos / 10) % 100
        val second = totalSecond % 60
        val minute = totalSecond / 60 % 60
        return java.util.Formatter()
            .format("%02d:%02d.%02d", minute.toInt(), second.toInt(), millisecond.toInt())
            .toString()
    }

    private fun getMarkerPosition(): Float {
        val dp16 = float2DP(16f, resources)
        return ((width - 2 * dp16) * bindData.currentVideoPosition / bindData.duration) + dp16
    }

    /**
     * Touch event handler
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    /**
     * Make trim current visible.
     */
    fun visibleTrimCurrent() {
        showTrimPaint = true
        showMarkerPaint = false
        textView.visibility = VISIBLE
        invalidate()
    }

    /**
     * Make marker current visible.
     */
    fun visibleMarkerCurrent() {
        showTrimPaint = false
        showMarkerPaint = true
        textView.visibility = GONE
        invalidate()
    }

    /**
     * If the trimming range is eligible.
     */
    var isEligible: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Draws the view.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val markerPosition = getMarkerPosition()

        if (showMarkerPaint)
            canvas.drawLine(
                markerPosition,
                float2DP(28f, resources),
                markerPosition,
                height.toFloat(),
                markerPaint
            )

        if (showTrimPaint)
            canvas.drawLine(
                markerPosition,
                float2DP(16f, resources),
                markerPosition,
                float2DP(32f, resources),
                trimPaint
            )

        layout.measure(width, height)
        layout.layout(0, 0, width, height)
        textView.text = getText()
        canvas.save()
        // To place the text view somewhere specific:
        canvas.translate(markerPosition - textView.width / 2, 0f)
        layout.draw(canvas)
        canvas.restore()
    }
}
