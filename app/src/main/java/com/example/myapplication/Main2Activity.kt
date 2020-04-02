package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main2.*

class Main2Activity : AppCompatActivity() {

    private var total_duration: Int = 0
    private var current_pos: Int = 0
    private var duration: Int = 0
    private val REQUEST_VIDEO_CAPTURE = 1
    private val REQUEST_VIDEO_SELECT = 2

    private fun dispatchTakeVideoIntent() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        take_video_button.setOnClickListener {
            dispatchTakeVideoIntent()
        }

        take_video_from_gallery_button.setOnClickListener {
            dispatchGetVideoIntent()
        }
        buttonInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        val videoUri: Uri? = data!!.data
        Log.d("vidURI ", videoUri.toString())
        videoView.setVideoURI(videoUri)
        videoView.setOnCompletionListener {
            videoView.start()
        }
        duration = videoView.duration
        videoView.setOnPreparedListener { setVideoProgress() }
        videoView.start()
    }

    private fun setVideoProgress() {
        current_pos = videoView.currentPosition
        total_duration = videoView.duration
    }

    private fun buttonInfo() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    Log.d("seekBar", progress.toString())
                    Log.d("video duration", total_duration.toString())
                    videoView.seekTo(progress * total_duration / 100)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.d("seekBar", duration.toString())
                Log.d("seekBar", videoView.currentPosition.toString())
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d("seekBar", "stop")
            }
        })
    }
}
