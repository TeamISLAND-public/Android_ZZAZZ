package com.teamisland.zzazz.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaExtractor
import android.media.MediaFormat.KEY_DURATION
import android.media.MediaFormat.KEY_FRAME_RATE
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.IntroAlertDialog
import kotlinx.android.synthetic.main.activity_intro.*
import kotlinx.android.synthetic.main.activity_intro_alertdialog.*

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
            )?.also { startActivityForResult(takeVideoIntent,
                REQUEST_VIDEO_CAPTURE
            ) }
        }
    }

    private fun dispatchGetVideoIntent() {
        Intent(
            Intent.ACTION_PICK,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).also { getVideoIntent ->
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        take_video_button.setOnClickListener { warnAndRun { dispatchTakeVideoIntent() } }
        take_video_from_gallery_button.setOnClickListener { warnAndRun { dispatchGetVideoIntent() } }

        gotoExportActivity.setOnClickListener {
            val intent = Intent(this, ProjectActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        val videoUri = (data ?: return).data ?: return
        val data_two = MediaExtractor()
        data_two.setDataSource(this@IntroActivity, videoUri, null)
        val video_info = data_two.getTrackFormat(0)
        val video_duration = video_info.getLong(KEY_DURATION)
        val video_fps = video_info.getInteger(KEY_FRAME_RATE)
        if (video_duration > resources.getInteger(R.integer.length_limit) * 1000) {
            Toast.makeText(this, getString(R.string.length_exceeded), LENGTH_LONG).show()
            return
        }
        if (video_fps > resources.getInteger(R.integer.fps_limit)) {
            Toast.makeText(this, getString(R.string.fps_exceeded), LENGTH_LONG).show()
            return
        }
        val intent = Intent(this, TrimmingActivity::class.java).also {
            it.putExtra(getString(R.string.selected_video_uri), videoUri)
        }
        startActivity(intent)
        finish()
    }

    companion object {
        private const val REQUEST_VIDEO_CAPTURE = 1
        private const val REQUEST_VIDEO_SELECT = 2
    }
}
