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
import android.util.Log
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
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.VideoIntent
import kotlinx.android.synthetic.main.activity_export.*
import kotlinx.android.synthetic.main.export_dialog.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ExportActivity : AppCompatActivity() {

    private lateinit var uri: String
    private var duration: Int = 0

    @Suppress("BlockingMethodInNonBlockingContext")
    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)

        val value = intent.getParcelableExtra<VideoIntent>("value")
//        uri = value.uri.toString()
        uri = "android.resource://$packageName/" + R.raw.test

        var checkStop = false
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
            );
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

        val dirString = Environment.getExternalStorageDirectory().toString() + "/ZZAZZ"
        val dir = File(dirString)
        if (!dir.exists()) {
            dir.mkdirs()
        }
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
            finish()
        }

        videoInit()

        // set translucent the image when they are not installed
        if (!isInstall("com.icon_instagram.android")) {
            share_instagram.alpha = 0.5F
        }
        if (!isInstall("com.icon_kakaotalk.android")) {
            share_kakaotalk.alpha = 0.5F
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

        //This is for test device which is the emulator
        //We have to remove the code which sets test device id before releasing application
        val testDeviceIds = mutableListOf("33BE2250B43518CCDA7DE426D04EE231")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)
    }

    private fun videoInit() {
        preview.setMediaController(null)
        preview.setVideoURI(Uri.parse(uri))
        preview.requestFocus()
        MediaMetadataRetriever().also {
            it.setDataSource(this, Uri.parse(uri))
            val time = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = time.toInt()
            Log.d("duration", duration.toString())
        }

        preview_play.setImageDrawable(getDrawable(R.drawable.preview_play))

        preview.setOnPreparedListener {
            preview_progress.max = duration
            preview_progress.progress = 0
        }

        videoStart()
    }

    private fun videoStart() {
        var position: Int = 0
        preview.seekTo(position)

        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        preview.setOnClickListener {
            preview_play.visibility = View.VISIBLE

            fadeOut.setAnimationListener(object :Animation.AnimationListener{
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    preview_play.visibility = View.GONE
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })

            preview_play.startAnimation(fadeOut)
        }

        preview.run {
            position = currentPosition
            preview_progress.progress = position
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
            }

            // when user stops touching
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        preview_play.setOnClickListener {
            if (preview.isPlaying) {
                preview.pause()
                position = preview.currentPosition
                preview_play.setImageDrawable(getDrawable(R.drawable.preview_play))
//                preview_play.background = getDrawable(R.drawable.shadow_effect)
                preview_play.outlineProvider = CustomOutlineProvider()
                preview_play.clipToOutline = true
            } else {
                preview.start()
                preview_play.setImageDrawable(getDrawable(R.drawable.preview_pause))
            }
            preview_play.startAnimation(fadeOut)
        }

        // changing text of button is needed
        preview.setOnCompletionListener {
            preview.pause()
            position = 0
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
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return
                }
                return
            }
        }
    }
}

private class CustomOutlineProvider : ViewOutlineProvider() {
    override fun getOutline(view: View?, outline: Outline?) {
        outline!!.setRoundRect(0, 0, view!!.width, view.height, 10F)
    }
}