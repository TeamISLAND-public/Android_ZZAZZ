package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_after_selection.*

class AfterSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_selection)
        val video_uri = intent.getParcelableExtra<Uri>(getString(R.string.selected_video_uri))
        videoView.setVideoURI(video_uri)
        videoView.start()
        videoView.setOnCompletionListener { videoView.start() }
    }
}
