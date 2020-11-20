package com.teamisland.zzazz.ui

import android.app.Activity
import android.content.ComponentCallbacks2
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Range
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.tabs.TabLayout
import com.teamisland.zzazz.R
import com.teamisland.zzazz.ui.TrimmingActivity.Companion.MODEL_OUTPUT
import com.teamisland.zzazz.ui.TrimmingActivity.Companion.VIDEO_DATA
import com.teamisland.zzazz.utils.*
import com.teamisland.zzazz.utils.dialog.GoToTrimDialog
import com.teamisland.zzazz.utils.dialog.LoadingDialog
import com.teamisland.zzazz.utils.inference.Person
import com.teamisland.zzazz.utils.interfaces.UnityDataBridge
import com.teamisland.zzazz.utils.objects.UnitConverter
import com.teamisland.zzazz.utils.objects.UnitConverter.float2DP
import com.teamisland.zzazz.utils.view.ZoomableView
import com.unity3d.player.IUnityPlayerLifecycleEvents
import com.unity3d.player.UnityPlayer
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.custom_tab.view.*
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

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

    private val resultPath: String by lazy { filesDir.absolutePath + "/result.mp4" }

    @Suppress("unused")
    private val modelOutput: ArrayList<Person> by lazy {
        intent.getParcelableArrayListExtra(MODEL_OUTPUT) ?: ArrayList()
    }

    @Suppress("unused")
    private val modelPath: String by lazy { filesDir.absolutePath + "/test_txt.txt" }
    private val capturePath: String by lazy { filesDir.absolutePath + "/capture_image" }
    private val fps: Float by lazy { frameCount * 1000f / videoDuration }

    private val videoData: VideoDataContainer by lazy { intent.getParcelableExtra(VIDEO_DATA)!! }

    private val zoomableListeners = ArrayList<ZoomableView>()

    private val frameCount: Int
        get() = videoData.frameCount
    private val videoDuration: Int
        get() = videoData.videoDuration

    private val dialog: LoadingDialog by lazy {
        LoadingDialog(
            this,
            LoadingDialog.EXPORT,
            capturePath,
            videoData.audioPath,
            frameCount,
            fps,
            resultPath,
            unityDataBridge
        )
    }
    private var frame: Int = 0
        set(value) {
            field = value
            setCurrentFrame(value)
        }

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
     * Destroys the activity and finishes ongoing coroutines.
     */
    override fun onDestroy() {
        super.onDestroy()
        mUnityPlayer.destroy()
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
            videoData.imagePath,
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
    }

    /**
     * For unity export frame update event handling.
     */
    @Suppress("unused")
    fun exportFrame(frame: Int) {
        dialog.update(50 * (frame + 1) / frameCount)
    }

    /**
     * Encode captured images to video.
     */
    @Suppress("unused")
    fun encodeVideo() {
        dialog.encodeVideo()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// Creation codes.
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * [AppCompatActivity.onCreate]
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)
        window.navigationBarColor = getColor(R.color.Background)

        addZoomableViews()

        zoomLevel = float2DP(2f, resources)

        project_play.setOnClickListener { unityDataBridge?.setPlayState(!project_play.isActivated) }

        projectTimeLineView.path = videoData.imagePath
        projectTimeLineView.frameCount = frameCount
        setLength()

        tabInit()

        gotoExportActivity.setOnClickListener {
            stopVideo()
            exportVideo()
        }

        back.setOnClickListener { onBackPressed() }

        sliding_view.setOnTouchListener { view, event ->
            view.performClick()
            projectSlidingPanelController.handleEvent(event)
        }

        video_frame.addView(mUnityPlayer)

        mUnityPlayer.setOnClickListener {
            CustomAdapter.selectedEffect?.let {
                stopVideo()
                frame = projectTimeLineView.currentFrame

                it.isActivated = false
                it.setBackgroundColor(Color.TRANSPARENT)
                CustomAdapter.selectedEffect = null
            }
        }
    }

    private fun addZoomableViews() {
        zoomableListeners.apply {
            add(projectTimeLineView)
            add(projectEffectEditor)
            add(timeIndexView)
            add(projectEffectEditor)
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
        val tabNames = arrayOf(
            getString(R.string.head_effect),
            getString(R.string.left_arm_effect),
            getString(R.string.right_arm_effect),
            getString(R.string.left_leg_effect),
            getString(R.string.right_leg_effect)
        )

        tabNames.forEach {
            val newTab = effect_tab.newTab()
            newTab.customView = createTabView(it)
            effect_tab.addTab(newTab)
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
            override fun onTabSelected(tab: TabLayout.Tab) {
                effect_view_pager.currentItem = tab.position
                val tabText = tab.view.tab_text
                tabText.typeface = archivoBold
                tabText.setTextColor(whiteColor)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabText = tab.view.tab_text
                tabText.typeface = archivoRegular
                tabText.setTextColor(white40Color)
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
        unityDataBridge?.exportImage()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// Code for exporting
    ////////////////////////////////////////////////////////////////////////////////////////////////////

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

    private fun setCurrentFrame(frame: Int) {
        for (zoomableListener in zoomableListeners)
            zoomableListener.currentFrame = frame

        this.frame = frame
        unityDataBridge?.setFrame(frame)
    }

    private fun setZoomLevel() {
        for (zoomableListener in zoomableListeners)
            zoomableListener.dpPerFrame = zoomLevel
    }

    private fun setLength() {
        for (zoomableListener in zoomableListeners) {
            zoomableListener.videoLength = videoDuration
            zoomableListener.frameCount = frameCount
        }
    }

    private var anchoredFrame = 0

    private val projectSlidingPanelController: ProjectSlidingPanelController by lazy {
        ProjectSlidingPanelController().apply {
            onMove = { deltaPos ->
                val deltaTime = (UnitConverter.px2dp(deltaPos, resources) / zoomLevel).roundToInt()
                frame = (anchoredFrame - deltaTime).coerceIn(0, frameCount - 1)
            }
            onMultitouch = { newDist, oldDist ->
                zoomLevel = zoomRange.clamp(zoomLevel * newDist / oldDist)
            }
            onTouchStart = {
                stopVideo()
                anchoredFrame = frame
            }
        }
    }

    // dp / frame
    private var zoomLevel = 0f
        set(value) {
            field = value
            setZoomLevel()
        }

    private val zoomRange: Range<Float> by lazy {
        val upperLimit = max(zoomLevel, float2DP(15f, resources))
        Range(2 / 15f, upperLimit)
    }
}
