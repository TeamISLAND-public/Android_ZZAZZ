package com.teamisland.zzazz.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.animation.AnimationUtils
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.GetVideoData
import com.teamisland.zzazz.utils.IntroAlertDialog
import kotlinx.android.synthetic.main.activity_intro.*

/**
 * Activity before video trimming.
 * Here you can choose video, or take it yourself.
 * Hands the uri of the video to next activity.
 */
class IntroActivity : AppCompatActivity() {

    private fun dispatchTakeVideoIntent() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(
                packageManager
            )?.also {
                startActivityForResult(
                    takeVideoIntent,
                    REQUEST_VIDEO_CAPTURE
                )
            }
        }
    }

    private fun dispatchGetVideoIntent() {
        Intent(
            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).also { getVideoIntent ->
            getVideoIntent.type = "video/*"
            getVideoIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(
                    Intent.createChooser(getVideoIntent, "Select Video"),
                    REQUEST_VIDEO_SELECT
                )
            }
        }
    }

    private fun warnAndRun(run_function: () -> (Unit)) {
        val builder = IntroAlertDialog(this@IntroActivity, run_function)
        builder.create()
        builder.show()
    }

    /**
     * [AppCompatActivity.onCreate]
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        take_video_button.startAnimation(fadeIn)
        take_video_from_gallery_button.startAnimation(fadeIn)

        take_video_button.setOnClickListener { warnAndRun { dispatchTakeVideoIntent() } }
        take_video_from_gallery_button.setOnClickListener { warnAndRun { dispatchGetVideoIntent() } }
    }

    /**
     * Retrieve uri from request. Checks whether the uri is valid under the restrictions.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        val videoUri = (data ?: return).data ?: return

        val videoFps = GetVideoData.getFPS(this@IntroActivity, videoUri)
        if (videoFps > resources.getInteger(R.integer.fps_limit)) {
            Toast.makeText(this, getString(R.string.fps_exceeded), LENGTH_LONG).show()
            return
        }

        val videoDuration = GetVideoData.getDuration(this@IntroActivity, videoUri)
        if (videoDuration > resources.getInteger(R.integer.length_limit) * 1000) {
            Toast.makeText(this, getString(R.string.length_exceeded), LENGTH_LONG).show()
            //return
        }
        Intent(this, TrimmingActivity::class.java).also {
            it.putExtra(VIDEO_URI, videoUri)
            startActivity(it)
        }
    }

    companion object {
        /**
         * Uri of the video retrieved.
         */
        const val VIDEO_URI: String = "pre_trim_video_uri"

        private const val REQUEST_VIDEO_CAPTURE = 1
        private const val REQUEST_VIDEO_SELECT = 2
    }
}
