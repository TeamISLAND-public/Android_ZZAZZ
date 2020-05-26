package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.Gravity
import android.view.Window
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.net.toFile
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.VideoIntent
import kotlinx.android.synthetic.main.activity_export.*
import kotlinx.android.synthetic.main.export_dialog.*
import kotlinx.android.synthetic.main.finish_toast.*
import java.io.FileOutputStream
import kotlin.properties.Delegates

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ExportActivity : AppCompatActivity() {

    private lateinit var uri: String
    private var duration by Delegates.notNull<Int>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)


        val value = intent.getParcelableExtra<VideoIntent>("value")
//        duration = value.duration
//        uri = value.uri.toString()
        duration = 0
        uri = "android.resource://$packageName/raw/test_5s.mp4"

        videoInit()

        buttonToExport.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setCancelable(false)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.export_dialog)
            dialog.create()
            dialog.show()
            val window = dialog.window

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setGravity(Gravity.CENTER)

            val input = getFileStreamPath(uri).inputStream()
//            val input = contentResolver.openInputStream(Uri.parse(uri))
            val output = FileOutputStream(getExternalFilesDir(Environment.DIRECTORY_MOVIES))
            val data = ByteArray(1024)
            var total = 0
            var count: Int
            dialog.progress_text.text = "$total%"
            val handler = Handler()

            Thread(Runnable {
                do {
                    count = input.read(data)
                    total += count
                    Thread.sleep(100)

                    handler.post {
                        dialog.export_progress.progress =
                            ((total * 100) / Uri.parse(uri).toFile().length()).toInt()
                        dialog.progress_text.text =
                            (((total * 100) / Uri.parse(uri).toFile()
                                .length()).toInt()).toString() + "%"
                    }

                    output.write(data, 0, count)
                } while (count != -1)
                output.flush()
                output.close()
                input.close()
                dialog.dismiss()
                val layout = layoutInflater.inflate(R.layout.finish_toast, finish)
                val toast = Toast(this)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.duration = Toast.LENGTH_SHORT
                toast.view = layout
                toast.show()
            }).start()
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

        // test ad
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build()
        adView.loadAd(adRequest)
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
