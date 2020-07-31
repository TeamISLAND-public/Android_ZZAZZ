package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.UnitConverter.float2SP
import java.util.*
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.pow

/**
 * Time index view.
 */
class TimeIndexView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : ZoomableView(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private val textSize = float2SP(9f, resources)
    private val max = float2SP(120f, resources)
    private val downPower = 2f
    private var timeInterval: Int = 0

    init {
        paint.typeface = resources.getFont(R.font.archivo_regular)
        paint.textSize = textSize
        paint.color = 0x66ffffff
    }

    override fun updateOnSync() {
        timeInterval = downPower.pow(floor(log(max, downPower) - log(pxPerMs, downPower))).toInt()
            .coerceAtLeast(1)
    }

    private fun getTimeText(ms: Int, printDecimals: Boolean): String {
        val totalSec = ms / 1000
        val millisecond = ms % 1000
        val sec = totalSec % 60
        val min = totalSec / 60 % 60
        return if (printDecimals)
            Formatter().format("%02d:%02d.%03d", min, sec, millisecond).toString()
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
            float2SP(11f, resources).toInt() + paddingTop + paddingBottom
        )
    }

    /**
     * Draws the view.
     */
    override fun onDraw(canvas: Canvas) {
        val decimal = timeInterval < 1000
        val currentTimeStep = currentTime / timeInterval * timeInterval

        for (i in (currentTimeStep - timeInterval) downTo 0 step timeInterval) {
            val desiredXPos = getPositionOfTime(i)
            canvas.drawText(getTimeText(i, decimal), desiredXPos, textSize + paddingTop, paint)
            if (desiredXPos < -max) break
        }
        for (i in currentTimeStep..videoLength step timeInterval) {
            val desiredXPos = getPositionOfTime(i)
            canvas.drawText(getTimeText(i, decimal), desiredXPos, textSize + paddingTop, paint)
            if (desiredXPos > width + max) break
        }
    }
}
