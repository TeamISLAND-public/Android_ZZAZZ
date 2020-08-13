package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.GetVideoData
import kotlinx.android.synthetic.main.activity_intro.*
import kotlinx.android.synthetic.main.activity_intro.linearLayout
import kotlinx.android.synthetic.main.activity_trimming.*

/**
 * Main activity of Intro Activity
 */
class IntroActivity : AppCompatActivity() {

    companion object {
        /**
         * Uri of the video retrieved.
         */
        const val VIDEO_URI: String = "pre_trim_video_uri"

        private const val LOAD_VIDEO = 1

        private const val TAKE_VIDEO = 2
    }

    @Suppress("SameParameterValue")
    private fun getVideo(requestCode: Int) {
        if (requestCode == LOAD_VIDEO) {
            Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).apply {
                type = "video/*"
                startActivityForResult(this, requestCode)
            }
        } else if (requestCode == TAKE_VIDEO) {
            Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                startActivityForResult(this, requestCode)
            }
        }
    }

    /**
     * [AppCompatActivity.onCreate]
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val shrink = AnimationUtils.loadAnimation(this, R.anim.shrink)
        shrink.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                take.visibility = View.INVISIBLE
            }

            override fun onAnimationEnd(animation: Animation?) {
                take.visibility = View.VISIBLE
            }

            override fun onAnimationStart(animation: Animation?) {
                take.visibility = View.INVISIBLE
            }
        })
        default_zzazz.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.startAnimation(shrink)
                }
                MotionEvent.ACTION_UP -> {
                    getVideo(LOAD_VIDEO)
                }
            }
            true
        }

        take.setOnClickListener { getVideo(TAKE_VIDEO) }

        val bounce = AnimationUtils.loadAnimation(this, R.anim.bounce)
        linearLayout.startAnimation(bounce)
    }

    /**
     * Retrieve uri from request. Checks whether the uri is valid under the restrictions.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        val videoUri = when (requestCode) {
            LOAD_VIDEO, TAKE_VIDEO -> (data ?: return).data ?: return
            else -> null
        }

        if (checkLimitation(videoUri)) {
            Intent(this, TrimmingActivity::class.java).also {
                it.putExtra(VIDEO_URI, videoUri)
                startActivity(it)
            }
        }
    }

    private fun checkLimitation(uri: Uri?): Boolean {
        if (uri == null) return false

        val videoFps = this.let { GetVideoData.getFPS(it, uri) }
        if (videoFps > resources.getInteger(R.integer.fps_limit)) {
            Toast.makeText(this, getString(R.string.fps_exceeded), Toast.LENGTH_LONG).show()
            return false
        }

        val videoDuration = this.let { GetVideoData.getDuration(it, uri) }
        if (videoDuration > resources.getInteger(R.integer.length_limit) * 1000) {
            Toast.makeText(this, getString(R.string.length_exceeded), Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }
}