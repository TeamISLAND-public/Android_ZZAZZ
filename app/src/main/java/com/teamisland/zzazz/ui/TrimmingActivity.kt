package com.teamisland.zzazz.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
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
class TrimmingActivity : AppCompatActivity() {

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

    private lateinit var videoUri: Uri
    internal var videoDuration: Int = 0
    internal var videoFps: Int = 0

    internal fun stopVideo() {
        playButton.isActivated = false
        mainVideoView.pause()
    }

    private fun testVideoPositionInRange(): Boolean {
        val now = mainVideoView.currentPosition
        with(rangeSeekBarView) {
            return Range(getStart(), getEnd()).contains(now)
        }
    }

    internal fun testVideoRange() {
        val range = rangeSeekBarView.getRange()
        if (mainVideoView.currentPosition !in range) {
            val desiredPos = range.clamp(mainVideoView.currentPosition)
            currentPositionView.setMarkerPos(desiredPos * 100.0 / videoDuration)
            mainVideoView.seekTo(desiredPos)
        }
    }

    /**
     * [AppCompatActivity.onCreate]
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimming)
        takePermission(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE))

        if (setupVideoProperties()) return

        val parentFolder = getExternalFilesDir(null) ?: return
        parentFolder.mkdirs()
        val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"
        val trimmedVideoFile = File(parentFolder, fileName)

        backButton.setOnClickListener { onBackPressed() }

        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        fadeOut.startOffset = 1000
        fadeOut.duration = 500
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                playButton.visibility = VISIBLE
            }

            override fun onAnimationEnd(animation: Animation?) {
                playButton.visibility = GONE
            }

            override fun onAnimationStart(animation: Animation?) {
                playButton.visibility = VISIBLE
            }
        })

        playButton.setOnClickListener { playButtonClickHandler(fadeOut) }
        playButton.startAnimation(fadeOut)

        rangeSeekBarView.setButtons(framePlus, frameMinus)
        rangeSeekBarView.addOnRangeSeekBarListener(object : OnRangeSeekBarListener {
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
        })
        rangeSeekBarView.initMaxWidth()
        rangeSeekBarView.setDuration(videoDuration)
        rangeSeekBarView.setFPS(videoFps)

        currentPositionView.setDuration(videoDuration)
        currentPositionView.setListener(object : IPositionChangeListener {
            /**
             * An event when current position changed.
             */
            override fun onChange(percentage: Double) {
                mainVideoView.seekTo((percentage * videoDuration / 100).toInt())
                stopVideo()
            }
        })
        currentPositionView.setRange(rangeSeekBarView)

        selectedThumbView.setRange(rangeSeekBarView)

        timeLineView.setVideo(videoUri)

        mainVideoView.setVideoURI(videoUri)
        mainVideoView.setOnPreparedListener {
            mainVideoView.start()
            mainVideoView.postDelayed({ mainVideoView.pause() }, 100)
        }
        mainVideoView.setOnCompletionListener { playButton.isActivated = false }
        mainVideoView.setOnClickListener { playButton.startAnimation(fadeOut) }

        gotoProjectActivity.setOnClickListener {
            TrimVideoUtils.startTrim(
                this,
                videoUri,
                trimmedVideoFile,
                rangeSeekBarView.getStart().toLong(),
                rangeSeekBarView.getEnd().toLong(),
                GetVideoData.getDuration(this, videoUri).toLong(),
                object : VideoTrimmingListener {
                    override fun onVideoPrepared() {
                    }

                    override fun onTrimStarted() {
                    }

                    override fun onFinishedTrimming(uri: Uri?) {
                        Toast.makeText(this@TrimmingActivity, "$uri", LENGTH_SHORT).show()
                        finish()
                        Intent(this@TrimmingActivity, ProjectActivity::class.java).also {
                            it.putExtra(VIDEO_FPS, videoFps)
                            it.putExtra(
                                VIDEO_DUR,
                                GetVideoData.getDuration(this@TrimmingActivity, uri ?: return@also)
                            )
                            it.putExtra(VIDEO_URI, uri)
                            startActivity(it)
                        }
                    }

                    override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
                        Toast.makeText(this@TrimmingActivity, "Error while trimming.", LENGTH_SHORT)
                            .show()
                    }
                }
            )
        }
    }

    private fun setupVideoProperties(): Boolean {
        videoUri = intent.getParcelableExtra(IntroActivity.VIDEO_URI) ?: return true
        videoDuration = GetVideoData.getDuration(this, videoUri)
        videoFps = GetVideoData.getFPS(this, videoUri)
        return false
    }

    private fun playButtonClickHandler(fadeOut: Animation) {
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
                        setMarkerPos(now * 100.0 / videoDuration)
                    }
                    if (rangeSeekBarView.getEnd() < mainVideoView.currentPosition)
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

    internal fun applyTrimRangeChanges() {
        val seekDur = rangeSeekBarView.getEnd() - rangeSeekBarView.getStart()
        if (seekDur <= resources.getInteger(R.integer.length_limit)) {
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

    private fun hasAllPermission(permission: Array<String>): Boolean {
        return permission.all {
            ActivityCompat.checkSelfPermission(this, it) == PERMISSION_GRANTED
        }
    }

    private fun requestPermission(permission: Array<String>) {
        ActivityCompat.requestPermissions(this, permission, 1)
    }

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
}
