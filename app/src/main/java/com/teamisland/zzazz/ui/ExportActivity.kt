package com.teamisland.zzazz.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.teamisland.zzazz.R
import kotlinx.android.synthetic.main.activity_export.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.hypot

/**
 * Activity for export project.
 */
class ExportActivity : AppCompatActivity() {

    private lateinit var uri: Uri
    private var duration: Int = 0
    private lateinit var fadeOut: Animation

    //This is for done button
    private var done = false

    /**
     * When restart the activity start preview like onCreate.
     * This function is called when cancel the sharing function.
     */
    override fun onRestart() {
        super.onRestart()
        preview.start()
        preview_play.setImageDrawable(getDrawable(R.drawable.preview_pause))
    }

    /**
     * When the activity is created
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)

        //This is for test
        uri = intent.getParcelableExtra("URI")

        videoInit()

        save.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    save.alpha = 0.4F
                }

                MotionEvent.ACTION_UP -> {
                    save.alpha = 1F
                    videoSave()
                }
            }
            true
        }

        share.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    share.alpha = 0.4F
                }

                MotionEvent.ACTION_UP -> {
                    share.alpha = 1F
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_STREAM,
                            FileProvider.getUriForFile(
                                this@ExportActivity,
                                "com.teamisland.zzazz.fileprovider",
                                File(uri.path)
                            )
                        )
                        type = "video/*"
                        startActivity(Intent.createChooser(this, "Share"))
                    }
                }
            }
            true
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
        preview.setVideoURI(uri)
        preview.requestFocus()

        //Get duration from uri & set duration
        MediaMetadataRetriever().also {
            it.setDataSource(this, uri)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun videoStart() {
        preview.start()

        fadeOut = AnimationUtils.loadAnimation(this@ExportActivity, R.anim.fade_out)
        fadeOut.startOffset = 1000
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                preview_play.visibility = View.VISIBLE
            }

            // button needs to be vanished
            override fun onAnimationEnd(animation: Animation?) {
                preview_play.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
                preview_play.visibility = View.VISIBLE
            }
        })
        fadeOut.duration = 500

        preview_play.startAnimation(fadeOut)

        //This is for video is end
        //When video is end, preview will start from first
        var end = false

        done_export.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    done_export.alpha = 0.4F
                }

                MotionEvent.ACTION_UP -> {
                    done_export.alpha = 1F
                    done = true
                    Intent(this, IntroActivity::class.java).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(this)
                    }
                }
            }
            true
        }

        back.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    back.alpha = 0.4F
                }

                MotionEvent.ACTION_UP -> {
                    back.alpha = 1F
                    done = true
                    finish()
                }
            }
            true
        }

        //Use thread to link seekBar from video
        preview.setOnPreparedListener {
            Thread(Runnable {
                do {
                    preview_progress.post {
                        preview_progress.progress = preview.currentPosition
                    }
                    Thread.sleep(10)
                } while (!done)
            }).start()
        }

        preview.setOnClickListener {
            preview_play.startAnimation(fadeOut)
        }

        preview_progress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {

            var playing = true
            var isDragging = false

            // while dragging
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (isDragging) preview.seekTo(progress)
            }

            // when user starts dragging
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                preview_progress.thumb = getDrawable(R.drawable.seekbar_pressed_thumb)
                playing = preview.isPlaying
                preview.pause()
                isDragging = true
            }

            // when user stops touching
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                preview.seekTo(preview_progress.progress)
                preview_progress.thumb = getDrawable(R.drawable.seekbar_normal_thumb)
                when {
                    preview_progress.progress == duration -> {
                        end = true
                        playing = false
                        preview_play.setImageDrawable(getDrawable(R.drawable.preview_play))
                    }
                    playing -> preview.start()
                    else -> end = false
                }
                isDragging = false
            }
        })

        preview_play.setOnClickListener {
            if (preview.isPlaying) {
                preview.pause()
                preview_play.setImageDrawable(getDrawable(R.drawable.preview_play))
            } else {
                if (end) {
                    preview.seekTo(0)
                    end = false
                }
                preview.start()
                preview_play.setImageDrawable(getDrawable(R.drawable.preview_pause))
            }

            preview_play.startAnimation(fadeOut)
        }

        // changing text of button is needed
        preview.setOnCompletionListener {
            preview_play.setImageDrawable(getDrawable(R.drawable.preview_play))
            end = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @Suppress(
        "BlockingMethodInNonBlockingContext",
        "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
    )
    @SuppressLint("SimpleDateFormat", "SetTextI18n", "InflateParams")
    private fun videoSave() {
        fadeOut.start()

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

        val input = contentResolver.openInputStream(Uri.fromFile(File(uri.path)))

        //Video name is depended by time
        val time = System.currentTimeMillis()
        val date = Date(time)
        val nameFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = nameFormat.format(date) + ".mp4"

        val contentValues = ContentValues()
        contentValues.put(
            MediaStore.Files.FileColumns.RELATIVE_PATH,
            Environment.DIRECTORY_MOVIES + "/ZZAZZ"
        )
        contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, filename)
        contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, "video/*")
        contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 1)

        val outputUri =
            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

        val parcelFileDescriptor = contentResolver.openFileDescriptor(outputUri!!, "w", null)

        val output = FileOutputStream(parcelFileDescriptor!!.fileDescriptor)

        val data = ByteArray(1024)
        var total = 0
        var count: Int
        val handler = Handler()

        val dialog = Dialog(this@ExportActivity)

        var isExport = false
        var isFinished = false

        //during saving
        GlobalScope.launch {
            handler.post {
                save.visibility = View.GONE
                dialog.setCancelable(false)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.export_dialog)
                dialog.create()
                dialog.show()
                val window = dialog.window
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.setGravity(Gravity.CENTER)
            }
            count = try {
                input?.read(data) ?: return@launch
            } catch (e: Exception) {
                -1
            }
            while (count != -1) {
                total += count

                try {
                    output.write(data, 0, count)
                    if (input != null) count = input.read(data)
                } catch (e: Exception) {
                    checkStop = true
                    break
                }
            }

            if (!checkStop) {
                input?.close()
                output.flush()
                output.close()
                contentValues.clear()
                contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 0)
                contentResolver.update(outputUri, contentValues, null, null)
                isExport = true
            }
        }

        // after saved
        GlobalScope.launch {
            while (!isFinished) {
                while (isExport) {
                    handler.post {
                        dialog.dismiss()

                        save.background = getDrawable(R.drawable.check)
                        val finRadius = hypot((save.width * 2).toDouble(), save.height.toDouble())
                        val animation = ViewAnimationUtils.createCircularReveal(
                            save, 0, save.height / 2, 0F,
                            finRadius.toFloat()
                        )
                        animation.duration = 1000
                        save.visibility = View.VISIBLE
                        animation.start()
                        save.isEnabled = false
                        save_text.text = getText(R.string.saved_text)

                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibrator.vibrate(VibrationEffect.createOneShot(200, 20))
                    }
                    isFinished = true
                    isExport = false
                }
            }
        }
    }

    /**
     * Request permission to save video.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1)
            return
    }
}