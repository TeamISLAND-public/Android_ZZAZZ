package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.GetVideoData
import kotlinx.android.synthetic.main.activity_intro.*
import java.io.File

/**
 * Main activity of Intro Activity
 */
class IntroActivity : AppCompatActivity() {

    companion object {
        /**
         * Uri of the video retrieved.
         */
        const val VIDEO_URI: String = "origin_video"

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
            Intent(MediaStore.ACTION_VIDEO_CAPTURE).also {
                startActivityForResult(it, requestCode)
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
        window.navigationBarColor = getColor(R.color.Background)

        val shrink = AnimationUtils.loadAnimation(this, R.anim.shrink)
        zzazz.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    take.visibility = View.INVISIBLE
                    v.startAnimation(shrink)
                }
                MotionEvent.ACTION_UP -> {
                    getVideo(LOAD_VIDEO)
                    v.clearAnimation()
                    take.visibility = View.VISIBLE
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

        if (videoUri != null) {
            Intent(this, TrimmingActivity::class.java).also {
                it.putExtra(VIDEO_URI, videoUri)
                startActivity(it)
            }
        }
    }
}
