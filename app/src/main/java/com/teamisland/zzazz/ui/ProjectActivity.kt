package com.teamisland.zzazz.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.teamisland.zzazz.utils.FragmentPagerAdapter
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.VideoIntent
import kotlinx.android.synthetic.main.activity_project.*

class ProjectActivity : AppCompatActivity() {

    private lateinit var video: VideoView
    private lateinit var playBar: SeekBar
    private lateinit var playBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        video = findViewById(R.id.video_view)
        playBar = findViewById(R.id.play_bar)
        playBtn = findViewById(R.id.play_button)

        videoInit()
        tabInit()
    }

    // prepare the video
    private fun videoInit() {
        val value = intent.getParcelableExtra<VideoIntent>("value")
        val duration = value.duration
        val uri = value.uri
//        val duration = 5184

        video.setMediaController(null)

        video.setVideoURI(Uri.parse(uri))

        // when ready to play video
        video.setOnPreparedListener {
            playBar.max = duration
            playBar.progress = 0
        }

        videoStart()
    }

    // start the video
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
            } else {
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

    // make effect tab
    private fun tabInit() {
        effect_tab.addTab(effect_tab.newTab().setText(getString(R.string.tab1_name)))
        effect_tab.addTab(effect_tab.newTab().setText(getString(R.string.tab2_name)))

        val pagerAdapter =
            FragmentPagerAdapter(
                supportFragmentManager,
                3
            )
        view_pager.adapter = pagerAdapter

        effect_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                view_pager.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(effect_tab))
    }
}
