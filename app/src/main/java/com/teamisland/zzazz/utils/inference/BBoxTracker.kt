package com.teamisland.zzazz.utils.inference

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

/**
 * JsonConverter
 */
object BBoxTracker {
    val heatmapsize = 32
    val inputsize = 256
    val scale = inputsize/ heatmapsize

    fun convert(bitmap: Bitmap,  person: Person, boxArray: IntArray) {
        val x = person.bBox[0]
        val y = person.bBox[2]
        val w = person.bBox[1] - person.bBox[0]
        val h = person.bBox[3] - person.bBox[2]
    }
}