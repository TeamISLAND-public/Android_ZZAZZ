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
import android.media.ThumbnailUtils
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import com.teamisland.zzazz.utils.UnitConverter.float2SP
import com.teamisland.zzazz.video_trimmer_library.utils.BackgroundExecutor
import kotlinx.coroutines.internal.SynchronizedObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil

/**
 * View for showing thumbnails of video by time.
 */
open class ProjectTimeLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    private var timeInterval = 0
    private val black = Paint()

    init {
        black.color = 0xff000000.toInt()
    }

    /**
     * Uri of target video.
     */
    var videoUri: Uri? = null

    /**
     * Duration of the video in ms.
     */
    var videoLength: Int = 17860

    /**
     * Current time in ms. Note that current time will be located at the horizontal center of the view.
     */
    var currentTime: Int = 3770
        set(value) {
            if (value !in 0..videoLength)
                throw IllegalArgumentException("Current time should be between 0 and the duration of the video, inclusive.")
            field = value
            invalidate()
        }

    /**
     * Distance between start to end time marker in px.
     */
    var pixelInterval: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    fun refreshSize() {
        getBitmap(height)
        invalidate()
    }

    /**
     * Pixel per millisecond.
     */
    var pxPerMs: Float = 0f
        set(value) {
            field = value
            pixelInterval = videoLength * pxPerMs
            invalidate()
        }

    /**
     * [View.onSizeChanged]
     */
    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (w != oldW)
            getBitmap(h)
    }

    private fun updateTimeInterval() {
        if (pixelInterval == 0f) pixelInterval = float2SP(1500f, resources)
        pxPerMs = pixelInterval / videoLength

        timeInterval = (videoLength * currentTime / pixelInterval).toInt()
    }

    internal val bitmapList = ArrayList<Bitmap?>()

    private fun getBitmap(viewHeight: Int) {
        if (videoUri == null) return

        val numThumbs = ceil(pixelInterval / viewHeight).toInt()
        bitmapList.clear()
        BackgroundExecutor.cancelAll("", true)
        val task = backgroundTaskObject(numThumbs, viewHeight)
        BackgroundExecutor.execute(task)
    }

    private fun backgroundTaskObject(numThumbs: Int, viewHeight: Int): BackgroundExecutor.Task =
        object : BackgroundExecutor.Task("", 0L, "") {
            override fun execute() {
                try {
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(context, videoUri)
                    val videoLengthInUs = videoLength * 1000L
                    val interval = videoLengthInUs / numThumbs
                    for (i in 0 until numThumbs) {
                        var bitmap: Bitmap? =
                            mediaMetadataRetriever.getScaledFrameAtTime(
                                i * interval,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                                viewHeight,
                                viewHeight
                            )

                        if (bitmap != null)
                            bitmap = ThumbnailUtils.extractThumbnail(bitmap, viewHeight, viewHeight)
                        bitmapList.add(bitmap)
                        invalidate()
                    }
                    mediaMetadataRetriever.release()
                } catch (e: Throwable) {
                    Thread.getDefaultUncaughtExceptionHandler()
                        ?.uncaughtException(Thread.currentThread(), e)
                }

            }
        }

    /**
     * [View.onDraw]
     */
    override fun onDraw(canvas: Canvas) {
        updateTimeInterval()
        var x = 0
        val originLocation = -currentTime * pxPerMs + width / 2
        val thumbSize = height
        val iterator = bitmapList.iterator()
        val endPoint = pixelInterval + originLocation
        while (iterator.hasNext()) {
            val next = iterator.next()
            val xLocation = (x + originLocation)
            if (next != null && -thumbSize < xLocation && xLocation < endPoint)
                canvas.drawBitmap(next, xLocation, 0f, null)
            x += thumbSize
        }
        canvas.drawRect(
            endPoint,
            0f,
            endPoint + height,
            height.toFloat(),
            black
        )
    }
}
