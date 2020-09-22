package com.teamisland.zzazz.utils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.objects.UnitConverter.float2DP
import com.teamisland.zzazz.utils.objects.UnitConverter.float2SP
import java.util.*
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.pow

/**
 * Time index view.
 */
class TimeIndexView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ZoomableView(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private val textSize = float2DP(8f, resources)
    private val max = float2SP(120f, resources)
    private val downPower = 2f
    private var frameInterval: Int = 0

    init {
        paint.typeface = resources.getFont(R.font.archivo_regular)
        paint.textSize = textSize
        paint.color = 0x66ffffff
    }

    override fun updateOnSync() {
        frameInterval =
            downPower.pow(
                floor(
                    log(
                        max,
                        downPower
                    ) - log(pxPerFrame * videoLength / frameCount / 1000, downPower)
                )
            ).toInt()
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
        if (frameInterval <= 0) {
            val temp = paint.color
            paint.color = 0xffffc200.toInt()
            canvas.drawText(
                "frameInterval: $frameInterval",
                width / 2f,
                textSize + paddingTop,
                paint
            )
            paint.color = temp
            return
        }
        val decimal = frameInterval < 1000
        val currentFrameStep = currentFrame - (currentFrame % frameInterval)

        for (i in (currentFrameStep - frameInterval) downTo 0 step frameInterval) {
            val desiredXPos = getPositionOfFrame(i)
            canvas.drawText(
                getTimeText(i * videoLength / frameCount, decimal),
                desiredXPos,
                textSize + paddingTop,
                paint
            )
            if (desiredXPos < -max) break
        }
        for (i in currentFrameStep until frameCount step frameInterval) {
            val desiredXPos = getPositionOfFrame(i)
            canvas.drawText(
                getTimeText(i * videoLength / frameCount, decimal),
                desiredXPos,
                textSize + paddingTop,
                paint
            )
            if (desiredXPos > width + max) break
        }
    }
}
