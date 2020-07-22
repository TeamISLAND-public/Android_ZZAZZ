package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.tabs.TabLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.custom_tab.view.*

/**
 * Activity for make project
 */
class ProjectActivity : AppCompatActivity() {

    private lateinit var uri: Uri
    private lateinit var fadeOut: Animation
//    private lateinit var video: BitmapVideo
//    private lateinit var bitmapList: List<Bitmap>
//    private var startFrame by Delegates.notNull<Int>()
//    private var endFrame by Delegates.notNull<Int>()
//    private var fps by Delegates.notNull<Long>()

    /**
     * [AppCompatActivity.onRestart]
     */
    override fun onRestart() {
        super.onRestart()
        video_display.seekTo(0)
        video_display.start()
        project_play.isActivated = true
        project_play.startAnimation(fadeOut)
    }

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
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
//        startFrame = intent.getIntExtra(TrimmingActivity.VIDEO_START_FRAME, 0)
//        endFrame = intent.getIntExtra(TrimmingActivity.VIDEO_END_FRAME, 0)
//        bitmapList = ArrayList(endFrame - startFrame + 1)
//
//        val mediaMetadataRetriever = MediaMetadataRetriever()
//        mediaMetadataRetriever.setDataSource(this, uri)
//
//        fps =
//            1000L * mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
//                .toLong() / mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
//                .toLong()
//        Log.d("time", "start")
//        bitmapList = mediaMetadataRetriever.getFramesAtIndex(startFrame, endFrame - startFrame + 1)
//        Log.d("time", "end")
//        mediaMetadataRetriever.release()
//        video = BitmapVideo(this, fps, bitmapList, video_display, project_play)
        playBitmap()

        slide.anchorPoint = 1F
        slide.getChildAt(1).setOnClickListener(null)
        slide.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        slide.isTouchEnabled = false

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
//                    video.pause()
                    video_display.pause()
                    project_play.isActivated = false
                    Intent(this, ExportActivity::class.java).apply {
//                        putExtra("URI", exportProject())
                        putExtra("URI", uri)
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
        video_display.setMediaController(null)
        video_display.setVideoURI(uri)
        video_display.requestFocus()
        video_display.start()
        project_play.isActivated = true
//        video.seekTo(0)
//        video.start()
        fadeOut.startOffset = 1000
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                project_play.visibility = View.VISIBLE
            }

            // button needs to be vanished
            override fun onAnimationEnd(animation: Animation?) {
                project_play.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
                project_play.visibility = View.VISIBLE
            }
        })
        fadeOut.duration = 500

        var end = false
        project_play.startAnimation(fadeOut)

        video_display.setOnClickListener {
            project_play.startAnimation(fadeOut)
        }
        video_display.setOnCompletionListener {
            project_play.isActivated = false
            end = true
        }

        project_play.isSelected = true

        project_play.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    project_play.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
//                    video.isPlaying = if (video.isPlaying) {
//                        video.pause()
//                        false
//                    } else {
//                        video.start()
//                        true
//                    }
                    if (video_display.isPlaying) {
                        video_display.pause()
                        project_play.isActivated = false
                    } else {
                        if (end) {
                            video_display.seekTo(0)
                            end = false
                        }
                        video_display.start()
                        project_play.isActivated = true
                    }
                    project_play.alpha = 1F
                    project_play.startAnimation(fadeOut)
                }
            }
            true
        }
    }

    // make effect tab
    private fun tabInit() {
        effect_tab.addTab(effect_tab.newTab().setCustomView(createTabView(getString(R.string.head_effect))))
        effect_tab.addTab(effect_tab.newTab().setCustomView(createTabView(getString(R.string.left_arm_effect))))
        effect_tab.addTab(effect_tab.newTab().setCustomView(createTabView(getString(R.string.right_arm_effect))))
        effect_tab.addTab(effect_tab.newTab().setCustomView(createTabView(getString(R.string.left_leg_effect))))
        effect_tab.addTab(effect_tab.newTab().setCustomView(createTabView(getString(R.string.right_leg_effect))))

        val pagerAdapter =
            FragmentPagerAdapter(
                supportFragmentManager,
                5
            )
        effect_view_pager.adapter = pagerAdapter
        val tabView = effect_tab.getTabAt(0)
        tabView!!.view.tab_text.typeface = ResourcesCompat.getFont(applicationContext, R.font.archivo_bold)
        tabView.view.tab_text.setTextColor(ContextCompat.getColor(applicationContext, R.color.White))
        effect_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                effect_view_pager.currentItem = tab!!.position
                tab.view.tab_text.typeface = ResourcesCompat.getFont(applicationContext, R.font.archivo_bold)
                tab.view.tab_text.setTextColor(ContextCompat.getColor(applicationContext, R.color.White))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab!!.view.tab_text.typeface = ResourcesCompat.getFont(applicationContext, R.font.archivo_regular)
                tab.view.tab_text.setTextColor(ContextCompat.getColor(applicationContext, R.color.ContentsText80))
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        effect_view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(effect_tab))
    }

    @SuppressLint("InflateParams")
    private fun createTabView(tabName: String): View? {
        val tabView = LayoutInflater.from(applicationContext).inflate(R.layout.custom_tab, null)
        val textView = tabView.findViewById<TextView>(R.id.tab_text)
        textView.text = tabName
        return tabView
    }

    private fun saveProject() {

    }

    // export project to export activity
//    @Suppress("BlockingMethodInNonBlockingContext")
//    @SuppressLint("SimpleDateFormat")
//    private fun exportProject(): Uri {
//        val time = System.currentTimeMillis()
//        val date = Date(time)
//        val nameFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
//        val filename = nameFormat.format(date)
//        val file = filesDir.absoluteFile.path + "/$filename.mp4"
//        val output = FileOutputStream(File(file))
//
//        var frame = 0
//        Thread {
//            while (frame <= endFrame - startFrame + 1) {
//                 convert bitmap to bytes
//                val size = bitmapList[frame].rowBytes * bitmapList[frame].height
//                val bytes = ByteBuffer.allocate(size)
//                bitmapList[frame++].copyPixelsToBuffer(bytes)
//                val byteArray = ByteArray(size)
//                 error
//                bytes.get(byteArray, 0, byteArray.size)
//
//                output.write(byteArray)
//            }
//        }.start()
//
//        return Uri.parse(file)
//    }
}
