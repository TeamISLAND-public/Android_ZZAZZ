package com.teamisland.zzazz.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
        const val VIDEO_URI: String = "pre_trim_video_uri"

        private const val LOAD_VIDEO = 1
    }

    private fun dispatchGetVideoIntent() {
        Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).apply {
//            action = Intent.ACTION_GET_CONTENT
            type = "video/*"
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            resolveActivity(packageManager)?.also {
//                startActivityForResult(
//                        Intent.createChooser(this, "Select Video"),
//                        REQUEST_VIDEO_SELECT
//                )
//            }
            startActivityForResult(this, LOAD_VIDEO)
//            resolveActivity(packageManager).also {
//                startActivityForResult(this, REQUEST_VIDEO_SELECT)
//            }
        }
    }

    /**
     * [AppCompatActivity.onCreate]
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        load.setOnClickListener { dispatchGetVideoIntent() }
    }

    /**
     * Retrieve uri from request. Checks whether the uri is valid under the restrictions.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        val videoUri: Uri
        if (requestCode == LOAD_VIDEO) {
            videoUri = (data ?: return).data ?: return
            Log.d("afdadsfdsafsad", videoUri.toString())

            val videoFps = this.let { GetVideoData.getFPS(it, videoUri) }
            if (videoFps > resources.getInteger(R.integer.fps_limit)) {
                Toast.makeText(this, getString(R.string.fps_exceeded), Toast.LENGTH_LONG).show()
                return
            }

            val videoDuration = this.let { GetVideoData.getDuration(it, videoUri) }
            if (videoDuration > resources.getInteger(R.integer.length_limit) * 1000) {
                Toast.makeText(this, getString(R.string.length_exceeded), Toast.LENGTH_LONG).show()
                return
            }

            Intent(this, TrimmingActivity::class.java).also {
                it.putExtra(VIDEO_URI, videoUri)
                startActivity(it)
            }
        }

    }
}