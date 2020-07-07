package com.teamisland.zzazz.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.VideoIntent
import kotlinx.android.synthetic.main.activity_export.*
import kotlinx.android.synthetic.main.export_dialog.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.Runnable
import java.text.SimpleDateFormat
import java.util.*

class ExportActivity : AppCompatActivity() {

    private lateinit var uri: String
    private var duration: Int = 0
    //This is for done button
    private var done = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)

//        uri = value.uri.toString()
        //This is for test
        uri = "android.resource://$packageName/" + R.raw.test

        // set translucent the image when they are not installed
        if (!isInstall("com.instagram.android")) {
            share_instagram.alpha = 0.5F
            share_instagram.isEnabled = false
        }
        if (!isInstall("com.kakao.talk")) {
            share_kakaotalk.alpha = 0.5F
            share_kakaotalk.isEnabled = false
        }

        videoInit()

        save.setOnClickListener {
            videoSave()
        }

        share_instagram.setOnClickListener {
            preview.pause()
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "video/*"
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri))
            intent.setPackage("com.instagram.android")
            startActivity(intent)
        }

        share_kakaotalk.setOnClickListener {
            preview.pause()
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "video/*"
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri))
            intent.setPackage("com.kakao.talk")
            startActivity(intent)
        }

        //This is for test device which is the emulator
        //We have to remove the code which sets test device id before releasing application
        val testDeviceIds = listOf("33BE2250B43518CCDA7DE426D04EE231")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)
        MobileAds.initialize(this)
        adView.loadAd(AdRequest.Builder().build())
    }

    @SuppressLint("SetTextI18n")
    private fun videoInit() {
        preview.setMediaController(null)
        preview.setVideoURI(Uri.parse(uri))
        preview.requestFocus()

        //Get duration from uri & set duration
        MediaMetadataRetriever().also {
            it.setDataSource(this, Uri.parse(uri))
            val time = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = time.toInt()
            val minute = duration / 60000
            val second = duration / 1000 - minute * 60
            video_length.text = String.format("%02d:%02d", minute, second)
        }
        preview_progress.max = duration
        preview_play.setImageDrawable(getDrawable(R.drawable.preview_pause))
        preview.setOnPreparedListener {
            preview_progress.progress = 0
        }

        videoStart()
    }

    private fun videoStart() {
        preview.seekTo(0)
        preview.start()

        val fadeOut = AnimationUtils.loadAnimation(this@ExportActivity, R.anim.fade_out)
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            // button needs to be vanished
            override fun onAnimationEnd(animation: Animation?) {
                preview_play.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })

        preview_play.startAnimation(fadeOut)

        //This is for video is end
        //When video is end, preview will start from first
        var end = false

        done_export.setOnClickListener {
            done = true
            val intent = Intent(this, IntroActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        back.setOnClickListener {
            done = true
            finish()
        }

        //Use thread to link seekBar from video
        preview.setOnPreparedListener {
            Thread(Runnable {
                do {
                    preview_progress.post {
                        preview_progress.progress = preview.currentPosition
                    }
                    try {
                        Thread.sleep(10)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                } while (!done)
            }).start()
        }

        preview.setOnClickListener {
            preview_play.visibility = View.VISIBLE

            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                // button needs to be vanished
                override fun onAnimationEnd(animation: Animation?) {
                    preview_play.visibility = View.GONE
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })

            preview_play.startAnimation(fadeOut)
        }

        preview_progress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            //This is for check_green user is dragging
            var drag = false

            //This is for check_green the state of video before user dragging
            var playing = true

            // while dragging
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (drag)
                    preview.seekTo(progress)
            }

            // when user starts dragging
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                playing = preview.isPlaying
                preview.pause()
                drag = true
            }

            // when user stops touching
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                preview.seekTo(preview_progress.progress)
                drag = false
                if (playing)
                    preview.start()
            }
        })

        preview_play.setOnClickListener {
            if (preview.isPlaying) {
                preview.pause()
                preview_play.setImageDrawable(getDrawable(R.drawable.preview_play))
//                preview_play.background = getDrawable(R.drawable.shadow_effect)
                preview_play.outlineProvider = CustomOutlineProvider()
                preview_play.clipToOutline = true
            } else {
                if (end)
                    preview.seekTo(0)
                preview.start()
                preview_play.setImageDrawable(getDrawable(R.drawable.preview_pause))
            }

            preview_play.visibility = View.VISIBLE
            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                // button needs to be vanished
                override fun onAnimationEnd(animation: Animation?) {
                    preview_play.visibility = View.GONE
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })

            preview_play.startAnimation(fadeOut)
        }

        // changing text of button is needed
        preview.setOnCompletionListener {
            preview.pause()
            preview_play.setImageDrawable(getDrawable(R.drawable.preview_play))
            end = true
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @SuppressLint("SimpleDateFormat", "SetTextI18n", "InflateParams")
    private fun videoSave() {
        var checkStop = false

        //Check permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }

        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.export_dialog)
        dialog.create()
        dialog.show()
        val window = dialog.window

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setGravity(Gravity.CENTER)

        val input = contentResolver.openInputStream(Uri.parse(uri))

        //Make file directory for saving the video
        val dirString = Environment.getExternalStorageDirectory().toString() + "/ZZAZZ"
        val dir = File(dirString)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        //Video name is depended by time
        val time = System.currentTimeMillis()
        val date = Date(time)
        val nameFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = nameFormat.format(date)
        val file = "$dirString/$filename.mp4"
        val output = FileOutputStream(File(file))

        val data = ByteArray(1024)
        val len = input!!.available()
        var total = 0
        var count: Int
        val handler = Handler()
        dialog.progress_text.text = "$total%"

        //Use coroutine for exporting
        val exporting = GlobalScope.launch {
            count = try {
                input.read(data)
            } catch (e: Exception) {
                -1
            }
            while (count != -1) {
                total += count

                handler.post {
                    dialog.progress_text.text =
                        (total.toDouble() / len * 100).toInt().toString() + "%"
                }

                try {
                    output.write(data, 0, count)
                    count = input.read(data)
                } catch (e: Exception) {
                    checkStop = true
                    break
                }
            }

            if (!checkStop) {
                input.close()
                output.flush()
                output.close()
                dialog.dismiss()

                handler.post {
                    val toast = Toast(this@ExportActivity)
                    val layout = layoutInflater.inflate(R.layout.finish_toast, null)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.view = layout
                    toast.show()
                }
            }
        }

        dialog.export_stop.setOnClickListener {
            input.close()
            output.close()
            dialog.dismiss()
            File(file).delete()
            exporting.cancel()
        }
    }

    private fun isInstall(packageName: String): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        return intent != null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1)
            return
    }

    private class CustomOutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            outline!!.setRoundRect(0, 0, view!!.width, view.height, 10F)
        }
    }
}
