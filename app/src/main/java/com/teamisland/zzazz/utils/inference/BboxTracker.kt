package com.teamisland.zzazz.utils.inference

import android.graphics.Bitmap
import android.util.Log

/**
 * JsonConverter
 */
object BBoxTracker {
    val heatmapsize = 32
    val inputsize = 256
    val widthBuffer = 0.4
    val heightBuffer = 0.2
    val scale = inputsize/ heatmapsize

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

        Log.d("check0", "%d %d %d %d".format(xHat, yHat, wHat, hHat))
        Log.d("check1", "%d %d %d %d".format(xBox, yBox, wBox, hBox))
//        Log.d("check2", "%d %d %d %d".format(xCandidate, yCandidate, wCandidate, hCandidate))
        Log.d("check3", "%d %d %d %d".format(xBufferedCandidate, yBufferedCandidate, wBufferedCandidate, hBufferedCandidate))

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
}

object HumanDetector{
    // TODO: 9/9/2020
}