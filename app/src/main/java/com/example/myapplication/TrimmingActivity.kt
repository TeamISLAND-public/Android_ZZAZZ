package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_after_selection.*
import org.florescu.android.rangeseekbar.RangeSeekBar

class TrimmingActivity : AppCompatActivity() {

    private fun playVideo() {
        if (videoView.currentPosition < rangeBar.selectedMinValue.toInt() * 1000) {
            videoView.seekTo(rangeBar.selectedMinValue.toInt() * 1000)
        }
        playButton.text = "Pause"
        playButton.setOnClickListener { stopVideo() }
        videoView.start()
    }

    private fun stopVideo() {
        if (videoView.currentPosition > rangeBar.selectedMinValue.toInt() * 1000) {
            videoView.seekTo(rangeBar.selectedMinValue.toInt() * 1000)
        }
        videoView.pause()
        playButton.text = "Start"
        playButton.setOnClickListener { playVideo() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_selection)
        val video_uri = intent.getParcelableExtra<Uri>(getString(R.string.selected_video_uri))
        if (video_uri == null) {
            Toast.makeText(this, getString(R.string.error_contact_devs), Toast.LENGTH_LONG).show()
            return
        }
        videoView.setVideoURI(video_uri)
        videoView.setOnCompletionListener {
            stopVideo()
            videoView.seekTo(1)
        }
        stopVideo()
        rangeBar.setOnRangeSeekBarChangeListener { _: RangeSeekBar<out Number>, _: Number, _: Number ->
            Log.d(
                "@@@",
                "starts at ${rangeBar.selectedMinValue} ends at ${rangeBar.selectedMaxValue}"
            )
        }
    }
}
