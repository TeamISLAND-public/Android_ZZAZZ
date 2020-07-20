package com.teamisland.zzazz.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Multiple Thread Manager
 */
class VideoManager(
    private val mediaMetadataRetriever: MediaMetadataRetriever,
    private val start: Int,
    private val end: Int
) {

    private val numCore = Runtime.getRuntime().availableProcessors()
    private var frame = 0

    /**
     *  A variable to save the result of converting video to bitmap list
     */
    private var bitmapArrayListPair: ArrayList<Pair<Int, Bitmap>> = ArrayList(numCore)

    private val workQueue = LinkedBlockingDeque<Runnable>()
    private val threadPoolExecutor = ThreadPoolExecutor(
        numCore,
        end - start + 1,
        0L,
        TimeUnit.SECONDS,
        workQueue
    )

    /**
     * @return [threadPoolExecutor] is terminated
     */
    fun isTerminated(): Boolean = frame > end

    /**
     * Run every threads.
     * Divide frames to convert parallelly
     *
     * @param start for start frame
     * @param end for end frame
     */
    fun runThread(start: Int, end: Int) {
        frame = start
        while (frame <= end) {
            val videoToBitmap = VideoToBitmap(
                frame++,
                bitmapArrayListPair,
                mediaMetadataRetriever,
                threadPoolExecutor
            )
            threadPoolExecutor.queue.put(videoToBitmap)
        }

        while (!threadPoolExecutor.queue.isEmpty()) {
            if (threadPoolExecutor.activeCount < numCore) {
                threadPoolExecutor.submit(threadPoolExecutor.queue.take())
                Log.d("thread", "${threadPoolExecutor.queue.size}")
                Log.d("thread", "pool-" + threadPoolExecutor.poolSize)
            }
        }
        Log.d("thread", "thread is terminated")
    }

    /**
     * Sort result.
     *
     * @return the result of converting video to bitmap array list
     */
    fun sortArrayList(): ArrayList<Bitmap> {
        val bitmapArrayList: ArrayList<Bitmap> = ArrayList(end - start + 1)

        for (i in start..end)
            for (j in bitmapArrayListPair)
                if (j.first == i)
                    bitmapArrayList.add(j.second)

        return bitmapArrayList

    }

    /**
     * Runnable class for thread pool
     *
     * This class converts video to bitmap list.
     *
     * @param frame means what frame to convert to bitmap.
     */
    private class VideoToBitmap(
        private val frame: Int,
        private var bitmapArrayListPair: ArrayList<Pair<Int, Bitmap>>,
        private val mediaMetadataRetriever: MediaMetadataRetriever,
        private val threadPoolExecutor: ThreadPoolExecutor
    ) :
        Runnable {

        /**
         * Override run.
         */
        override fun run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
            val bitmap = mediaMetadataRetriever.getFrameAtIndex(frame)
            val pair = Pair(frame, bitmap)
            bitmapArrayListPair.add(pair)
            threadPoolExecutor.queue.remove(this)
        }
    }
}