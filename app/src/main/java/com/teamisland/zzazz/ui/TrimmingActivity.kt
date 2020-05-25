package com.teamisland.zzazz.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lb.video_trimmer_library.interfaces.VideoTrimmingListener
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.VideoIntent
import kotlinx.android.synthetic.main.activity_trimming.*
import java.io.File

/**
 * Activity for video trimming.
 */
class TrimmingActivity : AppCompatActivity(), VideoTrimmingListener {

    private var video_uri: Uri? = null

    //    private var video_duration = 0
//
//    /**
//     * Starting position of the video in milliseconds.
//     */
//    private var _start_millisecond: Int = 0
//
//    /**
//     * Ending position of the video in milliseconds.
//     */
//    private var _end_millisecond: Int = 0
//
//    private var _stopper = false
//
//    private fun _get_ender_thread() = Thread(Runnable {
//        while (!_stopper) {
//            if (_end_millisecond <= video_view.currentPosition) {
//                video_view.pause()
//                _stopper = true
//            }
//            Thread.sleep(100)
//        }
//    })
//
//    private fun toggle_button_text(initial: Boolean = false) {
//        play_button.run {
//            if (text == "Start" || text == "Resume")
//                text = "Pause"
//            else if (initial)
//                text = "Play"
//            else
//                text = "Resume"
//        }
//    }
//
//    /**
//     * A function that sets up an interval of the video.
//     * @param l RangeSeekBar to setup an interval.
//     */
//    fun handler(l: RangeSeekBar) {
//        _start_millisecond = l.progressStart
//        _end_millisecond = l.progressEnd
//        Log.d("@@@", "$_start_millisecond $_end_millisecond")
//        video_view.run {
//            if (_end_millisecond < currentPosition) {
//                if (isPlaying)
//                    pause()
//                seekTo(_end_millisecond)
//            }
//            if (currentPosition < _start_millisecond) {
//                if (isPlaying)
//                    pause()
//                seekTo(_start_millisecond)
//            }
//        }
//    }
//
//    /**
//     * An object for range change listener.
//     */
//    private val rangeSeekBarChangeListener: RangeSeekBar.OnRangeSeekBarChangeListener = object :
//        RangeSeekBar.OnRangeSeekBarChangeListener {
//        override fun onProgressChanged(p0: RangeSeekBar, p1: Int, p2: Int, p3: Boolean) =
//            handler(p0)
//
//        override fun onStartTrackingTouch(p0: RangeSeekBar) = handler(p0)
//
//        override fun onStopTrackingTouch(p0: RangeSeekBar) = handler(p0)
//
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_trimming)
//
//        val intent = Intent(this, ProjectActivity::class.java)
//        // This should be edited.
//        // duration is duration of video, uri is uri parse of video
//        val value =
//            TrimToProjectValue(
//                5184,
//                "android.resource://" + packageName + "/" + R.raw.test_5s
//            )
//        intent.putExtra("value", value)
//        gotoProjectActivity.setOnClickListener { startActivity(intent) }
//
//        video_uri = intent.getParcelableExtra(getString(R.string.selected_video_uri)) ?: return
//        video_view.setVideoURI(video_uri)
//
//        video_view.setOnPreparedListener {
//            video_duration = video_view.duration
//            Log.d("@@@", "$video_duration")
//            _end_millisecond = video_duration
//
//            trim_range_bar.max = _end_millisecond
//            trim_range_bar.setProgress(0, _end_millisecond)
//            trim_range_bar.setOnRangeSeekBarChangeListener(rangeSeekBarChangeListener)
//
//            toggle_button_text(true)
//            play_button.setOnClickListener {
//                if (_end_millisecond <= video_view.currentPosition)
//                    video_view.seekTo(_start_millisecond)
//
//                toggle_button_text()
//
//                if (video_view.isPlaying) {
//                    _stopper = true
//                    video_view.pause()
//                } else {
//                    _stopper = false
//                    video_view.start()
//                    _get_ender_thread().start()
//                }
//            }
//
//            video_view.setOnCompletionListener {
//                toggle_button_text(true)
//            }
//        }
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimming)

        val intent = Intent(this, ProjectActivity::class.java)
        // This should be edited.
        // duration is duration of video, uri is uri parse of video
        val value =
            VideoIntent(
                5184,
                "android.resource://" + packageName + "/" + R.raw.test_5s
            )
        intent.putExtra("value", value)

        video_uri = getIntent().getParcelableExtra(getString(R.string.selected_video_uri))
        gotoProjectActivity.setOnClickListener { startActivity(intent) }

        videoTrimmer.setMaxDurationInMs(resources.getInteger(R.integer.length_limit))
        videoTrimmer.setOnK4LVideoListener(this)
        val parentFolder = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: return
        parentFolder.mkdirs()
        val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"
        val trimmedVideoFile = File(parentFolder, fileName)
        videoTrimmer.setDestinationFile(trimmedVideoFile)
        videoTrimmer.setVideoURI(video_uri ?: return)
        videoTrimmer.setVideoInformationVisibility(true)
    }

    override fun onVideoPrepared() {
        Toast.makeText(this@TrimmingActivity, "Video is prepared!", Toast.LENGTH_SHORT).show()
    }

    override fun onTrimStarted() {
        //trimmingProgressView.visibility = View.VISIBLE
    }

    /**
     * @param uri the result, trimmed video, or null if failed
     */
    override fun onFinishedTrimming(uri: Uri?) {
        //trimmingProgressView.visibility = View.GONE
        if (uri == null) {
            Toast.makeText(this@TrimmingActivity, "failed trimming", Toast.LENGTH_SHORT).show()
        } else {
            val msg = getString(R.string.video_saved_at, uri.path)
            Toast.makeText(this@TrimmingActivity, msg, Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setDataAndType(uri, "video/mp4")
            startActivity(intent)
        }
        finish()
    }

    /**
     * check {[android.media.MediaPlayer.OnErrorListener]}
     */
    override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
        //trimmingProgressView.visibility = View.GONE
        Toast.makeText(this@TrimmingActivity, "error while previewing video", Toast.LENGTH_SHORT)
            .show()
    }
}
