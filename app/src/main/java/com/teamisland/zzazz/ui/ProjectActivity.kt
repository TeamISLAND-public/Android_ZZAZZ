package com.teamisland.zzazz.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.teamisland.zzazz.R
//import com.teamisland.zzazz.utils.VideoIntent
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.export_dialog.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class ProjectActivity : AppCompatActivity() {

    private lateinit var uri: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        uri = "android.resource://$packageName/" + R.raw.test

        buttonToExport.setOnClickListener {
            val intent = Intent(this, ExportActivity::class.java)
            startActivity(intent)
        }

        videoInit()
        tabInit()
    }

    // prepare the video
    private fun videoInit() {
//        val value = intent.getParcelableExtra<VideoIntent>("value")
//        val duration = value.duration
//        val uri = value.uri
//        val duration = 5184

        video_view.setMediaController(null)

        video_view.setVideoURI(Uri.parse(uri))

        // when ready to play video
        video_view.setOnPreparedListener {
            play_bar.max = 0
            play_bar.progress = 0
        }

        videoStart()
    }

    // start the video
    private fun videoStart() {
        var position: Int = 0
        video_view.seekTo(position)

        video_view.run {
            position = currentPosition
            play_bar.progress = position
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

        play_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            // while dragging
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                position = progress
                video_view.seekTo(position)
                Log.d("progress", video_view.currentPosition.toString() + " currentFocus")
            }

            // when user starts dragging
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                video_view.pause()
                position = video_view.currentPosition
                video_view.seekTo(position)
                play_button.text = setTextPlay()
            }

            // when user stops touching
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        play_button.setOnClickListener {
            if (video_view.isPlaying) {
                video_view.pause()
                position = video_view.currentPosition
            } else {
                Log.d("progress", "$position position before start")
                video_view.seekTo(position)
                video_view.start()
                Log.d("progress", "$position position after start")
            }
            play_button.text = changeText(play_button.text)
        }

        // changing text of button is needed
        video_view.setOnCompletionListener {
            video_view.pause()
            play_button.text = setTextPlay()
            position = 0
        }
    }

    // make effect tab
    private fun tabInit() {
//        effect_tab.addTab(effect_tab.newTab().setText(getString(R.string.tab1_name)))
//        effect_tab.addTab(effect_tab.newTab().setText(getString(R.string.tab2_name)))
//
//        val pagerAdapter =
//            FragmentPagerAdapter(
//                supportFragmentManager,
//                3
//            )
//        view_pager.adapter = pagerAdapter
//
//        effect_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                view_pager.currentItem = tab!!.position
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {
//            }
//
//            override fun onTabReselected(tab: TabLayout.Tab?) {
//            }
//        })
//        view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(effect_tab))
    }
}
