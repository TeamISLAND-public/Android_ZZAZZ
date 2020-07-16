package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFprobe
import java.io.File

object FFmpegDelegate {

    fun getFrameAtMilliSeconds(
        context: Context,
        path: String,
        milliSeconds: Int,
        width: Int
    ): Bitmap? {
        val parentFolder = context.getExternalFilesDir(null) ?: return null
        parentFolder.mkdirs()
        val fileName = "trimmedVideo_${System.currentTimeMillis()}.bmp"
        val file = File(parentFolder, fileName)
        val trimmedVideoFile = file.absolutePath

        FFmpeg.execute("-ss ${milliSeconds / 1000.0} -i $path -qscale:v 31 -filter:v scale=$width:-1 -vframes 1 -pix_fmt bgr8 -y $trimmedVideoFile")

        val option = BitmapFactory.Options().apply {
            outWidth = width
            inJustDecodeBounds = false
        }
        val res = BitmapFactory.decodeFile(trimmedVideoFile, option)
        file.delete()
        return res
    }
}