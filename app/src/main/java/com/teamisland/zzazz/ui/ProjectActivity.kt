package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentCallbacks2
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Range
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs
import kotlin.math.max

/**
 * Activity for make project
 */
class ProjectActivity : AppCompatActivity(), CoroutineScope, IUnityPlayerLifecycleEvents {

    private lateinit var job: Job
    private lateinit var mUnityPlayer: UnityPlayer

    /**
     * Destroys the activity and finishes ongoing coroutines.
     */
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        mUnityPlayer.destroy()
    }

    /**
     * The context of this scope.
     * Context is encapsulated by the scope and used for implementation of coroutine builders that are extensions on the scope.
     * Accessing this property in general code is not recommended for any purposes except accessing the [Job] instance for advanced usages.
     *
     * By convention, should contain an instance of a [job][Job] to enforce structured concurrency.
     */
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var path: String
    private var fps: Float = 0f
    private var videoDuration = 0

    /**
     * Timer during [isPlaying] is true.
     */
    private var videoTimer = Timer()
    //    private lateinit var video: BitmapVideo
//    private lateinit var bitmapList: List<Bitmap>
//    private var startFrame by Delegates.notNull<Int>()
//    private var endFrame by Delegates.notNull<Int>()

    companion object {
        /**
         * Current time of video in Unity.
         */
        var time: String = "0"

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
         * Game object of video player in Unity.
         */
        const val VIDEO_OBJECT: String = "VideoPlayer"

        /**
         * Method name of setting url in Unity
         */
        const val SET_URL: String = "setURL"

        /**
         * Method name of playing video in Unity
         */
        const val PLAY: String = "play"

        /**
         * Method name of pausing video in Unity
         */
        const val PAUSE: String = "pause"

        /**
         * Method name of setting time of a video in Unity
         */
        const val SET_TIME: String = "setTime"
    }

    /**
     * [AppCompatActivity.onRestart]
     */
    override fun onRestart() {
        super.onRestart()
        UnityPlayer.UnitySendMessage(VIDEO_OBJECT, SET_TIME, "0")
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

        job = Job()
        mUnityPlayer = UnityPlayer(this, this)

//        startFrame = intent.getIntExtra(TrimmingActivity.VIDEO_START_FRAME, 0)
//        endFrame = intent.getIntExtra(TrimmingActivity.VIDEO_END_FRAME, 0)
//        bitmapList = ArrayList(endFrame - startFrame + 1)

        path = intent.getStringExtra(TrimmingActivity.VIDEO_PATH)
        UnityPlayer.UnitySendMessage(VIDEO_OBJECT, SET_URL, path)
        val uri = Uri.parse(path)
        videoDuration = getDuration(this, uri)
        fps = getFrameCount(this, uri) / (videoDuration / 1000f)

        zoomLevel = float2DP(0.06f, resources)
        val upperLimit = max(zoomLevel, float2DP(0.015f, resources) * fps)
        zoomRange = Range(0.004f, upperLimit)

        //        Log.d("time", "start")
//        bitmapList = mediaMetadataRetriever.getFramesAtIndex(startFrame, endFrame - startFrame + 1)
//        Log.d("time", "end")
//        mediaMetadataRetriever.release()
//        video = BitmapVideo(this, fps, bitmapList, video_display, project_play)
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

        gotoExportActivity.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    gotoExportActivity.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    gotoExportActivity.alpha = 1F
                    stopVideo()
                    Intent(this, ExportActivity::class.java).apply {
//                        putExtra("URI", exportProject())
                        putExtra("URI", uri)
                    }.also {
                        startActivity(it)
                    }
                }
            }
            true
        }

        back.setOnClickListener { onBackPressed() }

        sliding_view.setOnTouchListener { _, event -> slidingLayoutOnTouchEvent(event) }

        video_frame.addView(mUnityPlayer)

        setCurrentTime("0")

        mUnityPlayer.setOnClickListener {
            if (CustomAdapter.selectedEffect != null) {
                stopVideo()
                time = projectTimeLineView.currentTime.toString()

                (CustomAdapter.selectedEffect ?: return@setOnClickListener).isActivated = false
                (CustomAdapter.selectedEffect
                        ?: return@setOnClickListener).setBackgroundColor(Color.TRANSPARENT)
                CustomAdapter.selectedEffect = null

                val bitmap = (getDrawable(R.drawable.load) as BitmapDrawable).bitmap
                val point = Effect.Point(30, 30)
                val dataArrayList: MutableList<Effect.Data> = mutableListOf()

                // for test
                for (i in 0 until 30) {
                    dataArrayList.add(Effect.Data(bitmap, point, 30, 30))
                }
                effectList.add(
                        Effect(
                                (time.toDouble() * fps / 1000).toInt(),
                                ((time.toDouble() * fps / 1000) + 29).toInt(),
                                0,
                                0xFFFFFF,
                                dataArrayList
                        )
                )
                Log.d("effect add", "${effectList.size}")
            }
        }
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
        UnityPlayer.UnitySendMessage(VIDEO_OBJECT, PLAY, "")
        project_play.isActivated = true
        isPlaying = true

        videoTimer = Timer()
        val handler = Handler()
        val timerTask = object : TimerTask() {
            override fun run() {
                if (!isPlaying)
                    stopVideo()

                handler.post { setCurrentTime(time) }
            }
        }
        videoTimer.schedule(timerTask, 0, 10)
    }

    /**
     * Stop video.
     */
    fun stopVideo() {
        UnityPlayer.UnitySendMessage(VIDEO_OBJECT, PAUSE, "")
        project_play.isActivated = false
        isPlaying = false
        videoTimer.cancel()
    }

    private fun tabInit() {
        with(effect_tab) {
            addTab(effect_tab.newTab().setCustomView(createTabView(getString(R.string.head_effect))))
            addTab(effect_tab.newTab().setCustomView(createTabView(getString(R.string.left_arm_effect))))
            addTab(effect_tab.newTab().setCustomView(createTabView(getString(R.string.right_arm_effect))))
            addTab(effect_tab.newTab().setCustomView(createTabView(getString(R.string.left_leg_effect))))
            addTab(effect_tab.newTab().setCustomView(createTabView(getString(R.string.right_leg_effect))))
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

    private fun sortEffect() {
        for (i in 0 until effectList.size) {
            for (j in i until effectList.size) {
                if (effectList[j].getStartFrame() < effectList[i].getStartFrame() ||
                        (effectList[j].getStartFrame() == effectList[i].getStartFrame()) &&
                        (effectList[j].getEndFrame() < effectList[i].getEndFrame())
                ) {
                    val effect = effectList[i]
                    effectList[i] = effectList[j]
                    effectList[j] = effect
                }
            }
        }
    }

    /**
     * Call in Unity.
     */
    private fun setCurrentTime(index: String) {
        projectTimeLineView.currentTime = index.toDouble().toInt()
        timeIndexView.currentTime = index.toDouble().toInt()
        time = index
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
                    val currentTime = (time.toDouble() - delta).toInt()
                            .coerceIn(0, videoDuration)
                    setCurrentTime(currentTime.toString())
                    posX1 = posX2
                    UnityPlayer.UnitySendMessage(VIDEO_OBJECT, SET_TIME, (time.toDouble() / 1000).toString())
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
