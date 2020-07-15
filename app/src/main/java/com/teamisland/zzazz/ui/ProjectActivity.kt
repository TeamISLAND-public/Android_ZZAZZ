package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.tabs.TabLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.BitmapVideo
import com.teamisland.zzazz.utils.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class ProjectActivity : AppCompatActivity() {

    private lateinit var uri: Uri
    private lateinit var video: BitmapVideo
    private lateinit var bitmapList: ArrayList<Bitmap>
    private var maxFrame by Delegates.notNull<Int>()
    private var fps by Delegates.notNull<Long>()
    private lateinit var playing: Job
    private var frame: Int = 0

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        fps = intent.getIntExtra(TrimmingActivity.VIDEO_FPS, 0).toLong()
        val duration = intent.getIntExtra(TrimmingActivity.VIDEO_DUR, 0)
        maxFrame = (duration / 1000 * fps).toInt()
        bitmapList = ArrayList(maxFrame + 1)
        val trimmedUri: Uri = intent.getParcelableExtra(TrimmingActivity.VIDEO_URI)
        uri = FileProvider.getUriForFile(
            this,
            "com.teamisland.zzazz.fileprovider",
            File(trimmedUri.path)
        )

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(this, uri)

        for (i in 1..maxFrame) {
            bitmapList.add(mediaMetadataRetriever.getFrameAtIndex(i))
        }

        video = BitmapVideo(fps, bitmapList, video_display, project_play)

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

        back.setOnClickListener { onBackPressed() }
    }

    // play video
    private fun playBitmap() {
        video.seekTo(0)
        video.start()
        project_play.isSelected = true
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
                val size = bitmapList[frame].rowBytes * bitmapList[frame].height
                val bytes = ByteBuffer.allocate(size)
                bitmapList[frame].copyPixelsToBuffer(bytes)
                val byteArray = ByteArray(size)
                bytes.get(byteArray, 0, byteArray.size)

                output.write(byteArray)
                frame++
            }
        }

        return file
    }
}
