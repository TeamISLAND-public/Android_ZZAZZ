package com.example.ui_video

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.MediaController
import android.widget.VideoView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_video)

        val video = findViewById<VideoView>(R.id.video_view)
        val playBtn = findViewById<Button>(R.id.play_button)

        val controller = MediaController(this)
        controller.hide()

        video.setMediaController(controller)

        video.requestFocus()
        video.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.test_5s));

        video.start()
        video.postDelayed(Runnable {
            run {
                controller.show(0)
                video.pause()
            }
        }, 100)

        playBtn.setOnClickListener {
            if (video.isPlaying) {
                video.pause()
                playBtn.text = "Play"
            } else {
                video.start()
                playBtn.text = "Pause"
            }

            video.setOnCompletionListener {
                video.pause()
                playBtn.text = "Play"
            }
        }
    }
}

