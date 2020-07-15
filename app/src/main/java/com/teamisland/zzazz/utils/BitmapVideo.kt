package com.teamisland.zzazz.utils

import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView

class BitmapVideo(
    private val fps: Long,
    private val video: ArrayList<Bitmap>,
    private val imageView: ImageView,
    private val playButton: ImageButton
) {

    var isPlaying = true
    var end = false
    private var export = false
    private var frame: Int = 0

    init {
        Thread {
            while (!export) {
                while (isPlaying) {
                    imageView.post { imageView.setImageBitmap(video[frame++]) }
                    Thread.sleep(1000 / fps)
                    if (frame == video.size) {
                        isPlaying = false
                        end = true
                        playButton.isActivated = false
                    }
                    Log.d("frame", "$frame")
                }
            }
        }.start()
    }

    fun seekTo(frame: Int) {
        this.frame = frame
        imageView.setImageBitmap(video[frame])
    }

    fun start() {
        if (end) {
            seekTo(0)
            end = false
        }
        isPlaying = true
        playButton.isActivated = true
    }

    fun pause() {
        isPlaying = false
        playButton.isActivated = false
    }

    fun done() {
        export = true
    }
}