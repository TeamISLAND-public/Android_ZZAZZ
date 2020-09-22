package com.teamisland.zzazz.utils.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.teamisland.zzazz.R
import com.teamisland.zzazz.ui.TrimmingActivity
import com.teamisland.zzazz.utils.inference.Person
import com.teamisland.zzazz.utils.objects.UnitConverter
import com.teamisland.zzazz.utils.view.ZoomableView
import java.util.*



class BBoxView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private val backgroundPaint = paint.apply {
        color = resources.getColor(R.color.Background, null)
    }

    init {
        paint.typeface = resources.getFont(R.font.archivo_regular)
        paint.color = 0x66ffffff
    }

    lateinit var modelOutput : ArrayList<Person>
    var frame : Int = 0


    /**
     * Draws the view.
     */
    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(10F,10F,500F,500F, paint)
        canvas.drawRect(modelOutput[frame].bBox.x.toFloat(),
            modelOutput[frame].bBox.y.toFloat(),
            modelOutput[frame].bBox.w.toFloat(),
            modelOutput[frame].bBox.h.toFloat(), backgroundPaint)
    }
}
