package com.example.ui_video

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_video)

        val video = findViewById<VideoView>(R.id.video_view)

        val controller = MediaController(this)
        video.setMediaController(controller)

        video.requestFocus()
        video.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.test_5s));

        video.postDelayed(Runnable {
            run {
                controller.show(0)
            }
        }, 100)
    }
}

