package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_after_selection.*
import org.florescu.android.rangeseekbar.RangeSeekBar

class TrimmingActivity : AppCompatActivity() {

    private fun barUpdateHandler(a: RangeSeekBar<Int>) {
        val i = a.selectedMinValue * 1000
        Log.d("@@@", "Milsec${i}")
        videoView.seekTo(i)
        videoView.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_selection)
        val video_uri = intent.getParcelableExtra<Uri>(getString(R.string.selected_video_uri))
        if (video_uri == null) {
            Toast.makeText(this, "Invalid video URI; Contact the developer.", Toast.LENGTH_LONG)
                .show()
            return
        }
        val rangeBar2 = rangeBar as RangeSeekBar<Int>
        videoView.setVideoURI(video_uri)
        videoView.setOnCompletionListener { }
        videoView.start()
        videoView.setOnCompletionListener { videoView.start() }
        rangeBar2.setOnRangeSeekBarChangeListener { a: RangeSeekBar<Int>, _: Number, _: Number ->
            barUpdateHandler(a)
        }
    }
}
