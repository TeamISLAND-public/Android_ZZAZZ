package com.teamisland.zzazz.utils.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.objects.UnitConverter.float2DP
import kotlin.math.abs
import kotlin.math.min

/**
 * Effect range & editor.
 */
class ProjectEffectEditorView @JvmOverloads constructor(
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

    private var leftKnobPosition: Float = 100.0f
    private var rightKnobPosition: Float = 1000.0f

    /**
     * Time in ms where left knob is pointing.
     */
    @Suppress("unused")
    var leftKnobTime: Int
        get() = getFrameOfPosition(leftKnobPosition)
        set(value) {
            leftKnobPosition = getPositionOfFrame(value)
            invalidate()
        }

    /**
     * Time in ms where right knob is pointing.
     */
    @Suppress("unused")
    var rightKnobTime: Int
        get() = getFrameOfPosition(rightKnobPosition)
        set(value) {
            rightKnobPosition = getPositionOfFrame(value)
            invalidate()
        }

    /**
     * Knob drawer.
     */
    override fun onDraw(canvas: Canvas) {
        val heightF = height.toFloat()

        canvas.drawRect(
            leftKnobPosition,
            timelineHeight,
            rightKnobPosition,
            timelineHeight + 10f,
            rangePaint
        )

        canvas.drawLine(leftKnobPosition, 0f, leftKnobPosition, heightF, knobPaint)
        canvas.drawLine(rightKnobPosition, 0f, rightKnobPosition, heightF, knobPaint)

        canvas.save()
        canvas.translate(leftKnobPosition, (height + timelineHeight) / 2f)
        leftKnobTriangle.draw(canvas)
        canvas.translate(rightKnobPosition - leftKnobPosition, 0f)
        rightKnobTriangle.draw(canvas)
        canvas.restore()
    }

    private val threshold = float2DP(20f, resources)

    private fun getNearBy(x: Float): Int {
        val d1 = abs(leftKnobPosition - x)
        val d2 = abs(rightKnobPosition - x)
        if (min(d1, d2) > threshold) return -1
        return if (d1 > d2) 1 else 0
    }

    private var sel = -1
    private var dx = 0f
    private var knobPos: Float
        get() {
            return if (sel == 1) rightKnobPosition else leftKnobPosition
        }
        set(value) {
            if (sel == 1) rightKnobPosition = value.coerceAtLeast(leftKnobPosition)
            else leftKnobPosition = value.coerceAtMost(rightKnobPosition)
        }

    /**
     * Knob edit listener
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val target = getNearBy(event.x)
                if (target == -1) return false
                sel = target
                dx = knobPos - event.x
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                knobPos = (event.x + dx).coerceIn(x, width + x)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                sel = -1
                return true
            }
        }
        return false
    }
}