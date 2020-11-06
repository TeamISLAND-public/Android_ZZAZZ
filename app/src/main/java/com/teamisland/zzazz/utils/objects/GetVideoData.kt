package com.teamisland.zzazz.utils.objects

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT
import android.net.Uri
import com.teamisland.zzazz.utils.objects.FFmpegDelegate.printFrameCount

/**
 * Video data query.
 */
@Suppress("unused")
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
            if (mime.startsWith("video/") && format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                videoFps = format.getInteger(MediaFormat.KEY_FRAME_RATE)
            }
        }
        mediaExtractor.release()
        return videoFps
    }

    /**
     * Returns the number of frames in target video.
     */
    fun getFrameCount(context: Context, uri: Uri): Long {
        val mediaMediaExtractor = MediaMetadataRetriever()
        mediaMediaExtractor.setDataSource(context, uri)
        return try {
            mediaMediaExtractor.extractMetadata(METADATA_KEY_VIDEO_FRAME_COUNT)!!.toLong()
        } catch (e: NullPointerException) {
            val path = AbsolutePathRetriever.getPathAnyway(context, uri)
            printFrameCount(path)
            1L
        } finally {
            mediaMediaExtractor.release()
        }
    }

    /**
     * Returns target video duration in ms.
     */
    fun getDuration(context: Context, uri: Uri): Int {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, uri)
        val res =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                .toInt()
        mediaMetadataRetriever.release()
        return res
    }
}