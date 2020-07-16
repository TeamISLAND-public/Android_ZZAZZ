package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import com.teamisland.zzazz.R

/**
 * Video with bitmap
 */
class BitmapVideo(
    context: Context,
    private val fps: Long,
    private val video: List<Bitmap>,
    private val imageView: ImageView,
    private val playButton: ImageButton
) {

    /**
     * Is video is playing
     */
    var isPlaying: Boolean = true

    /**
     * Check the video end
     */
    private var end: Boolean = false
    private var export = false
    private var frame: Int = 0

    private val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)

    init {
        fadeOut.startOffset = 1000
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                playButton.visibility = View.VISIBLE
            }

            // button needs to be vanished
            override fun onAnimationEnd(animation: Animation?) {
                playButton.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
                playButton.visibility = View.VISIBLE
            }
        })
        fadeOut.duration = 500

        imageView.setOnClickListener {
            playButton.startAnimation(fadeOut)
        }

        Thread {
            while (!export) {
                while (isPlaying) {
                    imageView.post { imageView.setImageBitmap(video[frame++]) }
                    Thread.sleep(1000 / fps)
                    if (frame == video.size) {
                        end = true
                        changeActivated(false)
                    }
                }
            }
        }.start()
    }

    /**
     * Seek to [frame]
     * @param frame
     */
    fun seekTo(frame: Int) {
        this.frame = frame
        imageView.setImageBitmap(video[frame])
    }

    /**
     * Start the video
     */
    fun start() {
        if (end) {
            seekTo(0)
            end = false
        }
        changeActivated(true)
    }

    /**
     * Pause the video
     */
    fun pause() {
        changeActivated(false)
    }

    private fun changeActivated(state: Boolean) {
        isPlaying = state
        playButton.isActivated = state
        playButton.startAnimation(fadeOut)
    }

    /**
     * Finish video playing
     */
    fun done() {
        export = true
    }
}