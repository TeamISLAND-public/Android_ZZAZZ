package com.teamisland.zzazz.utils

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri

object GetVideoFPS {
    fun getFPS(context: Context, uri: Uri): Int {
        val mediaExtractor = MediaExtractor()
        var videoFps = 24
        mediaExtractor.setDataSource(context, uri, null)
        for (i in 0 until mediaExtractor.trackCount) {
            val format = mediaExtractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)!!
            if (mime.startsWith("video/")) {
                if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                    videoFps = format.getInteger(MediaFormat.KEY_FRAME_RATE)
                }
            }
            mediaExtractor.release()
        }
        return videoFps
    }
}