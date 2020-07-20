package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.BitmapVideo
import com.teamisland.zzazz.utils.FragmentPagerAdapter
import com.teamisland.zzazz.utils.VideoManager
import kotlinx.android.synthetic.main.activity_project.*
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

/**
 * Activity for make project
 */
class ProjectActivity : AppCompatActivity() {

    private lateinit var uri: Uri
    private lateinit var video: BitmapVideo
    private lateinit var bitmapList: List<Bitmap>
    private var startFrame by Delegates.notNull<Int>()
    private var endFrame by Delegates.notNull<Int>()
    private var fps by Delegates.notNull<Long>()

    /**
     * [AppCompatActivity.onCreate]
     */
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "ControlFlowWithEmptyBody")
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        val path = intent.getStringExtra(TrimmingActivity.VIDEO_PATH)
        uri = Uri.parse(path)
        startFrame = intent.getIntExtra(TrimmingActivity.VIDEO_START_FRAME, 0)
        endFrame = intent.getIntExtra(TrimmingActivity.VIDEO_END_FRAME, 0)
        bitmapList = ArrayList(endFrame - startFrame + 1)

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(this, uri)

        fps =
            1000L * mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                .toLong() / mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                .toLong()
//        val videoManager = VideoManager(mediaMetadataRetriever, startFrame, endFrame)
//        videoManager.runThread(startFrame, endFrame)
//
//        Thread {
//            while (!videoManager.isTerminated()) {
//                Log.d("thread", "is not terminated")
//            }
//            bitmapList = videoManager.sortArrayList()
//            mediaMetadataRetriever.release()
//
//            video = BitmapVideo(this, fps, bitmapList, video_display, project_play)
//            playBitmap()
//        }.start()
        Log.d("time", "start")
        bitmapList = mediaMetadataRetriever.getFramesAtIndex(startFrame, endFrame - startFrame + 1)
        Log.d("time", "end")
        mediaMetadataRetriever.release()
        video = BitmapVideo(this, fps, bitmapList, video_display, project_play)
        playBitmap()

        slide.anchorPoint = 1F
        slide.getChildAt(1).setOnClickListener(null)
        slide.panelState = SlidingUpPanelLayout.PanelState.HIDDEN

        tabInit()

        add_effect_button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    add_effect_button.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    add_effect_button.alpha = 1F
                    slide.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
                    project_title.text = getString(R.string.add_effect)
//                    video.pause()
                }
            }
            true
        }

        effect_back.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    effect_back.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    effect_back.alpha = 1F
                    slide.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
                    project_title.text = getString(R.string.project_title)
                }
            }
            true
        }

        effect_done.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    effect_done.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    effect_done.alpha = 1F
                    slide.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
                    project_title.text = getString(R.string.project_title)
                }
            }
            true
        }

        save_project.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    save_project.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    saveProject()
                    save_project.alpha = 1F
                }
            }
            true
        }

        gotoExportActivity.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    gotoExportActivity.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    gotoExportActivity.alpha = 1F
                    video.pause()
                    Intent(this, ExportActivity::class.java).apply {
//                        putExtra("URI", exportProject())
                        startActivity(this)
                    }
                }
            }
            true
        }

        back.setOnClickListener { onBackPressed() }
    }

    // play video
    @SuppressLint("ClickableViewAccessibility")
    private fun playBitmap() {
        video.seekTo(0)
        video.start()
        project_play.isSelected = true
        project_play.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    project_play.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    video.isPlaying = if (video.isPlaying) {
                        video.pause()
                        false
                    } else {
                        video.start()
                        true
                    }
                    project_play.alpha = 1F
                }
            }
            true
        }
    }

    // make effect tab
    private fun tabInit() {
        effect_tab.addTab(effect_tab.newTab().setText(getString(R.string.head_effect)))
        effect_tab.addTab(effect_tab.newTab().setText(getString(R.string.left_arm_effect)))

        val pagerAdapter =
            FragmentPagerAdapter(
                supportFragmentManager,
                3
            )
        effect_view_pager.adapter = pagerAdapter

        effect_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                effect_view_pager.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        effect_view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(effect_tab))
    }

    private fun saveProject() {

    }

    // export project to export activity
    @Suppress("BlockingMethodInNonBlockingContext")
    @SuppressLint("SimpleDateFormat")
    private fun exportProject(): Uri {
        val time = System.currentTimeMillis()
        val date = Date(time)
        val nameFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = nameFormat.format(date)
        val file = filesDir.absoluteFile.path + "/$filename.mp4"
        val output = FileOutputStream(File(file))

        var frame = 0
        Thread {
            while (frame <= endFrame - startFrame + 1) {
                // convert bitmap to bytes
                val size = bitmapList[frame].rowBytes * bitmapList[frame].height
                val bytes = ByteBuffer.allocate(size)
                bitmapList[frame++].copyPixelsToBuffer(bytes)
                val byteArray = ByteArray(size)
                // error
                bytes.get(byteArray, 0, byteArray.size)

                output.write(byteArray)
            }
        }.start()

        return Uri.parse(file)
    }
}
