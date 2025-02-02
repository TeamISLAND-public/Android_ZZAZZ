/*
 * MIT License
 *
 * Copyright (c) 2016 Knowledge, education for life.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.teamisland.zzazz.utils.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.media.ThumbnailUtils.extractThumbnail
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.teamisland.zzazz.R
import com.teamisland.zzazz.video_trimmer_library.utils.BackgroundExecutor

/**
 * View for showing thumbnails of video by time.
 */
open class ProjectTimeLineView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ZoomableView(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint().apply {
        color = resources.getColor(R.color.Background, null)
    }

    /**
     * Uri of target video.
     */
    lateinit var path: String

    /**
     * Get bitmap for every width & height change.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        getBitmap()
    }

    internal val bitmapList = ArrayList<Bitmap?>()

    private fun getBitmap() {
        val viewHeight = height
        bitmapList.clear()

        BackgroundExecutor.cancelAll("", true)
        BackgroundExecutor.execute(object : BackgroundExecutor.Task("", 0L, "") {
            override fun execute() {
                try {
                    for (i in 1..frameCount) {
                        val s = path + "/img%08d.png".format(i)
                        Log.d("image path", s)
                        var bitmap: Bitmap? =
                            BitmapFactory.decodeFile(s)
                        if (bitmap != null)
                            bitmap = extractThumbnail(bitmap, viewHeight, viewHeight)
                        bitmapList.add(bitmap)
                        invalidate()
                    }
                } catch (e: Throwable) {
                    Thread.getDefaultUncaughtExceptionHandler()
                        ?.uncaughtException(Thread.currentThread(), e)
                }
            }
        })
    }

    /**
     * [View.onDraw]
     */
    override fun onDraw(canvas: Canvas) {
        val originLocation = getPositionOfFrame(0)

        var x = if (originLocation > 0) originLocation else originLocation % height

        val endPoint = pixelInterval + originLocation

        while (x < width && x < endPoint) {
            try {
                val index = ((x - originLocation) * bitmapList.size / pixelInterval).toInt()
                    .coerceAtMost(bitmapList.size - 1)
                bitmapList[index]?.let { canvas.drawBitmap(it, x, 0f, null) }
            } catch (e: Throwable) {
                Log.e(
                    "ProjectTimeLineView",
                    "Drawing bitmap failed. Position $x error $e"
                )
            }
            x += height
        }

        canvas.drawRect(endPoint, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
    }
}
