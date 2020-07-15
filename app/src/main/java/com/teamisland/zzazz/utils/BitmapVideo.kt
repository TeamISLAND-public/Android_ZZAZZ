package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import com.teamisland.zzazz.R

class BitmapVideo(
    context: Context,
    private val fps: Long,
    private val video: ArrayList<Bitmap>,
    private val imageView: ImageView,
    private val playButton: ImageButton
) {

    var isPlaying = true
    var end = false
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
        changeActivated(true)
    }

    fun pause() {
        changeActivated(false)
    }

    private fun changeActivated(state: Boolean) {
        isPlaying = state
        playButton.isActivated = state
        playButton.startAnimation(fadeOut)
    }

    fun done() {
        export = true
    }
}