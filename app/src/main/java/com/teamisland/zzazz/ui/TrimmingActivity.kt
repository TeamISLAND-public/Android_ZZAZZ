package com.teamisland.zzazz.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.arthenica.mobileffmpeg.Config
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.AbsolutePathRetriever
import com.teamisland.zzazz.utils.FFmpegDelegate
import com.teamisland.zzazz.utils.GetVideoData
import com.teamisland.zzazz.utils.ITrimmingData
import kotlinx.android.synthetic.main.activity_trimming.*
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext

/**
 * Activity for video trimming.
 */
class TrimmingActivity : AppCompatActivity(), CoroutineScope {

    ////////// Class member declaration.

    private val videoUri: Uri by lazy { intent.getParcelableExtra(IntroActivity.VIDEO_URI)!! }
    internal val videoDuration: Int by lazy { GetVideoData.getDuration(this, videoUri) }
    internal val videoFrameCount: Long by lazy { GetVideoData.getFrameCount(this, videoUri) }

    private lateinit var testModelFile: File

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

    ////////// IMPORTANT: data bind event handlers are here.

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

    ////////// UI updaters.

    internal fun goProjectButtonEnableCheck(start: Long, end: Long) {
        val seekDur = end - start
        val lengthLimit = resources.getInteger(R.integer.length_limit)
        gotoProjectActivity.isEnabled = (seekDur <= lengthLimit)
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

    ////////// Overrides.

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

        // Take permission to R/W external storage.
        takePermission(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE))

        val modelName = "test_txt.txt"
        testModelFile = File(filesDir, modelName)
        // Set click handlers.
        backButton.setOnClickListener { onBackPressed() }
        gotoProjectActivity.setOnClickListener { startTrimming() }
        framePlus.setOnClickListener { moveSelectedFrameIndexBy(1) }
        frameMinus.setOnClickListener { moveSelectedFrameIndexBy(-1) }

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

    private fun moveSelectedFrameIndexBy(amount: Int) {
        player.playWhenReady = false
        when (rangeSeekBarView.currentThumbIndex) {
            0 -> dataBinder.rangeStartIndex += amount
            1 -> dataBinder.rangeExclusiveEndIndex += amount
        }
    }

    private fun startTrimming() {
        val inPath = AbsolutePathRetriever.getPath(this, videoUri) ?: return
        val outPath = run {
            // Set destination location.
            val parentFolder = getExternalFilesDir(null)!!
            parentFolder.mkdirs()
            val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"
            File(parentFolder, fileName)
        }.absolutePath
        FFmpegDelegate.trimVideo(inPath, dataBinder.startMs, dataBinder.endMs, outPath) { i ->
            if (i == Config.RETURN_CODE_SUCCESS)
                Intent(this, ProjectActivity::class.java).apply {
                    println(outPath)
                    putExtra(VIDEO_PATH, outPath)
                    putExtra(
                        VIDEO_FRAME_COUNT,
                        dataBinder.rangeExclusiveEndIndex - dataBinder.rangeStartIndex
                    )
                }.also { startActivity(it) }
        }
    }

    ////////// Permission checking functions.

    private fun hasAllPermission(permission: Array<String>): Boolean =
        permission.all { ActivityCompat.checkSelfPermission(this, it) == PERMISSION_GRANTED }

    private fun requestPermission(permission: Array<String>) =
        ActivityCompat.requestPermissions(this, permission, 1)

    private fun takePermission(permission: Array<String>) {
        if (hasAllPermission(permission)) return

        if (permission.any { ActivityCompat.shouldShowRequestPermissionRationale(this, it) }) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Permission is needed to read & write the video.")
            builder.setPositiveButton(android.R.string.ok) { _, _ -> requestPermission(permission) }
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.show()
        } else {
            requestPermission(permission)
        }
    }

    ////////// Companion codes.

    companion object {
        /**
         * Uri of the trimmed video.
         */
        const val VIDEO_PATH: String = "TRIMMED_PATH"

        /**
         * Uri of the trimmed video.
         */
        const val VIDEO_FRAME_COUNT: String = "TRIMMED_FRAME_COUNT"

        /**
         * Path of the core model
         */
        @Suppress("unused")
        const val MODEL_PATH: String = "MODEL_PATH"
    }

    ////////// Coroutine codes.

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
