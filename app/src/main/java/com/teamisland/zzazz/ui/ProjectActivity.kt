package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class ProjectActivity : AppCompatActivity() {

    private lateinit var uri: Uri
    private lateinit var video: ArrayList<Bitmap>
    private var maxFrame by Delegates.notNull<Int>()
    private var fps by Delegates.notNull<Long>()
    private lateinit var playing: Job
    private var isPlaying: Boolean = true
    private var frame: Int = 0

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

//        fps = intent.getLongExtra("fps")
//        maxFrame = intent.getIntExtra("frame")
//        uri = intent.getParcelableExtra("Uri")

        fps = 30L
        maxFrame = 5184
        video = ArrayList(maxFrame + 1)
//        uri = Uri.parse(intent.getStringExtra("uri"))
        uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.test_5s)

        val mediaMetadataRetriever = MediaMetadataRetriever()
//        mediaMetadataRetriever.setDataSource(uri.path)
        for (i in 1..maxFrame + 1) {
            video.add(
                mediaMetadataRetriever.getFrameAtTime(
                    (1000000 * i).toLong(),
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
            )
        }

        project_back.setImageDrawable(getDrawable(R.drawable.video_back))
        project_back.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    project_back.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    project_back.alpha = 1F
                }
            }
            true
        }

        project_next.setImageDrawable(getDrawable(R.drawable.video_next))
        project_next.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    project_next.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    project_next.alpha = 1F
                }
            }
            true
        }

        var end = false
        project_play.setImageDrawable(getDrawable(R.drawable.video_play_small))
        project_play.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    project_play.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    isPlaying = if (isPlaying) {
                        if (end) {
                            end = false
                        }
                        project_play.setImageDrawable(getDrawable(R.drawable.video_pause_small))
                        false
                    } else {
                        project_play.setImageDrawable(getDrawable(R.drawable.video_play_small))
                        true
                    }
                    project_play.alpha = 1F
                }
            }
            true
        }

        slide.anchorPoint = 0.3f
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
                }
            }
            true
        }

        check_effect.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    check_effect.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    check_effect.alpha = 1F
                    slide.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
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

        playBitmap()

        val intent = Intent(this, ExportActivity::class.java)
        gotoExportActivity.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    gotoExportActivity.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    gotoExportActivity.alpha = 1F
                    playing.cancel()
//                    intent.putExtra("Uri", exportProject())
                    startActivity(intent)
                }
            }
            true
        }
    }

    // play video
    private fun playBitmap() {
        val handler = Handler()
        playing = GlobalScope.launch {
            while (isPlaying) {
                if (frame == maxFrame) {
                    isPlaying = false
                    handler.post {
                        project_play.setImageDrawable(getDrawable(R.drawable.video_play_small))
                    }
                    frame = 0
                } else {
                    handler.postDelayed(Runnable {
//                        video_display.setImageBitmap(video[frame])
                    }, 1000 / fps)
                    frame++
                }
            }
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
    private fun exportProject(): String {
        val time = System.currentTimeMillis()
        val date = Date(time)
        val nameFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = nameFormat.format(date)
        val file = filesDir.absoluteFile.path + "/$filename.mp4"
        val output = FileOutputStream(File(file))

        frame = 0
        GlobalScope.launch {
            while (frame <= maxFrame) {
                // convert bitmap to bytes
                val size = video[frame].rowBytes * video[frame].height
                val bytes = ByteBuffer.allocate(size)
                video[frame].copyPixelsToBuffer(bytes)
                val byteArray = ByteArray(size)
                bytes.get(byteArray, 0, byteArray.size)

                output.write(byteArray)
                frame++
            }
        }

        return file
    }
}
