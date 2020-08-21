package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentCallbacks2
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Range
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.tabs.TabLayout
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.*
import com.teamisland.zzazz.utils.GetVideoData.getDuration
import com.teamisland.zzazz.utils.GetVideoData.getFrameCount
import com.teamisland.zzazz.utils.UnitConverter.float2DP
import com.unity3d.player.IUnityPlayerLifecycleEvents
import com.unity3d.player.UnityPlayer
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.custom_tab.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.math.max

/**
 * Activity for make project
 */
class ProjectActivity : AppCompatActivity(), IUnityPlayerLifecycleEvents {

    private lateinit var mUnityPlayer: UnityPlayer

    /**
     * Destroys the activity and finishes ongoing coroutines.
     */
    override fun onDestroy() {
        super.onDestroy()
        mUnityPlayer.destroy()
    }

    /**
     * Path of result video
     */
    private lateinit var resultPath: String
    private lateinit var path: String
    private lateinit var modelpath: String
    private var videoDuration = 0
    private var fps: Float = 0f
    private var frameCount: Int = 0

    //    private lateinit var video: BitmapVideo
//    private lateinit var bitmapList: List<Bitmap>
//    private var startFrame by Delegates.notNull<Int>()
//    private var endFrame by Delegates.notNull<Int>()

    companion object {
        /**
         * Current time of video in Unity.
         */
        var frame: Int = 0

        /**
         * Check is video playing.
         * When video stops, Unity set this variable to false.
         */
        var isPlaying: Boolean = false

        /**
         * List of effect
         */
        var effectList: MutableList<Effect> = mutableListOf()

        /**
         * Check the project is saved
         */
        const val IS_SAVED: Int = 1

        /**
         * Play manager object of video player in Unity.
         */
        const val PLAY_MANAGER: String = "PlayManager"

        /**
         * Frame visualizer of effect object of video player in Unity.
         */
        const val FRAME_VISUALIZER: String = "FrameVisualizer"

        /**
         * Method name of setting video in Unity
         */
        const val SET_VIDEO: String = "setVideo"

        /**
         * Method name of reading test data in Unity
         */
        const val READ_DATA: String = "ReadData"

        /**
         * Method name of exporting result video in Unity
         */
        const val EXPORT: String = "exportVideo"
    }

    /**
     * [AppCompatActivity.onRestart]
     */
    override fun onRestart() {
        super.onRestart()
        startVideo()
    }

    /**
     * Pause Unity.
     *
     * [AppCompatActivity.onPause]
     */
    override fun onPause() {
        super.onPause()
        mUnityPlayer.pause()
    }

    /**
     * Resume Unity.
     *
     * [AppCompatActivity.onResume]
     */
    override fun onResume() {
        super.onResume()
        mUnityPlayer.resume()
    }

    /**
     * [AppCompatActivity.onLowMemory]
     */
    override fun onLowMemory() {
        super.onLowMemory()
        mUnityPlayer.lowMemory()
    }

    /**
     * [AppCompatActivity.onTrimMemory]
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
            mUnityPlayer.lowMemory()
    }

    /**
     * [AppCompatActivity.onWindowFocusChanged]
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        mUnityPlayer.windowFocusChanged(hasFocus)
    }

    /**
     * [AppCompatActivity.onCreate]
     */
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "ControlFlowWithEmptyBody")
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        mUnityPlayer = UnityPlayer(this, this)

        resultPath = dataDir.absolutePath + "/result.mp4"
        path = intent.getStringExtra(TrimmingActivity.VIDEO_PATH)
//        modelpath = intent.getStringExtra(TrimmingActivity.MODEL_PATH)
        val uri = Uri.parse(path)
        videoDuration = getDuration(this, uri)
        frameCount = getFrameCount(this, uri).toInt()
        fps = frameCount / (videoDuration / 1000f)

        zoomLevel = float2DP(0.06f, resources)
        val upperLimit = max(zoomLevel, float2DP(0.015f, resources) * fps)
        zoomRange = Range(0.004f, upperLimit)

        modelpath = filesDir.absolutePath + "test_txt.txt"
        UnityPlayer.UnitySendMessage(FRAME_VISUALIZER, READ_DATA, modelpath)
        Log.d("testmodelfile", path)
        Log.d("testmodelfile", modelpath)

        playVideo()

        projectTimeLineView.videoUri = uri
        setLength()
        setZoomLevel()

        tabInit()

//        save_project.setOnTouchListener { _, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    save_project.alpha = 0.5F
//                }
//
//                MotionEvent.ACTION_UP -> {
//                    stopVideo()
//                    Intent(this, SaveProjectActivity::class.java).also {
//                        startActivityForResult(it, IS_SAVED)
//                    }
//                    save_project.alpha = 1F
//                }
//            }
//            true
//        }

        gotoExportActivity.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    v.alpha = 1F
                    stopVideo()
                    exportVideo()
                }
            }
            true
        }

        back.setOnClickListener { onBackPressed() }

        sliding_view.setOnTouchListener { _, event -> slidingLayoutOnTouchEvent(event) }

        video_frame.addView(mUnityPlayer)

        setCurrentTime(0)

        mUnityPlayer.setOnClickListener {
            if (CustomAdapter.selectedEffect != null) {
                stopVideo()
                frame = (projectTimeLineView.currentTime * fps / 1000).toInt()

                (CustomAdapter.selectedEffect ?: return@setOnClickListener).isActivated = false
                (CustomAdapter.selectedEffect
                    ?: return@setOnClickListener).setBackgroundColor(Color.TRANSPARENT)
                CustomAdapter.selectedEffect = null

                val bitmap =
                    (ContextCompat.getDrawable(this, R.drawable.load) as BitmapDrawable).bitmap
                val point = Effect.Point(30, 30)
                val dataArrayList: MutableList<Effect.Data> = mutableListOf()

                // for test
                for (i in 0 until 30) {
                    dataArrayList.add(Effect.Data(bitmap, point, 30, 30))
                }
                effectList.add(
                    Effect(
                        frame,
                        frame + 29,
                        0,
                        0xFFFFFF,
                        dataArrayList
                    )
                )
            }
        }

        // export images from origin video
        Log.d("Export", "Start exporting the images from an origin video.")
        val originPath = filesDir.absolutePath + "/video_image"
        val originFile = File(originPath)
        if (!originFile.exists())
            originFile.mkdir()
//        FFmpeg.execute("-r 1 -i $path $originPath/img%08d.png")
        Log.d("Export", "Finish exporting the images from an origin video.")
        UnityPlayer.UnitySendMessage(
            PLAY_MANAGER,
            SET_VIDEO,
            "$originPath:$frameCount"
        )
    }

    /**
     * [AppCompatActivity.onBackPressed]
     */
    override fun onBackPressed() {
        val builder = ProjectAlertDialog(this) { super.onBackPressed() }
        builder.create()
        builder.show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun playVideo() {
        project_play.isSelected = true
        project_play.isActivated = false
        project_play.setOnClickListener {
            if (isPlaying) {
                stopVideo()
            } else {
                startVideo()
            }
        }
    }

    private fun startVideo() {
        if (isPlaying) return
        project_play.isActivated = true
        isPlaying = true

        if (frame == frameCount)
            frame = 0

        GlobalScope.launch {
            var time = 1000 * frame / fps
            while (isPlaying) {
                time += 50
                if (frameCount <= frame) {
                    frame = frameCount - 1
                    setCurrentTime(videoDuration)
                    stopVideo()
                    break
                }
                frame = (time * fps / 1000).toInt()
                setCurrentTime(time.toInt())
                delay(50)
            }
        }
    }

    /**
     * Stop video.
     */
    fun stopVideo() {
        project_play.isActivated = false
        isPlaying = false
    }

    private fun tabInit() {
        with(effect_tab) {
            addTab(
                effect_tab.newTab().setCustomView(createTabView(getString(R.string.head_effect)))
            )
            addTab(
                effect_tab.newTab()
                    .setCustomView(createTabView(getString(R.string.left_arm_effect)))
            )
            addTab(
                effect_tab.newTab()
                    .setCustomView(createTabView(getString(R.string.right_arm_effect)))
            )
            addTab(
                effect_tab.newTab()
                    .setCustomView(createTabView(getString(R.string.left_leg_effect)))
            )
            addTab(
                effect_tab.newTab()
                    .setCustomView(createTabView(getString(R.string.right_leg_effect)))
            )
        }

        val addPagerAdapter =
            AddFragmentPagerAdapter(
                supportFragmentManager,
                5,
                this
            )
        effect_view_pager.adapter = addPagerAdapter

        for (index in 1 until effect_tab.tabCount)
            (effect_tab.getTabAt(index) ?: return).view.tab_text.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.ContentsText40
                )
            )
        val tabView = effect_tab.getTabAt(0)
        (tabView ?: return).view.tab_text.typeface =
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
                        R.color.ContentsText40
                    )
                )
            }

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })
        effect_view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(effect_tab))
    }

    private fun createTabView(tabName: String): View? {
        val tabView = View.inflate(applicationContext, R.layout.custom_tab, null)
        val textView = tabView.findViewById<TextView>(R.id.tab_text)
        textView.text = tabName
        return tabView
    }

    private fun saveProject() {

    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun exportVideo() {
        stopVideo()
        val dialog = LoadingDialog(this, LoadingDialog.EXPORT, path, fps, resultPath)
        dialog.create()
        dialog.show()
    }

    /**
     * [AppCompatActivity.onActivityResult]
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IS_SAVED && resultCode == Activity.RESULT_OK) {
            val projectName = data?.getStringExtra(SaveProjectActivity.PROJECT_NAME)
            project_title.text = projectName
            Log.d("filename", projectName.toString())
            saveProject()
        }
    }

    private fun setCurrentTime(index: Int) {
        projectTimeLineView.currentTime = index
        timeIndexView.currentTime = index
        frame = (index * fps / 1000).toInt()
    }

    private fun setZoomLevel() {
        projectTimeLineView.dpPerMs = zoomLevel
        timeIndexView.dpPerMs = zoomLevel
    }

    private fun setLength() {
        projectTimeLineView.videoLength = videoDuration
        timeIndexView.videoLength = videoDuration
    }

    private var posX1 = 0f
    private var posX2 = 0f
    private var mode = 0
    private var newDist = 0f
    private var oldDist = 0f
    private var zoomLevel = 0f
    private lateinit var zoomRange: Range<Float>

    private fun slidingLayoutOnTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action?.and(MotionEvent.ACTION_MASK)) {
            MotionEvent.ACTION_DOWN -> {
                stopVideo()
                posX1 = event.x
                mode = 1
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> mode = 0
            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = 2
                newDist = distance(event)
                oldDist = distance(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == 1) {
                    posX2 = event.x
                    val delta =
                        (posX2 - posX1) / resources.displayMetrics.density / zoomLevel
                    val currentTime = ((1000 * frame / fps).toDouble() - delta).toInt()
                        .coerceIn(0, videoDuration)
                    setCurrentTime(currentTime)
                    posX1 = posX2
                } else if (mode == 2) {
                    newDist = distance(event)
                    zoomLevel = zoomRange.clamp(zoomLevel * newDist / oldDist)
                    setZoomLevel()
                    oldDist = newDist
                }
            }
        }
        return true
    }

    private fun distance(event: MotionEvent): Float = abs(event.getX(0) - event.getX(1))

    /**
     * [IUnityPlayerLifecycleEvents.onUnityPlayerQuitted]
     */
    override fun onUnityPlayerQuitted() {
        mUnityPlayer.quit()
    }

    /**
     * [IUnityPlayerLifecycleEvents.onUnityPlayerUnloaded]
     */
    override fun onUnityPlayerUnloaded() {
        mUnityPlayer.unload()
    }
}
