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
package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils.extractThumbnail
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.teamisland.zzazz.R
import com.teamisland.zzazz.video_trimmer_library.utils.BackgroundExecutor
import kotlin.math.roundToInt

/**
 * View for showing thumbnails of video by time.
 */
open class ProjectTimeLineView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ZoomableView(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint()
    internal var sampleMsQuantum = 50

    init {
        backgroundPaint.color = resources.getColor(R.color.Background, null)
    }

    /**
     * Uri of target video.
     */
    lateinit var videoUri: Uri

    /**
     * [View.onSizeChanged]
     */
    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (w != oldW)
            getBitmap(h)
    }

    override fun updateOnSync() {
        timeInterval = (videoLength * currentTime / pixelInterval).toInt()
    }

    internal val bitmapList = ArrayList<Bitmap?>()

    private var timeInterval: Int = 0

    private fun getBitmap(viewHeight: Int) {
        bitmapList.clear()
        val frameCount: Int
        try {
            frameCount = GetVideoData.getFrameCount(context, videoUri).coerceAtLeast(1)
        } catch (a: UninitializedPropertyAccessException) {
            Log.w("ProjectTimeLineView", a.message ?: "")
            return
        }
        sampleMsQuantum =
            (videoLength * 90f / frameCount).roundToInt()
                .coerceAtLeast(1)

        val numThumbs = (frameCount / 90f).roundToInt().coerceAtLeast(1)
        BackgroundExecutor.cancelAll("", true)
        BackgroundExecutor.execute(object : BackgroundExecutor.Task("", 0L, "") {
            override fun execute() {
                try {
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(context, videoUri)
                    val intervalUs = sampleMsQuantum * 1000L
                    for (i in 0 until numThumbs) {
                        var bitmap: Bitmap? =
                            mediaMetadataRetriever.getScaledFrameAtTime(
                                i * intervalUs,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                                viewHeight,
                                viewHeight
                            )
                        if (bitmap != null)
                            bitmap = extractThumbnail(bitmap, viewHeight, viewHeight)
                        bitmapList.add(bitmap)
                        invalidate()
                    }
                    mediaMetadataRetriever.release()
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
        if (bitmapList.size == 0) return
        val originLocation = getPositionOfTime(0)

        val fl = sampleMsQuantum * pxPerMs

        var x =
            if (originLocation > 0) originLocation else originLocation % height

        val endPoint = pixelInterval + originLocation

        while (x < width && x < endPoint) {
            val temp = ((x - originLocation) / fl).roundToInt().coerceIn(0, bitmapList.size - 1)
            bitmapList[temp]?.let { canvas.drawBitmap(it, x, 0f, null) }
            x += height
        }

        canvas.drawRect(
            endPoint,
            0f,
            endPoint + height,
            height.toFloat(),
            backgroundPaint
        )
    }
}