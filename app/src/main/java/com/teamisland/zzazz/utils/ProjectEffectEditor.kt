package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.UnitConverter.float2DP

/**
 * Effect range & editor.
 */
class ProjectEffectEditor @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ZoomableView(context, attrs, defStyleAttr) {

    private val timelineHeight = float2DP(40f, resources)

    private val knobPaint = Paint().apply {
        color = resources.getColor(R.color.PointColor, null)
        strokeWidth = float2DP(1f, resources)
    }

    private val rangePaint = Paint().apply {
        color = resources.getColor(R.color.PointColor, null)
        alpha = 0xb3
        style = Paint.Style.FILL
    }

    private val leftKnobTriangle by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_project_effect_left_triangle)!!.apply {
            val halfHeight = intrinsicHeight / 2
            // Anchor at end-center.
            setBounds(-intrinsicWidth, -halfHeight, 0, halfHeight)
        }
    }

    private val rightKnobTriangle by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_project_effect_right_triangle)!!.apply {
            val halfHeight = intrinsicHeight / 2
            // Anchor at start-center.
            setBounds(0, -halfHeight, intrinsicWidth, halfHeight)
        }
    }

    private var leftKnobPosition: Float = 0.0f
    private var rightKnobPosition: Float = 0.0f

    private var leftKnobTime: Int
        get() {
            return getTimeOfPosition(leftKnobPosition)
        }
        set(value) {
            leftKnobPosition = getPositionOfTime(value)
            invalidate()
        }

    private var rightKnobTime: Int
        get() {
            return getTimeOfPosition(rightKnobPosition)
        }
        set(value) {
            rightKnobPosition = getPositionOfTime(value)
            invalidate()
        }


    /**
     * Knob drawer.
     */
    override fun onDraw(canvas: Canvas) {
        val heightF = height.toFloat()

//        canvas.drawRect(rect, rangePaint)

        canvas.drawLine(leftKnobPosition, 0f, leftKnobPosition, heightF, knobPaint)
        canvas.drawLine(rightKnobPosition, 0f, rightKnobPosition, heightF, knobPaint)

        canvas.save()
        canvas.translate(leftKnobPosition, timelineHeight + (height - timelineHeight) / 2f)
        leftKnobTriangle.draw(canvas)
        canvas.translate(rightKnobPosition - leftKnobPosition, 0f)
        rightKnobTriangle.draw(canvas)
        canvas.restore()
    }
}