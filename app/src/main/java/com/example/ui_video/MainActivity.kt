package com.example.ui_video

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.VideoView

class MainActivity : AppCompatActivity() {

    private lateinit var video: VideoView
    private lateinit var playBar: SeekBar
    private lateinit var playBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_video)

        video = findViewById(R.id.video_view)
        playBar = findViewById(R.id.play_bar)
        playBtn = findViewById(R.id.play_button)

        videoInit()
    }

    private fun videoInit() {
        val intent = intent
//        val duration = intent.getIntExtra("duration")
//        val uri = intent.getIntExtra("Uri")
        // duration of test file is 5184ms
        val duration = 5184

        video.setMediaController(null)

//        video.setVideoURI(Uri.parse(uri))
        video.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.test_5s))

        // when ready to play video
        video.setOnPreparedListener {
            playBar.max = duration
            playBar.progress = 0
        }

        videoStart()
    }

    private fun videoStart() {
        var position: Int = 0
        video.seekTo(position)

        video.run {
            position = currentPosition
            playBar.progress = position
            Log.d("progress", position.toString())
        }

        // the function for change text of button
        fun changeText(prev: CharSequence): CharSequence {
            if (prev == "Play") return "Pause"
            return "Play"
        }

        // the function for set text of button to play
        fun setTextPlay(): CharSequence {
            return "Play"
        }

        playBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            // while dragging
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                position = progress
                video.seekTo(position)
                Log.d("progress", video.currentPosition.toString() + " currentFocus")
            }

            // when user starts dragging
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                video.pause()
                position = video.currentPosition
                video.seekTo(position)
                playBtn.text = setTextPlay()
            }

            // when user stops touching
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        playBtn.setOnClickListener {
            if (video.isPlaying) {
                video.pause()
                position = video.currentPosition
            }
            else {
                Log.d("progress", "$position position before start")
                video.seekTo(position)
                video.start()
                Log.d("progress", "$position position after start")
            }
            playBtn.text = changeText(playBtn.text)
        }

        // changing text of button is needed
        video.setOnCompletionListener {
            video.pause()
            playBtn.text = setTextPlay()
            position = 0
        }
    }
}
