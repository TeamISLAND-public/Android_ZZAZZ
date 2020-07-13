package com.teamisland.zzazz.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Range
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.GetVideoData
import com.teamisland.zzazz.utils.IPositionChangeListener
import com.teamisland.zzazz.video_trimmer_library.interfaces.OnRangeSeekBarListener
import com.teamisland.zzazz.video_trimmer_library.interfaces.VideoTrimmingListener
import com.teamisland.zzazz.video_trimmer_library.utils.TrimVideoUtils
import com.teamisland.zzazz.video_trimmer_library.view.RangeSeekBarView
import kotlinx.android.synthetic.main.activity_trimming.*
import java.io.File

/**
 * Activity for video trimming.
 */
class TrimmingActivity : AppCompatActivity(), VideoTrimmingListener, OnRangeSeekBarListener,
    IPositionChangeListener {

    private lateinit var videoUri: Uri
    private var videoDuration: Int = 0
    private var videoFps: Int = 0

    private fun stopVideo() {
        playButton.isActivated = false
        mainVideoView.pause()
    }

    private fun testVideoPositionInRange(): Boolean {
        val now = mainVideoView.currentPosition
        with(rangeSeekBarView) {
            return Range(getStart(), getEnd()).contains(now)
        }
    }

    private fun testVideoRange() {
        val start = rangeSeekBarView.getStart()
        val end = rangeSeekBarView.getEnd()
        if (start > mainVideoView.currentPosition) {
            currentPositionView.setMarkerPos(start * 100.0 / videoDuration)
            mainVideoView.seekTo(start)
        }
        if (end < mainVideoView.currentPosition) {
            currentPositionView.setMarkerPos(end * 100.0 / videoDuration)
            mainVideoView.seekTo(end)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimming)
        takePermission()

        videoUri = intent.getParcelableExtra(IntroActivity.VIDEO_URI) ?: return
        videoDuration = GetVideoData.getDuration(this, videoUri)
        videoFps = GetVideoData.getFPS(this, videoUri)

        val parentFolder = getExternalFilesDir(null) ?: return
        parentFolder.mkdirs()
        val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"
        val trimmedVideoFile = File(parentFolder, fileName)

        backButton.setOnClickListener { onBackPressed() }

        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        fadeOut.startOffset = 1000
        fadeOut.duration = 500
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            /**
             *
             * Notifies the repetition of the animation.
             *
             * @param animation The animation which was repeated.
             */
            override fun onAnimationRepeat(animation: Animation?) {
                playButton.visibility = VISIBLE
            }

            /**
             *
             * Notifies the end of the animation. This callback is not invoked
             * for animations with repeat count set to INFINITE.
             *
             * @param animation The animation which reached its end.
             */
            override fun onAnimationEnd(animation: Animation?) {
                playButton.visibility = GONE
            }

            /**
             *
             * Notifies the start of the animation.
             *
             * @param animation The started animation.
             */
            override fun onAnimationStart(animation: Animation?) {
                playButton.visibility = VISIBLE
            }
        })

        playButton.setOnClickListener {
            playButton.startAnimation(fadeOut)
            if (playButton.isActivated) {
                playButton.isActivated = false
                mainVideoView.pause()
            } else {
                playButton.isActivated = true
                if (!testVideoPositionInRange())
                    mainVideoView.seekTo(rangeSeekBarView.getStart())
                mainVideoView.start()
                Thread(Runnable {
                    do {
                        val now = mainVideoView.currentPosition
                        with(currentPositionView) {
                            post {
                                setMarkerPos(now.toDouble() * 100 / videoDuration)
                            }
                        }
                        if (!testVideoPositionInRange())
                            stopVideo()
                        try {
                            Thread.sleep(10)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    } while (mainVideoView.isPlaying)
                }).start()
            }
        }
        playButton.startAnimation(fadeOut)

        rangeSeekBarView.setButtons(framePlus, frameMinus)
        rangeSeekBarView.addOnRangeSeekBarListener(this)
        rangeSeekBarView.initMaxWidth()
        rangeSeekBarView.setDuration(videoDuration)
        rangeSeekBarView.setFPS(videoFps)

        currentPositionView.setDuration(videoDuration)
        currentPositionView.setListener(this)
        currentPositionView.setRange(rangeSeekBarView)

        selectedThumbView.setRange(rangeSeekBarView)

        timeLineView.setVideo(videoUri)

        mainVideoView.setVideoURI(videoUri)
        mainVideoView.setOnPreparedListener {
            mainVideoView.start()
            mainVideoView.postDelayed({ mainVideoView.pause() }, 100)
        }
        mainVideoView.setOnCompletionListener {
            playButton.isActivated = false
        }
        mainVideoView.setOnClickListener { playButton.startAnimation(fadeOut) }

        gotoProjectActivity.setOnClickListener {
            TrimVideoUtils.startTrim(
                this,
                videoUri,
                trimmedVideoFile,
                rangeSeekBarView.getStart().toLong(),
                rangeSeekBarView.getEnd().toLong(),
                GetVideoData.getDuration(this, videoUri).toLong(),
                this
            )
        }
    }

    private fun applyTrimRangeChanges() {
        val seekDur = rangeSeekBarView.getEnd() - rangeSeekBarView.getStart()
        if (seekDur <= 60000) {
            val totalSeconds = seekDur / 1000
            val seconds = totalSeconds % 60
            val minutes = totalSeconds / 60 % 60
            trimText.text =
                java.util.Formatter().format("%02d:%02d Trimmed", minutes, seconds).toString()
            gotoProjectActivity.isEnabled = true
        } else {
            trimText.text = getString(R.string.video_too_long)
            gotoProjectActivity.isEnabled = false
        }
    }

    private fun hasNoPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this@TrimmingActivity,
            permission
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permissionArray: Array<String>) {
        ActivityCompat.requestPermissions(
            this@TrimmingActivity,
            permissionArray,
            1
        )
    }

    private fun takePermission() {
        if (hasNoPermission(READ_EXTERNAL_STORAGE) || hasNoPermission(WRITE_EXTERNAL_STORAGE)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                val builder = AlertDialog.Builder(this@TrimmingActivity)
                builder.setMessage("Permission is needed to read & write the video.")
                builder.setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                    requestPermission(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE))
                }
                builder.setNegativeButton(android.R.string.cancel, null)
                builder.show()
            } else {
                requestPermission(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE))
            }
        }
    }

    /**[VideoTrimmingListener]*/

    override fun onVideoPrepared() {
        Toast.makeText(this, "Video prepared.", LENGTH_SHORT).show()
    }

    override fun onTrimStarted() {
        Toast.makeText(this, "Trim started.", LENGTH_SHORT).show()
    }

    /**
     * @param uri the result, trimmed video, or null if failed
     */
    override fun onFinishedTrimming(uri: Uri?) {
        Toast.makeText(this, "$uri", LENGTH_SHORT).show()
        finish()
        Intent(this, ProjectActivity::class.java).also {
            it.putExtra(VIDEO_FPS, videoFps)
            it.putExtra(VIDEO_DUR, GetVideoData.getDuration(this, uri!!))
            it.putExtra(VIDEO_URI, uri)
            startActivity(it)
        }
    }

    companion object {
        /**
         * Uri of the trimmed video.
         */
        const val VIDEO_URI: String = "URI"

        /**
         * FPS of the trimmed video.
         */
        const val VIDEO_FPS: String = "FPS"

        /**
         * Duration of the trimmed video.
         */
        const val VIDEO_DUR: String = "DUR"
    }

    /**
     * check [android.media.MediaPlayer.OnErrorListener]
     */
    override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
        Toast.makeText(this, "Error while trimming.", LENGTH_SHORT).show()
    }

    /**[OnRangeSeekBarListener]*/

    override fun onCreate(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
        println("onCreate")
        applyTrimRangeChanges()
    }

    override fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
        println("onSeek")
        applyTrimRangeChanges()
        testVideoRange()
        stopVideo()
    }

    override fun onSeekStart(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
        println("onSeekStart")
        applyTrimRangeChanges()
    }

    override fun onSeekStop(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
        println("onSeekStop")
        applyTrimRangeChanges()
    }

    override fun onDeselect(rangeSeekBarView: RangeSeekBarView) {
        //do nothing.
    }

    /**[IPositionChangeListener]*/

    /**
     * An event when current position changed.
     */
    override fun onChange(percentage: Double) {
        mainVideoView.seekTo((percentage * videoDuration / 100).toInt())
        stopVideo()
    }
}
