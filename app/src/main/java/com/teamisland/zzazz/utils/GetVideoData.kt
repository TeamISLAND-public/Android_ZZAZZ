package com.teamisland.zzazz.utils

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT
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

    /**
     * Returns the number of frames in target video.
     */
    fun getFrameCount(context: Context, uri: Uri): Int {
        val mediaMediaExtractor = MediaMetadataRetriever()
        mediaMediaExtractor.setDataSource(context, uri)
        val res = mediaMediaExtractor.extractMetadata(METADATA_KEY_VIDEO_FRAME_COUNT).toInt()
        mediaMediaExtractor.release()
        return res
    }

    /**
     * Returns target video duration in ms.
     */
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