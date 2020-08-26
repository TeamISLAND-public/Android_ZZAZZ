package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Capsulizes FFmpeg jobs.
 */
object FFmpegDelegate {

    /**
     * Gets bitmap of frame at the specified milliseconds.
     */
    fun getFrameAtMilliSeconds(
        context: Context,
        path: String,
        milliSeconds: Int,
        width: Int
    ): Bitmap? {
        val parentFolder = context.getExternalFilesDir(null) ?: return null
        parentFolder.mkdirs()
        val fileName = "trimmedVideo_$milliSeconds.bmp"
        val file = File(parentFolder, fileName)
        val trimmedVideoFile = file.absolutePath

        FFmpeg.execute("-ss ${milliSeconds / 1000.0} -i $path -filter:v scale=${width}:-1 -vframes 1 -y $trimmedVideoFile")
        val option = BitmapFactory.Options().apply {
            outWidth = width
            inJustDecodeBounds = false
        }
        val res = BitmapFactory.decodeFile(trimmedVideoFile, option)
        file.delete()
        return res
    }

    /**
     * Trims video. Uses output seek for accuracy, so it can be little slow.
     */
    fun trimVideo(
        inPath: String,
        startMs: Long,
        endMs: Long,
        outPath: String,
        callback: (Int) -> Unit
    ) {
        val start = startMs / 1000.0
        val end = endMs / 1000.0
        FFmpeg.execute("-i $inPath -ss $start -t ${end - start} $outPath")
        callback(Config.getLastReturnCode())
    }
}