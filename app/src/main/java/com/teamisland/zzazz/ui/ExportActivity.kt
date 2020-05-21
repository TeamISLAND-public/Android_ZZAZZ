package com.teamisland.zzazz.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.ExportDialog
import com.teamisland.zzazz.utils.VideoIntent
import kotlinx.android.synthetic.main.activity_export.*
import kotlin.properties.Delegates

class ExportActivity : AppCompatActivity() {

    private lateinit var uri: String
    private var duration by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)


        val value = intent.getParcelableExtra<VideoIntent>("value")
//        duration = value.duration
//        uri = value.uri.toString()
        duration = 0
        uri = ""

        videoInit()

        buttonToExport.setOnClickListener {
            val dialog = ExportDialog(this@ExportActivity)
            dialog.setCancelable(false)
            dialog.create()
            dialog.show()
        }

        share_instagram.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "video/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.setPackage("com.instagram.android")
            startActivity(intent)
        }

        share_kakaotalk.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "video/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.setPackage("com.kakao.talk")
            startActivity(intent)
        }
    }

    private fun videoInit() {
        preview.setMediaController(null)
        preview.setVideoURI(Uri.parse(uri))

        preview.setOnPreparedListener {
            preview_progress.max = duration
            preview_progress.progress = 0
        }

        videoStart()
    }


    private fun videoStart() {
        var position: Int = 0
        preview.seekTo(position)

        preview.run {
            position = currentPosition
            preview_progress.progress = position
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

        preview_progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            // while dragging
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                position = progress
                preview.seekTo(position)
            }

            // when user starts dragging
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                preview.pause()
                position = preview.currentPosition
                preview.seekTo(position)
                preview_play.text = setTextPlay()
            }

            // when user stops touching
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        preview_play.setOnClickListener {
            if (preview.isPlaying) {
                preview.pause()
                position = preview.currentPosition
            } else {
                preview.seekTo(position)
                preview.start()
            }
            preview_play.text = changeText(preview_play.text)
        }

        // changing text of button is needed
        preview.setOnCompletionListener {
            preview.pause()
            preview_play.text = setTextPlay()
            position = 0
        }
    }
}
