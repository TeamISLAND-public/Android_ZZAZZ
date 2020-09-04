package com.teamisland.zzazz.utils.objects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.File

/**
 * Capsulizes FFmpeg jobs.
 */
object FFmpegDelegate {

    /**
     * Gets bitmap of frame at specified millisecond.
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

        FFmpeg.execute("-ss ${milliSeconds / 1000.0} -i $path -filter:v scale=$width:-1 -vframes 1 -y $trimmedVideoFile")
        val option = BitmapFactory.Options().apply {
            outWidth = width
            inJustDecodeBounds = false
        }
        val res = BitmapFactory.decodeFile(trimmedVideoFile, option)
        file.delete()
        return res
    }

    /**
     * Extracts all frames in the video.
     * @param outPath Should be .png & contain %08d.
     */
    fun extractFrames(
        start: Double,
        end: Double,
        inPath: String,
        outPath: String,
        callback: (Int) -> Unit
    ) {
        FFmpeg.execute("-ss $start -i $inPath -t ${end - start} $outPath")
        callback(Config.getLastReturnCode())
    }

    /**
     * Extracts audio from given time range.
     */
    fun extractAudio(start: Double, end: Double, inPath: String, context: Context) {
        val absolutePath = context.filesDir.absolutePath
        FFmpeg.execute("-ss $start -i $inPath -t ${end - start} $absolutePath/audio.mp3")
    }
}