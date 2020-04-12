package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.media.MediaExtractor
import android.media.MediaFormat.KEY_DURATION
import android.media.MediaFormat.KEY_FRAME_RATE
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_before_selection.*

class BeforeSelectionActivity : AppCompatActivity() {

    private val _REQUEST_VIDEO_CAPTURE = 1
    private val _REQUEST_VIDEO_SELECT = 2

    private fun dispatchTakeVideoIntent() {
        showRestrictionToast()
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(
                packageManager
            )?.also { startActivityForResult(takeVideoIntent, _REQUEST_VIDEO_CAPTURE) }
        }
    }

    private fun dispatchGetVideoIntent() {
        showRestrictionToast()
        Intent(
            Intent.ACTION_PICK,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).also { getVideoIntent ->
            getVideoIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(
                    Intent.createChooser(getVideoIntent, "Select Video"), _REQUEST_VIDEO_SELECT
                )
            }
        }
    }

    private fun showRestrictionToast() {
        Toast.makeText(
            this@BeforeSelectionActivity,
            getString(
                R.string.video_restrictions,
                resources.getInteger(R.integer.fps_limit),
                resources.getInteger(R.integer.length_limit) / 1000
            ),
            LENGTH_LONG
        ).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_before_selection)

        take_video_button.setOnClickListener { dispatchTakeVideoIntent() }
        take_video_from_gallery_button.setOnClickListener { dispatchGetVideoIntent() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        val videoUri = data!!.data ?: return
        val data_two = MediaExtractor()
        data_two.setDataSource(this@BeforeSelectionActivity, videoUri, null)
        val video_info = data_two.getTrackFormat(0)
        val video_duration = video_info.getLong(KEY_DURATION)
        val video_fps = video_info.getInteger(KEY_FRAME_RATE)
        Log.d("video characteristics", "$video_duration microsecond at $video_fps fps")
        if (video_duration > resources.getInteger(R.integer.length_limit) * 1000) {
            Toast.makeText(this, "length exceeded", LENGTH_LONG).show()
            return
        }
        if (video_fps > resources.getInteger(R.integer.fps_limit)) {
            Toast.makeText(this, "fps exceeded", LENGTH_LONG).show()
            return
        }
        val intent = Intent(this, AfterSelectionActivity::class.java).also {
            it.putExtra(getString(R.string.selected_video_uri), videoUri)
        }
        startActivity(intent)
    }
}
