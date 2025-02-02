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
package com.teamisland.zzazz.video_trimmer_library.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.util.AttributeSet
import android.util.Range
import android.view.MotionEvent
import android.view.View
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.interfaces.ITrimmingData
import com.teamisland.zzazz.utils.objects.AbsolutePathRetriever
import com.teamisland.zzazz.utils.objects.FFmpegDelegate
import com.teamisland.zzazz.utils.objects.UnitConverter
import com.teamisland.zzazz.utils.view.CurrentPositionView
import com.teamisland.zzazz.video_trimmer_library.utils.BackgroundExecutor
import kotlin.math.ceil

/**
 * View for showing thumbnails of video by time.
 */
open class TimeLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * ITrimmingData.
     */
    internal lateinit var bindData: ITrimmingData

    /**
     * CurrentPositionView.
     */
    lateinit var currentPositionView: CurrentPositionView

    /**
     * Uri of the video attached.
     */
    lateinit var videoUri: Uri

    internal val bitmapList = ArrayList<Bitmap?>()

    private val pointColor = resources.getColor(R.color.PointColor, null)
    private val grayColor = resources.getColor(R.color.ContentsText80, null)

    /**
     * [View.onSizeChanged]
     */
    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (w != oldW)
            getBitmap(w, h)
    }

    private fun getBitmap(viewWidth: Int, viewHeight: Int) {
        // Set thumbnail properties (Thumbs are squares)
        val numThumbs = ceil(viewWidth.toDouble() / viewHeight).toInt()
        bitmapList.clear()
        if (isInEditMode) {
            val bitmap = ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeResource(resources, android.R.drawable.sym_def_app_icon)
                    ?: return,
                viewHeight,
                viewHeight
            )
            for (i in 0 until numThumbs)
                bitmapList.add(bitmap)
            return
        }
        bitmapList.clear()
        val path = try {
            AbsolutePathRetriever.getPath(context, videoUri)
        } catch (e: Exception) {
            null
        }
        BackgroundExecutor.cancelAll("", true)
        BackgroundExecutor.execute(object : BackgroundExecutor.Task("", 0L, "") {
            override fun execute() {
                try {
                    val thumbnailList = ArrayList<Bitmap?>()

                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(context, videoUri)
                    val videoLengthInMs =
                        (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            ?: return)
                            .toLong() * 1000L
                    val interval = videoLengthInMs / numThumbs
                    for (i in 0 until numThumbs) {
                        var bitmap: Bitmap? = when (2) {
                            1 -> mediaMetadataRetriever.getScaledFrameAtTime(
                                i * interval,
                                MediaMetadataRetriever.OPTION_CLOSEST,
                                viewHeight,
                                viewHeight
                            )
                            2 -> mediaMetadataRetriever.getScaledFrameAtTime(
                                i * interval,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                                viewHeight,
                                viewHeight
                            )
                            3 -> FFmpegDelegate.getFrameAtMilliSeconds(
                                context,
                                path ?: return,
                                (i * interval).toInt() / 1000,
                                viewHeight
                            )
                            else -> null
                        }

                        if (bitmap != null)
                            bitmap = ThumbnailUtils.extractThumbnail(bitmap, viewHeight, viewHeight)

                        thumbnailList.add(bitmap)
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
        )
    }

    /**
     * [View.onDraw]
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        var x = 0
        val thumbSize = height
        for (bitmap in bitmapList) {
            if (bitmap != null)
                canvas.drawBitmap(bitmap, x.toFloat(), 0f, null)
            x += thumbSize
        }
    }

    /**
     *
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null)
            return false
        val thumbWidth = UnitConverter.float2DP(16f, resources)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPositionView.markerPaint.color = pointColor
                currentPositionView.textView.visibility = VISIBLE
                currentPositionView.invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                currentPositionView.markerPaint.color = grayColor
                currentPositionView.textView.visibility = GONE
                currentPositionView.invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val availLength = currentPositionView.width - 2 * thumbWidth
                val adjustedX = Range(0f, availLength).clamp(event.x)
                val desiredPositionFraction = adjustedX * bindData.duration / availLength
                bindData.currentVideoPosition = desiredPositionFraction.toLong()
                return true
            }
        }
        return false
    }
}
