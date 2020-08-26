package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
//import android.content.Intent
//import android.content.pm.PackageManager.PERMISSION_GRANTED
//import android.graphics.Bitmap
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.snackbar.Snackbar
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.GetVideoData
import com.teamisland.zzazz.utils.ITrimmingData
import com.teamisland.zzazz.utils.dialog.LoadingDialog
import kotlinx.android.synthetic.main.activity_trimming.*
import kotlinx.coroutines.*
import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext

/**
 * Activity for video trimming.
 */
class TrimmingActivity : AppCompatActivity(), CoroutineScope {

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// Fields.
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    private val videoUri: Uri by lazy { intent.getParcelableExtra(IntroActivity.VIDEO_URI)!! }
    internal val videoDuration: Int by lazy { GetVideoData.getDuration(this, videoUri) }
    internal val videoFrameCount: Long by lazy { GetVideoData.getFrameCount(this, videoUri) }

    private val dataSourceFactory: DataSource.Factory by lazy {
        DefaultDataSourceFactory(this, Util.getUserAgent(this, "PlayerSample"))
    }
    internal val player: SimpleExoPlayer by lazy {
        SimpleExoPlayer.Builder(this).build().apply {
            repeatMode = SimpleExoPlayer.REPEAT_MODE_OFF
        }.also { mainVideoView.player = it }
    }
    private val mediaSource: MediaSource by lazy {
        ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri)
    }

    internal val dataBinder by lazy {
        object : ITrimmingData {

            override val duration = videoDuration
            override val frameCount = videoFrameCount

            override var rangeStartIndex: Long = 0L
                set(value) {
                    if (value == field) return
                    val eligibleChange = value in 0 until rangeExclusiveEndIndex

                    if (!eligibleChange) return

                    val oldValue = field
                    field = value

                    onRangeStartChanged(oldValue)
                    onRangeChanged(oldValue, rangeExclusiveEndIndex, true)
                }
            override var rangeExclusiveEndIndex: Long = videoFrameCount
                set(value) {
                    if (value == field) return
                    val eligibleChange = value in (rangeStartIndex + 1)..videoFrameCount

                    if (!eligibleChange) return

                    val oldValue = field
                    field = value

                    onRangeEndChanged(oldValue)
                    onRangeChanged(rangeStartIndex, oldValue, false)
                }

            override val startMs: Long
                get() = rangeStartIndex * videoDuration / videoFrameCount
            override val endMs: Long
                get() = rangeExclusiveEndIndex * videoDuration / videoFrameCount
            override val endExcludeMs: Long
                get() = (rangeExclusiveEndIndex - 1) * videoDuration / videoFrameCount

            override var currentVideoPosition: Long = 0
                get() = player.currentPosition
                set(value) {
                    if (value == field) return
                    if (value < startMs || endMs < value)
                        return

                    val oldValue = field
                    field = value

                    onCurrentVideoPositionChanged(oldValue, field)
                }

            override fun updateUI() {
                rangeSeekBarView.invalidate()
                currentPositionView.invalidate()
                goProjectButtonEnableCheck(startMs, endMs)
                setButtonEnable()
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// Data binding event handlers.
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    /** DO NOT USE [ITrimmingData.currentVideoPosition] HERE. */
    @Suppress("UNUSED_PARAMETER")
    internal fun onCurrentVideoPositionChanged(old: Long, new: Long) {
        player.seekTo(new)
        currentPositionView.invalidate()
    }

    @Suppress("UNUSED_PARAMETER")
    internal fun onRangeChanged(oldStart: Long, oldEnd: Long, isStartChanged: Boolean) {
        dataBinder.updateUI()
    }

    @Suppress("UNUSED_PARAMETER")
    internal fun onRangeEndChanged(old: Long) {
        dataBinder.currentVideoPosition = dataBinder.endExcludeMs
    }

    @Suppress("UNUSED_PARAMETER")
    internal fun onRangeStartChanged(old: Long) {
        dataBinder.currentVideoPosition = dataBinder.startMs
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// UI updaters.
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    internal fun goProjectButtonEnableCheck(start: Long, end: Long) {
        val seekDur = end - start
        val lengthLimit = resources.getInteger(R.integer.length_limit)
        val b = seekDur <= lengthLimit
        gotoProjectActivity.isEnabled = b
        currentPositionView.isEligible = b
    }

    internal fun setButtonEnable() {
        if (rangeSeekBarView.currentThumbIndex == -1) {
            framePlus.visibility = INVISIBLE
            frameMinus.visibility = INVISIBLE
            return
        }
        framePlus.visibility = VISIBLE
        frameMinus.visibility = VISIBLE
        val left = dataBinder.rangeStartIndex
        val right = dataBinder.rangeExclusiveEndIndex
        val frameCount = dataBinder.frameCount
        with(rangeSeekBarView) {
            when (currentThumbIndex) {
                0 -> {
                    framePlus.isEnabled = left + 1 < right
                    frameMinus.isEnabled = left > 0
                }
                1 -> {
                    framePlus.isEnabled = right < frameCount
                    frameMinus.isEnabled = left + 1 < right
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// Overrides.
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * [AppCompatActivity.dispatchTouchEvent]
     */
    override fun dispatchTouchEvent(motionEvent: MotionEvent?): Boolean {
        trimming_hint_text.visibility = GONE
        return super.dispatchTouchEvent(motionEvent)
    }

    /**
     * [AppCompatActivity.onCreate]
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimming)
        window.navigationBarColor = getColor(R.color.Background)

        // Set click handlers.
        backButton.setOnClickListener { onBackPressed() }

        gotoProjectActivity.setOnClickListener { startTrimming() }

        frameMinus.setOnClickListener { moveSelectedFrameIndexBy(-1) }
        framePlus.setOnClickListener { moveSelectedFrameIndexBy(1) }

        // Set controller(play/pause button) timeout.
        mainVideoView.controllerShowTimeoutMs = 1000

        // Setup views.
        rangeSeekBarView.selectedThumbView = selectedThumbView
        rangeSeekBarView.currentPositionView = currentPositionView
        rangeSeekBarView.bindData = dataBinder

        currentPositionView.bindData = dataBinder

        timeLineView.videoUri = videoUri
        timeLineView.bindData = dataBinder
        timeLineView.currentPositionView = currentPositionView

        player.prepare(mediaSource)
        player.addListener(object : Player.EventListener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying) return
                if (dataBinder.endExcludeMs <= player.currentPosition)
                    player.seekTo(dataBinder.startMs)
                launch {
                    while (player.isPlaying) {
                        currentPositionView.invalidate()
                        if (dataBinder.endExcludeMs <= player.currentPosition) {
                            player.playWhenReady = false
                            break
                        }
                        delay(16)
                    }
                }
            }

        })

        dataBinder.updateUI()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// Misc.
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun moveSelectedFrameIndexBy(amount: Int) {
        player.playWhenReady = false
        when (rangeSeekBarView.currentThumbIndex) {
            0 -> dataBinder.rangeStartIndex += amount
            1 -> dataBinder.rangeExclusiveEndIndex += amount
        }
    }

    private fun startTrimming() {
        val dialog = LoadingDialog(this, LoadingDialog.TRIM, dataBinder, videoUri)
        dialog.create()
        dialog.show()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// Companion codes.
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    companion object {
        /**
         * Path of the trimmed audio.
         */
        const val AUDIO_PATH: String = "AUDIO_PATH"

        /**
         * Path of the folder which has images of trimmed video.
         */
        const val IMAGE_PATH: String = "IMAGE_PATH"

        /**
         * Frame count of the trimmed video.
         */
        const val VIDEO_FRAME_COUNT: String = "TRIMMED_FRAME_COUNT"

        /**
         * Duration of the trimmed video.
         */
        const val VIDEO_DURATION: String = "TRIMMED_DURATION"

        /**
         * Result of the core model
         */
        const val MODEL_OUTPUT: String = "MODEL_OUTPUT"
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// Coroutine codes.
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    private val job = Job()

    /**
     * Notifies ongoing coroutine jobs that the activity has been ended.
     */
    override fun onDestroy() {
        job.cancel(CancellationException("Trimming activity has been destroyed."))
        super.onDestroy()
    }

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
