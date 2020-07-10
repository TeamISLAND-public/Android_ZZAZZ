package com.teamisland.zzazz.utils

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri

/**
 * Video data query.
 */
object GetVideoData {
    /**
     * Returns target video fps.
     */
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
        }
        mediaExtractor.release()
        return videoFps
    }

    fun getDuration(context: Context, uri: Uri): Int {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, uri)
        val res =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                .toInt()
        mediaMetadataRetriever.release()
        return res
    }
}