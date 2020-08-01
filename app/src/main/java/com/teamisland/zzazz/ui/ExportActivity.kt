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
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.teamisland.zzazz.R
import kotlinx.android.synthetic.main.activity_export.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.hypot

/**
 * Activity for export project.
 */
class ExportActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var uri: Uri
    internal var duration: Int = 0
    private lateinit var fadeOut: Animation

    private val dataSourceFactory: DataSource.Factory by lazy {
        DefaultDataSourceFactory(this, Util.getUserAgent(this, "PlayerSample"))
    }
    internal val player: SimpleExoPlayer by lazy {
        SimpleExoPlayer.Builder(this).build().also {
            it.repeatMode = SimpleExoPlayer.REPEAT_MODE_OFF
            preview.player = it
        }
    }

    //This is for done button
    internal var done = false

    /**
     * When restart the activity start preview like onCreate.
     * This function is called when cancel the sharing function.
     */
    override fun onRestart() {
        super.onRestart()
        player.playWhenReady = true
        preview_play.setImageDrawable(getDrawable(R.drawable.video_pause))
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
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
        player.prepare(mediaSource)
        preview.useController = false
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
        preview_progress.progress = 0
        preview_play.setImageDrawable(getDrawable(R.drawable.video_pause))

        videoStart()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun videoStart() {
        player.playWhenReady = true

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
        player.addListener(object : Player.EventListener {
            /**
             * Called when the value returned from either [.getPlayWhenReady] or [ ][.getPlaybackState] changes.
             *
             * @param playWhenReady Whether playback will proceed when ready.
             * @param playbackState The new [playback state][Player.State].
             */
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    launch {
                        do {
                            preview_progress.post {
                                preview_progress.progress = player.currentPosition.toInt()
                            }
                            delay(10)
                        } while (!done)
                    }
                }
            }
        })

        clickListener.setOnClickListener {
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
                if (isDragging) player.seekTo(progress.toLong())
            }

            // when user starts dragging
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                preview_progress.thumb = getDrawable(R.drawable.seekbar_pressed_thumb)
                playing = player.isPlaying
                player.playWhenReady = false
                isDragging = true
            }

            // when user stops touching
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                player.seekTo(preview_progress.progress.toLong())
                preview_progress.thumb = getDrawable(R.drawable.seekbar_normal_thumb)
                when {
                    preview_progress.progress == duration -> {
                        end = true
                        playing = false
                        preview_play.setImageDrawable(getDrawable(R.drawable.video_play))
                    }
                    playing -> player.playWhenReady = true
                    else -> end = false
                }
                isDragging = false
            }
        })

        preview_play.setOnClickListener {
            if (player.isPlaying) {
                player.playWhenReady = false
                preview_play.setImageDrawable(getDrawable(R.drawable.video_play))
            } else {
                if (end) {
                    player.seekTo(0)
                    end = false
                }
                player.playWhenReady = true
                preview_play.setImageDrawable(getDrawable(R.drawable.video_pause))
            }

            preview_play.startAnimation(fadeOut)
        }

        // changing text of button is needed
        player.addListener(object : Player.EventListener {
            /**
             * Called when the value returned from either [.getPlayWhenReady] or [ ][.getPlaybackState] changes.
             *
             * @param playWhenReady Whether playback will proceed when ready.
             * @param playbackState The new [playback state][Player.State].
             */
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    preview_play.setImageDrawable(getDrawable(R.drawable.video_play))
                    end = true
                }
            }
        })
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

        val parcelFileDescriptor =
            contentResolver.openFileDescriptor(outputUri ?: return, "w", null)

        val output = FileOutputStream((parcelFileDescriptor ?: return).fileDescriptor)

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

    /**
     * Stops all ongoing coroutines.
     * [AppCompatActivity.onDestroy]
     */
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private val job = Job()

    /**
     * The context of this scope.
     * Context is encapsulated by the scope and used for implementation of coroutine builders that are extensions on the scope.
     * Accessing this property in general code is not recommended for any purposes except accessing the [Job] instance for advanced usages.
     *
     * By convention, should contain an instance of a [job][Job] to enforce structured concurrency.
     */
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
}