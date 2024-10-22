package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.exoplayer2.ExoPlayer
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
import com.teamisland.zzazz.ui.ProjectActivity.Companion.RESULT
import com.teamisland.zzazz.ui.TrimmingActivity.Companion.VIDEO_FRAME_COUNT
import com.teamisland.zzazz.utils.dialog.LoadingDialog
import com.teamisland.zzazz.utils.dialog.LoadingDialog.Companion.SAVE
import com.teamisland.zzazz.utils.objects.GetVideoData
import kotlinx.android.synthetic.main.activity_export.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.math.hypot

/**
 * Activity for export project.
 */
class ExportActivity : AppCompatActivity(), CoroutineScope {

    internal var duration: Int = 0
    private val path: String by lazy { intent.getStringExtra(RESULT) }
    private val frameCount: Int by lazy { intent.getIntExtra(VIDEO_FRAME_COUNT, 0) }
    private val uri: Uri by lazy { Uri.parse(path) }

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

    //This is for video is end
    //When video is end, preview will start from first
    private var end = false

    /**
     * When restart the activity start preview like onCreate.
     * This function is called when cancel the sharing function.
     */
    override fun onRestart() {
        super.onRestart()
        player.playWhenReady = true
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
        window.navigationBarColor = getColor(R.color.Background)

        videoInit()

        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        fadeOut.startOffset = 1000
        fadeOut.duration = 500
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                playButton.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation?) {
                playButton.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
                playButton.visibility = View.VISIBLE
            }
        })
        playButton.setOnClickListener { playButtonClickHandler(fadeOut) }
        playButton.startAnimation(fadeOut)

        video.setOnClickListener { playButton.startAnimation(fadeOut) }

        done_export.setOnClickListener {
            done = true
            Intent(this, IntroActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(this)
            }
        }

        back.setOnClickListener { finish() }

        save.setOnClickListener { videoSave() }

        share.setOnClickListener {
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
            duration = GetVideoData.getDuration(this, uri)
            val minute = duration / 60000
            val second = duration / 1000 - minute * 60
            video_length.text = String.format("%02d:%02d", minute, second)
        }
        preview_progress.max = duration
        preview_progress.progress = 0

        player.playWhenReady = true

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
                preview_progress.thumb =
                    ContextCompat.getDrawable(this@ExportActivity, R.drawable.seekbar_pressed_thumb)
                preview_progress.progressTintList =
                    ColorStateList.valueOf(getColor(R.color.PointColor))
                playing = player.isPlaying
                player.playWhenReady = false
                isDragging = true
            }

            // when user stops touching
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                player.seekTo(preview_progress.progress.toLong())
                preview_progress.thumb =
                    ContextCompat.getDrawable(this@ExportActivity, R.drawable.seekbar_normal_thumb)
                preview_progress.progressTintList = ColorStateList.valueOf(getColor(R.color.White))
                when {
                    preview_progress.progress == duration -> {
                        end = true
                        playing = false
                    }
                    playing -> player.playWhenReady = true
                    else -> end = false
                }
                isDragging = false
            }
        })

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
                    end = true
                    playButton.isActivated = false
                }
            }
        })

        stopVideo()
    }

    private fun stopVideo() {
        playButton.isActivated = false
        player.playWhenReady = false
    }

    internal fun startVideo() {
        player.playWhenReady = true
        playButton.isActivated = true
        Thread(Runnable {
            do {
                preview_progress.apply {
                    this.progress = player.currentPosition.toInt()
                    if (end) {
                        end = false
                        stopVideo()
                    }
                }
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            } while (player.isPlaying)
        }).start()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @Suppress(
        "BlockingMethodInNonBlockingContext",
        "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
    )
    private fun videoSave() {
        val dialog = LoadingDialog(this, SAVE, frameCount)
        dialog.create()
        dialog.show()
        CoroutineScope(Dispatchers.Main).launch {
            dialog.play {
                save.background =
                    ContextCompat.getDrawable(this@ExportActivity, R.drawable.check)
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
        }
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

    private val playButtonClickListenerObject = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == ExoPlayer.STATE_READY)
                preview.postDelayed({ startVideo() }, 100)
        }
    }

    private fun playButtonClickHandler(fadeOut: Animation) {
        playButton.startAnimation(fadeOut)
        when {
            end -> {
                end = false
                player.seekTo(0)
                player.removeListener(playButtonClickListenerObject)
                startVideo()
            }
            playButton.isActivated -> {
                stopVideo()
            }
            else -> {
                player.removeListener(playButtonClickListenerObject)
                startVideo()
            }
        }
    }
}