package com.teamisland.zzazz.utils.objects

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View

internal object ViewUtils {

    internal fun getScreenShot(view: View): Bitmap {
        val screenShotBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(screenShotBitmap)
        val drawable = view.background
        if (drawable != null) drawable.draw(canvas)
        else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return screenShotBitmap
    }
}