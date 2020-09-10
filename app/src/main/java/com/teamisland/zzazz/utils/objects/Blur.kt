package com.teamisland.zzazz.utils.objects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur


internal object Blur {
    internal fun getBlur(context: Context, _bitmap: Bitmap, radius: Float): Bitmap {
        val bitmap: Bitmap = _bitmap.copy(_bitmap.config, true)

        val renderScript = RenderScript.create(context)
        val input: Allocation = Allocation.createFromBitmap(
            renderScript, bitmap, Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
        )
        val output: Allocation = Allocation.createTyped(renderScript, input.type)
        val script = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        script.setRadius(radius)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(bitmap)

        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = 0xCC070707.toInt()
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)

        return bitmap
    }
}