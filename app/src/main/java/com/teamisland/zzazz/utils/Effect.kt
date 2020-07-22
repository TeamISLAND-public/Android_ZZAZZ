package com.teamisland.zzazz.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.FloatRange

/**
 * Class for each effects
 */
class Effect(
    private var startFrame: Int,
    private var endFrame: Int,
    private val type: Int,
    private var color: Int,
    bitmapArrayList: MutableList<Bitmap>,
    pointArrayList: MutableList<Point>,
    widthArrayList: MutableList<Int>,
    heightArrayList: MutableList<Int>
) {
    private var dataArrayList: MutableList<Data> = mutableListOf()
    private var visible = true

    init {
        for (frame in 0 until endFrame) {
            dataArrayList.add(
                Data(
                    bitmapArrayList[frame],
                    pointArrayList[frame],
                    widthArrayList[frame],
                    heightArrayList[frame]
                )
            )
        }
    }

    /**
     * @return [startFrame]
     */
    fun getStartFrame() = startFrame

    /**
     * @return [endFrame]
     */
    fun getEndFrame() = endFrame

    /**
     * @param color to set color
     */
    fun setColor(color: Int) {
        this.color = color
    }

    /**
     * @param startFrame to set startFrame
     */
    fun setStart(startFrame: Int) {
        this.startFrame = startFrame
    }

    /**
     * @param endFrame to set endFrame
     */
    fun setEnd(endFrame: Int) {
        this.endFrame = endFrame
    }

    /**
     * Move x coordinate at frame.
     */
    fun moveX(frame: Int, dx: Int) {
        dataArrayList[frame - startFrame].moveX(dx)
    }

    /**
     * Move y coordinate at frame.
     */
    fun moveY(frame: Int, dy: Int) {
        dataArrayList[frame - startFrame].moveY(dy)
    }

    /**
     * Rotate the effect at frame.
     */
    fun rotate(frame: Int, @FloatRange(from = 0.0, to = 360.0) degree: Float) {
        dataArrayList[frame - startFrame].rotate(degree)
    }

    /**
     * Change width of effect at frame.
     */
    fun changeWidth(frame: Int, dw: Int) {
        dataArrayList[frame - startFrame].changeWidth(dw)
    }

    /**
     * Change height of effect at frame.
     */
    fun changeHeight(frame: Int, dh: Int) {
        dataArrayList[frame - startFrame].changeHeight(dh)
    }

    /**
     * Draw effect with canvas.
     */
    fun draw(frame: Int, canvas: Canvas) {
        if (visible) {
            TODO("have to implement")
        }
    }

    /**
     * Change visibility
     */
    fun changeVisible() {
        visible = !visible
    }

    /**
     * Data of effect on each frames.
     */
    private class Data(
        private var bitmap: Bitmap,
        private var point: Point,
        private var width: Int,
        private var height: Int
    ) {

        private var degree: Float = 0F

        /**
         * Move x coordinate.
         */
        fun moveX(dx: Int) {
            point.x += dx
        }

        /**
         * Move y coordinate.
         */
        fun moveY(dy: Int) {
            point.y += dy
        }

        /**
         * Rotate effect Image.
         */
        fun rotate(@FloatRange(from = 0.0, to = 360.0) angle: Float) {
            degree += angle
            degree %= 360
        }

        /**
         * Change width of effect.
         */
        fun changeWidth(dw: Int) {
            width += dw
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
        }

        /**
         * Change height of effect.
         */
        fun changeHeight(dh: Int) {
            height += dh
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
        }

        fun draw(canvas: Canvas) {
            TODO("have to implement")
        }
    }

    /**
     * Coordinate of point
     */
    class Point(
        /**
         * x point
         */
        var x: Int,
        /**
         * y point
         */
        var y: Int
    )
}