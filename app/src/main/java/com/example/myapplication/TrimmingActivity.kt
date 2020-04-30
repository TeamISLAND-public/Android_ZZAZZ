package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_after_selection.*

class TrimmingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_selection)
        val video_uri = intent.getParcelableExtra<Uri>(getString(R.string.selected_video_uri))
        if (video_uri == null) {
            Toast.makeText(this, "Invalid video URI; Contact the developer.", Toast.LENGTH_LONG)
                .show()
            return
        }
        videoView.setVideoURI(video_uri)
        videoView.start()
        videoView.setOnCompletionListener { videoView.start() }
    }
}
