package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.tabs.TabLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.Effect
import com.teamisland.zzazz.utils.FragmentPagerAdapter
import com.teamisland.zzazz.utils.GetVideoData.getDuration
import com.teamisland.zzazz.utils.ProjectAlertDialog
import com.teamisland.zzazz.utils.SaveProjectActivity
import com.teamisland.zzazz.utils.UnitConverter.float2DP
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.custom_tab.view.*
import kotlin.math.abs
import kotlin.properties.Delegates

/**
 * Activity for make project
 */
class ProjectActivity : AppCompatActivity() {

    private lateinit var uri: Uri
    private var frame = 0
    private lateinit var fadeOut: Animation

    //    private lateinit var video: BitmapVideo
//    private lateinit var bitmapList: List<Bitmap>
//    private var startFrame by Delegates.notNull<Int>()
//    private var endFrame by Delegates.notNull<Int>()
    private var fps by Delegates.notNull<Long>()

    private var videoDuration = 0

    companion object {
        /**
         * List of effect
         */
        var effectList: MutableList<Effect> = mutableListOf()

        /**
         * Temporary list of effect
         */
        var tempList: MutableList<Effect> = mutableListOf()

        /**
         * Check the project is saved
         */
        const val IS_SAVED = 1
    }

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
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(this, uri)

        videoDuration = getDuration(this, uri)

        zoomLevelMax = videoDuration * float2DP(10f, resources) / 16
        zoomLevelMin = videoDuration * float2DP(10f, resources) / 1000

        zoomLevel = (zoomLevelMin + zoomLevelMax) / 2

        timeIndexView.videoLength = videoDuration
        projectTimeLineView.videoLength = videoDuration
        projectTimeLineView.videoUri = uri

        setZoomLevel()
        setCurrentTime(0)

        fps =
            1000L * mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                .toLong() / mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                .toLong()

//        Log.d("time", "start")
//        bitmapList = mediaMetadataRetriever.getFramesAtIndex(startFrame, endFrame - startFrame + 1)
//        Log.d("time", "end")
//        mediaMetadataRetriever.release()
//        video = BitmapVideo(this, fps, bitmapList, video_display, project_play)
        playVideo()

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
                    frame = (video_display.currentPosition * fps).toInt()
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
                    onBackPressed()
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
                    effectList.addAll(tempList)
                    sortEffect()
                    tempList.clear()
                    Log.d("add", "${effectList.size}")
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
                    video_display.pause()
                    project_play.isActivated = false
                    Intent(this, SaveProjectActivity::class.java).apply {
                        startActivityForResult(this, IS_SAVED)
                    }
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

        sliding_view.setOnTouchListener { _, event -> tabLayoutOnTouchEvent(event) }
    }

    private fun setZoomLevel() {
        projectTimeLineView.pixelInterval = zoomLevel
        timeIndexView.pixelInterval = zoomLevel
    }

    /**
     * [AppCompatActivity.onBackPressed]
     */
    override fun onBackPressed() {
        if (slide.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            effect_back.alpha = 1F
            slide.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
            project_title.text = getString(R.string.project_title)
            tempList.clear()
            Log.d("add", "${effectList.size}")
        } else {
            val builder = ProjectAlertDialog(this) { super.onBackPressed() }
            builder.create()
            builder.show()
        }
    }

    // play video
    @SuppressLint("ClickableViewAccessibility")
    private fun playVideo() {
        video_display.setMediaController(null)
        video_display.setVideoURI(uri)
        video_display.requestFocus()
        video_display.start()
        video_display.postDelayed({ videoBinder() }, 500)
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
                        videoBinder()
                    }
                    project_play.alpha = 1F
                    project_play.startAnimation(fadeOut)
                }
            }
            true
        }
    }

    private fun videoBinder() = Thread(Runnable {
        do {
            try {
                setCurrentTime(video_display.currentPosition.coerceIn(0, videoDuration))
                Thread.sleep(10)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        } while (video_display.isPlaying)
    }).start()

    // make effect tab
    private fun tabInit() {
        with(effect_tab) {
            addTab(newTab().setCustomView(createTabView(getString(R.string.head_effect))))
            addTab(newTab().setCustomView(createTabView(getString(R.string.left_arm_effect))))
            addTab(newTab().setCustomView(createTabView(getString(R.string.right_arm_effect))))
            addTab(newTab().setCustomView(createTabView(getString(R.string.left_leg_effect))))
            addTab(newTab().setCustomView(createTabView(getString(R.string.right_leg_effect))))
        }

        val pagerAdapter = FragmentPagerAdapter(supportFragmentManager, 5, frame)
        effect_view_pager.adapter = pagerAdapter
        val tabView = effect_tab.getTabAt(0) ?: throw NoSuchElementException("No tab at index 0.")
        tabView.view.tab_text.typeface =
            ResourcesCompat.getFont(applicationContext, R.font.archivo_bold)
        tabView.view.tab_text.setTextColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.White
            )
        )
        effect_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                effect_view_pager.currentItem = (tab ?: return).position
                tab.view.tab_text.typeface =
                    ResourcesCompat.getFont(applicationContext, R.font.archivo_bold)
                tab.view.tab_text.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.White
                    )
                )
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                (tab ?: return).view.tab_text.typeface =
                    ResourcesCompat.getFont(applicationContext, R.font.archivo_regular)
                tab.view.tab_text.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.ContentsText80
                    )
                )
            }

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })
        effect_view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(effect_tab))
    }

    private fun createTabView(tabName: String): View? {
        val tabView = LayoutInflater.from(applicationContext).inflate(R.layout.custom_tab, null)
        val textView = tabView.findViewById<TextView>(R.id.tab_text)
        textView.text = tabName
        return tabView
    }

    private fun saveProject() {

    }

    /**
     * [AppCompatActivity.onActivityResult]
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IS_SAVED) {
            if (resultCode == Activity.RESULT_OK) {
                val projectName = data?.getStringExtra(SaveProjectActivity.PROJECT_NAME)
                project_title.text = projectName
                Log.d("filename", projectName!!)
                saveProject()
            }
        }
    }

    private fun sortEffect() {
//        val effect = effectList[start]
//        var left = start + 1
//        var right = end
//
//        while (left <= right) {
//            while (effectList[left].getStartFrame() < effect.getStartFrame() || (effectList[left].getStartFrame() == effect.getStartFrame()) and (effectList[left].getEndFrame() < effect.getEndFrame())) {
//                left++
//            }
//            while (effectList[right].getStartFrame() > effect.getStartFrame() || (effectList[right].getStartFrame() == effect.getStartFrame()) and (effectList[left].getEndFrame() > effect.getEndFrame())) {
//                right--
//            }
//            if (left <= right) {
//                val temp = effectList[left]
//                effectList[left] = effectList[right]
//                effectList[right] = temp
//            }
//        }
//
//        if(start < end) {
//            val temp = effectList[start]
//            effectList[start] = effectList[right]
//            effectList[right] = temp
//
//            sortEffect(start, right - 1)
//            sortEffect(right + 1, end)
//        }
        for (i in 0 until effectList.size) {
            for (j in i until effectList.size) {
                if ((effectList[j].getStartFrame() < effectList[i].getStartFrame() || (effectList[j].getStartFrame() == effectList[i].getStartFrame()) and (effectList[j].getEndFrame() < effectList[i].getEndFrame()))) {
                    val effect = effectList[i]
                    effectList[i] = effectList[j]
                    effectList[j] = effect
                }
            }
        }
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

    private fun setCurrentTime(i: Int) {
        projectTimeLineView.currentTime = i
        timeIndexView.currentTime = i
    }

    private var posX1 = 0f
    private var posX2 = 0f
    private var mode = 0
    private var newDist = 0f
    private var oldDist = 0f
    private var zoomLevel = 0f
    private var zoomLevelMax = 0f
    private var zoomLevelMin = 0f

    private fun tabLayoutOnTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action?.and(MotionEvent.ACTION_MASK)) {
            MotionEvent.ACTION_DOWN -> {
                posX1 = event.x
                mode = 1
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (mode == 2)
                    projectTimeLineView.refreshSize()
                mode = 0
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = 2
                newDist = distance(event)
                oldDist = distance(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == 1) {
                    posX2 = event.x
                    val delta = (posX2 - posX1) * videoDuration / zoomLevel
                    val time = (video_display.currentPosition - delta).toInt()
                        .coerceIn(0, videoDuration)
                    video_display.seekTo(time)
                    setCurrentTime(time)
                    posX1 = posX2
                } else if (mode == 2) {
                    newDist = distance(event)
                    zoomLevel = (zoomLevel * newDist / oldDist).coerceIn(zoomLevelMin, zoomLevelMax)
                    setZoomLevel()
                    oldDist = newDist
                }
            }
        }
        return true
    }

    private fun distance(event: MotionEvent): Float {
        return abs(event.getX(0) - event.getX(1))
    }
}
