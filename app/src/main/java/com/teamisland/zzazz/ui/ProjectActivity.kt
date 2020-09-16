package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentCallbacks2
import android.content.Intent
import android.graphics.Color
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
import com.teamisland.zzazz.ui.TrimmingActivity.Companion.AUDIO_PATH
import com.teamisland.zzazz.ui.TrimmingActivity.Companion.IMAGE_PATH
import com.teamisland.zzazz.ui.TrimmingActivity.Companion.MODEL_OUTPUT
import com.teamisland.zzazz.ui.TrimmingActivity.Companion.VIDEO_DURATION
import com.teamisland.zzazz.ui.TrimmingActivity.Companion.VIDEO_FRAME_COUNT
import com.teamisland.zzazz.utils.AddFragmentPagerAdapter
import com.teamisland.zzazz.utils.CustomAdapter
import com.teamisland.zzazz.utils.SaveProjectActivity
import com.teamisland.zzazz.utils.dialog.GoToTrimDialog
import com.teamisland.zzazz.utils.dialog.LoadingDialog
import com.teamisland.zzazz.utils.inference.Person
import com.teamisland.zzazz.utils.interfaces.UnityDataBridge
import com.teamisland.zzazz.utils.objects.UnitConverter.float2DP
import com.teamisland.zzazz.utils.objects.UnitConverter.px2dp
import com.unity3d.player.IUnityPlayerLifecycleEvents
import com.unity3d.player.UnityPlayer
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.custom_tab.view.*
import java.io.File
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.properties.Delegates

/**
 * Activity for make project
 */
class ProjectActivity : AppCompatActivity() {

    private val mUnityPlayer: UnityPlayer by lazy {
        UnityPlayer(this, object : IUnityPlayerLifecycleEvents {
            override fun onUnityPlayerUnloaded() = Unit
            override fun onUnityPlayerQuitted() = Unit
        })
    }

    /**
     * Destroys the activity and finishes ongoing coroutines.
     */
    override fun onDestroy() {
        super.onDestroy()
        mUnityPlayer.destroy()
    }

    private val resultPath: String by lazy { filesDir.absolutePath + "/result.mp4" }

    private val modelOutput: ArrayList<Person?> by lazy {
        intent.getParcelableArrayListExtra<Person?>(MODEL_OUTPUT)
    }

    @Suppress("unused")
    private val modelPath: String by lazy { filesDir.absolutePath + "/test_txt.txt" }
    private val capturePath: String by lazy { filesDir.absolutePath + "/capture_image" }
    private val fps: Float by lazy { frameCount * 1000f / videoDuration }
    private val imagePath: String by lazy { intent.getStringExtra(IMAGE_PATH) }
    private val audioPath: String by lazy { intent.getStringExtra(AUDIO_PATH) }
    private val frameCount: Int by lazy { intent.getIntExtra(VIDEO_FRAME_COUNT, 0) }
    private val videoDuration: Int by lazy { intent.getIntExtra(VIDEO_DURATION, 0) }
    private val dialog by lazy {
        LoadingDialog(
            this,
            LoadingDialog.EXPORT,
            capturePath,
            audioPath,
            frameCount,
            fps,
            resultPath,
            unityDataBridge
        )
    }
    private var frame: Int = 0

    companion object {
        /**
         * Check the project is saved
         */
        const val IS_SAVED: Int = 1

        /**
         * Path of the result video.
         */
        const val RESULT: String = "RESULT"
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
        stopVideo()
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// Unity data exchange.
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    private var unityDataBridge: UnityDataBridge? = null

    /**
     * Used for unity interface accepting.
     */
    @Suppress("unused")
    fun accept(li: UnityDataBridge) {
        unityDataBridge = li
        li.onSuccessfulAccept()
        li.retrieveMetadata(
            imagePath,
            capturePath,
            frameCount,
            fps,
            filesDir.absolutePath + "/inference.txt"
        )
    }

    /**
     * Used for unity message sending test.
     */
    @Suppress("unused")
    fun whoAmI(s: String) {
        Log.d("Unity whoAmI", s)
    }

    /**
     * For unity play state update event handling.
     */
    @Suppress("unused")
    fun playState(b: Boolean) {
        project_play.isActivated = b
    }

    /**
     * For unity play frame update event handling.
     */
    @Suppress("unused")
    fun unityFrame(b: Int) {
        frame = b
        setCurrentTime(frame * videoDuration / frameCount)
    }

    /**
     * For unity export frame update event handling.
     */
    @Suppress("unused")
    fun exportFrame(frame: Int) {
        dialog.update(50 * (frame + 1) / frameCount)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// Creation codes.
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * [AppCompatActivity.onCreate]
     */
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "ControlFlowWithEmptyBody")
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)
        window.navigationBarColor = getColor(R.color.Background)

        zoomLevel = float2DP(0.06f, resources)

//        Log.i(
//            "zzazz_core1",
//            String.format(
//                "shape %d %s %d",
//                modelOutput[1]!!.keyPoints.size, //21
//                modelOutput[0]!!.keyPoints[0].position.toString(), //Position(x=0.13257)
//                modelOutput.size
//            )
//        )

        project_play.setOnClickListener { unityDataBridge?.setPlayState(!project_play.isActivated) }

        projectTimeLineView.path = imagePath
        projectTimeLineView.frameCount = frameCount
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

        gotoExportActivity.setOnClickListener {
            stopVideo()
            exportVideo()
        }

        back.setOnClickListener { onBackPressed() }

        sliding_view.setOnTouchListener { _, event -> slidingLayoutOnTouchEvent(event) }

        video_frame.addView(mUnityPlayer)

        setCurrentTime(0)

        mUnityPlayer.setOnClickListener {
            CustomAdapter.selectedEffect?.let {
                stopVideo()
                frame = (projectTimeLineView.currentTime * fps / 1000).roundToInt()

                it.isActivated = false
                it.setBackgroundColor(Color.TRANSPARENT)
                CustomAdapter.selectedEffect = null
            }
        }
    }

    /**
     * [AppCompatActivity.onBackPressed]
     */
    override fun onBackPressed() {
        val builder = GoToTrimDialog(this) { super.onBackPressed() }
        builder.create()
        builder.show()
    }

    private fun startVideo() {
        unityDataBridge?.setPlayState(true)
    }

    /**
     * Stop video.
     */
    fun stopVideo() {
        unityDataBridge?.setPlayState(false)
    }

    private fun tabInit() {
        val tabNameList = arrayOf(
            getString(R.string.head_effect),
            getString(R.string.left_arm_effect),
            getString(R.string.right_arm_effect),
            getString(R.string.left_leg_effect),
            getString(R.string.right_leg_effect)
        )
        tabNameList.forEach {
            with(effect_tab) { addTab(newTab().setCustomView(createTabView(it))) }
        }

        effect_view_pager.adapter = AddFragmentPagerAdapter(supportFragmentManager, 5, this)

        val archivoBold = ResourcesCompat.getFont(applicationContext, R.font.archivo_bold)
        val archivoRegular = ResourcesCompat.getFont(applicationContext, R.font.archivo_regular)
        val white40Color = ContextCompat.getColor(applicationContext, R.color.ContentsText40)
        val whiteColor = ContextCompat.getColor(applicationContext, R.color.White)

        for (index in 1 until effect_tab.tabCount) {
            effect_tab.getTabAt(index)?.view?.tab_text?.setTextColor(white40Color)
        }

        val tabView = effect_tab.getTabAt(0)
        tabView?.view?.tab_text?.apply {
            setTextColor(whiteColor)
            typeface = archivoBold
        }

        effect_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab == null) return
                effect_view_pager.currentItem = tab.position
                tab.view.tab_text.typeface = archivoBold
                tab.view.tab_text.setTextColor(whiteColor)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                if (tab == null) return
                tab.view.tab_text.typeface = archivoRegular
                tab.view.tab_text.setTextColor(white40Color)
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
        /* no-op */
    }

    private fun exportVideo() {
        stopVideo()
        val captureFile = File(capturePath)
        if (!captureFile.exists())
            captureFile.mkdir()
        dialog.create()
        dialog.show()
        dialog.activate()
        UnityPlayer.UnitySendMessage("InteractionManager", "ExportImage", "")
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// Code for exporting
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Encode captured images to video.
     */
    @Suppress("unused")
    fun encodeVideo() {
        dialog.encodeVideo()
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

    private fun setCurrentTime(ms: Int) {
        projectTimeLineView.currentTime = ms
        projectEffectEditor.currentTime = ms
        timeIndexView.currentTime = ms
        frame = (ms * fps / 1000).toInt()
    }

    private fun setZoomLevel() {
        projectTimeLineView.dpPerMs = zoomLevel
        projectEffectEditor.dpPerMs = zoomLevel
        timeIndexView.dpPerMs = zoomLevel
    }

    private fun setLength() {
        projectTimeLineView.videoLength = videoDuration
        projectEffectEditor.videoLength = videoDuration
        timeIndexView.videoLength = videoDuration
        projectTimeLineView.frameCount = frameCount
        timeIndexView.frameCount = frameCount
    }

    private var posX1 = 0f
    private var posXAnchor = 0f
    private var anchoredMs = 0
    private var mode = 0
    private var newDist = 0f
    private var oldDist = 0f

    // dp / time
    private var zoomLevel by Delegates.notNull<Float>()
    private val zoomRange: Range<Float> by lazy {
        val upperLimit = max(zoomLevel, float2DP(0.015f, resources) * fps)
        Range(0.004f, upperLimit)
    }

    private fun slidingLayoutOnTouchEvent(event: MotionEvent): Boolean {
        when (event.action.and(MotionEvent.ACTION_MASK)) {
            MotionEvent.ACTION_DOWN -> {
                stopVideo()
                posX1 = event.x
                posXAnchor = event.x
                anchoredMs = (1000 * frame / fps).roundToInt()
                mode = 1
            }
            MotionEvent.ACTION_UP -> mode = 0
            MotionEvent.ACTION_POINTER_UP -> mode = 1
            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = 2
                newDist = distance(event)
                oldDist = distance(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == 1) {
                    val deltaPos = event.x - posXAnchor
                    val deltaTime = (px2dp(deltaPos, resources) / zoomLevel).roundToInt()
                    val currentTime = Range(0, videoDuration).clamp(anchoredMs - deltaTime)
                    val currentFrame = (currentTime * fps / 1000).roundToInt()
                    frame = currentFrame
                    setCurrentTime(currentTime)
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
}
