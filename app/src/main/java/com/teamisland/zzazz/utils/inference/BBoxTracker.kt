package com.teamisland.zzazz.utils.inference

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.util.ArrayList

/**
 * BBoxTracker
 */
object BBoxTracker {
    val heatmapsize = 32
    val inputsize = 256
    val widthBuffer = 0.4
    val heightBuffer = 0.2
    val scale = inputsize / heatmapsize
    var currentBox = BBox(0, 0, 0, 0)

    fun convert(bitmap: Bitmap,  person: Person, prevBox: BBox): BBox {
        val xHat = person.bBox.x
        val yHat = person.bBox.y
        val wHat = person.bBox.w
        val hHat = person.bBox.h

        val xBox = prevBox.x
        val yBox = prevBox.y
        val wBox = prevBox.w
        val hBox = prevBox.h

        val xCandidate = (wBox.toDouble() / inputsize.toDouble()) * scale * xHat + xBox
        val yCandidate = (hBox.toDouble() / inputsize.toDouble()) * scale * yHat + yBox
        val wCandidate = (wBox.toDouble() / inputsize.toDouble()) * scale * wHat
        val hCandidate = (hBox.toDouble() / inputsize.toDouble()) * scale * hHat

        val xBufferedCandidate = (xCandidate - widthBuffer * wCandidate).toInt()
        val yBufferedCandidate = (yCandidate - heightBuffer * hCandidate).toInt()
        val wBufferedCandidate = (wCandidate * (1 + 2 * widthBuffer)).toInt()
        val hBufferedCandidate = (yCandidate * (1 + 2 * heightBuffer)).toInt()

        return candidate2Box(bitmap, xBufferedCandidate, yBufferedCandidate, wBufferedCandidate, hBufferedCandidate)
    }

    fun candidate2Box(bitmap: Bitmap,
                      xBufferedCandidate : Int,
                      yBufferedCandidate : Int,
                      wBufferedCandidate : Int,
                      hBufferedCandidate : Int): BBox {
        val imageWidth = bitmap.width
        val imageHeight = bitmap.height

        var x : Int = xBufferedCandidate
        var y : Int = yBufferedCandidate
        var w : Int = wBufferedCandidate
        var h : Int = hBufferedCandidate

        if (xBufferedCandidate < 0){
            x = 0
        }
        if (yBufferedCandidate < 0){
            y = 0
        }
        if (x + wBufferedCandidate > imageWidth){
            w = imageWidth - x
        }
        if (y + hBufferedCandidate > imageHeight){
            h = imageHeight - y
        }
        return BBox(x, y, w, h)
    }

    fun track(path: String, i: Int , personList: ArrayList<Person>): Bitmap? {
        val bitmap: Bitmap? = BitmapFactory.decodeFile(path + "/img%08d.png".format(i))
        if (bitmap == null) {
            return bitmap
        }
        if (bitmap != null) {
            if (i < 3) {
                // TODO: 9/9/2020 human detector
                currentBox = BBox(0, 0, bitmap.width, bitmap.height)
                val croppedBitmap: Bitmap = Bitmap.createBitmap(
                    bitmap,
                    currentBox.x,
                    currentBox.y,
                    currentBox.w,
                    currentBox.h
                )
                var resized = Bitmap.createScaledBitmap(croppedBitmap, inputsize, inputsize, true)
                return resized

            }
            if (i >= 3) {
                currentBox = convert(bitmap, personList[i - 2], currentBox)
                val croppedBitmap: Bitmap = Bitmap.createBitmap(
                    bitmap,
                    currentBox.x,
                    currentBox.y,
                    currentBox.w,
                    currentBox.h
                )
                var resized = Bitmap.createScaledBitmap(croppedBitmap, inputsize, inputsize, true)
                return resized
            }
        }
        return null
    }
}

object HumanDetector{
    // TODO: 9/9/2020
}