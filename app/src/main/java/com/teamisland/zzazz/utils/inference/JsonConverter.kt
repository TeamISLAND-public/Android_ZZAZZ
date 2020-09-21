package com.teamisland.zzazz.utils.inference

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.util.*

/**
 * JsonConverter
 */
internal object JsonConverter {
    fun convert(personList: ArrayList<Person>, frameCount: Int, context: Context) {
        if (frameCount != personList.size + 1) {
            Log.d(
                "framecount", "framecount does not match %d %d".format(
                    frameCount,
                    personList.size
                )
            )

            val fileName = "inference.txt"
            val file = File(context.filesDir.absolutePath, fileName)

            if (file.exists())
                file.delete()

            val writer = FileWriter(file, true)
            writer.write("frame_count : $frameCount\n")

            for (i in 0 until personList.size) {
                val frameOutput = JSONObject()
                val keyPointArray = JSONArray()
                for (j in personList[0].keyPoints.indices) {
                    val keyPoints = JSONObject()
                    try {
                        keyPoints.put("x", personList[i].keyPoints[j].position.x)
                        keyPoints.put("y", personList[i].keyPoints[j].position.y)
                        keyPoints.put("z", personList[i].keyPoints[j].position.z)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    keyPointArray.put(keyPoints)
                }
                frameOutput.put("frameNumber", i)
                frameOutput.put("keypoints", keyPointArray)
                writer.write(frameOutput.toString() + "\n")
            }
            writer.close()
        }
    }
}
