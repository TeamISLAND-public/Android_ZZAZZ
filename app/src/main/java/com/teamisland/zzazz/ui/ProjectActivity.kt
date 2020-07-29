package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Range
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.tabs.TabLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.*
import com.teamisland.zzazz.utils.GetVideoData.getDuration
import com.teamisland.zzazz.utils.GetVideoData.getFrameCount
import com.teamisland.zzazz.utils.UnitConverter.float2DP
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.custom_tab.view.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

/**
 * Activity for make project
 */
class ProjectActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var job: Job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
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

    private lateinit var uri: Uri
    private var frame = 0
    private lateinit var fadeOut: Animation
    private val dataSourceFactory: DataSource.Factory by lazy {
        DefaultDataSourceFactory(this, Util.getUserAgent(this, "PlayerSample"))
    }
    internal val player: SimpleExoPlayer by lazy {
        SimpleExoPlayer.Builder(this).build().also {
            it.repeatMode = SimpleExoPlayer.REPEAT_MODE_OFF
            video_display.player = it
        }
    }
    private var fps: Float = 0f

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
        const val IS_SAVED: Int = 1
    }

    /**
     * [AppCompatActivity.onRestart]
     */
    override fun onRestart() {
        super.onRestart()
        player.seekTo(0)
        startVideo()
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

        job = Job()

        uri = intent.getParcelableExtra(TrimmingActivity.VIDEO_URI)
        videoDuration = getDuration(this, uri)
        fps = getFrameCount(this, uri) / videoDuration.toFloat()

        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        zoomLevel = float2DP(0.06f, resources)
        val upperLimit = if (fps <= 4) zoomLevel else 0.015f * fps
        zoomRange = Range(0.004f, upperLimit)

        timeIndexView.videoLength = videoDuration
        projectTimeLineView.videoLength = videoDuration
        projectTimeLineView.videoUri = uri

        setZoomLevel()
        setCurrentTime(0)

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
                    frame = (player.currentPosition * fps).toInt()
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
                    stopVideo()
                    Intent(this, SaveProjectActivity::class.java).also {
                        startActivityForResult(it, IS_SAVED)
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
                    stopVideo()
                    Intent(this, ExportActivity::class.java).apply {
                        putExtra("URI", uri)
                    }.also {
                        startActivity(it)
                    }
                }
            }
            true
        }

        back.setOnClickListener { onBackPressed() }

        sliding_view.setOnTouchListener { _, event -> tabLayoutOnTouchEvent(event) }
        player.prepare(ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri))
        val effectAdapter = EffectAdapter()
        recyclerView.adapter = effectAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        effectAdapter.notifyDataSetChanged()
    }

    private fun setZoomLevel() {
        projectTimeLineView.dpPerMs = zoomLevel
        timeIndexView.dpPerMs = zoomLevel
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
        video_display.requestFocus()
        startVideo()
        fadeOut.startOffset = 1000
        fadeOut.duration = 500
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

        var end = false
        project_play.startAnimation(fadeOut)

        video_frame.setOnClickListener {
            project_play.startAnimation(fadeOut)
        }
        player.addListener(object : Player.EventListener {
            /**
             * Called when the value returned from either [.getPlayWhenReady] or [ ][.getPlaybackState] changes.
             *
             * @param playWhenReady Whether playback will proceed when ready.
             * @param playbackState The new [playback state][Player.State].
             */
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    project_play.isActivated = false
                    player.playWhenReady = false
                    end = true
                }
            }
        })

        project_play.isSelected = true

        project_play.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    project_play.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    if (player.isPlaying) {
                        stopVideo()
                    } else {
                        if (end) {
                            player.seekTo(0)
                            end = false
                            player.addListener(playButtonClickListenerObject)
                            return@setOnTouchListener true
                        }
                        startVideo()
                    }
                    project_play.alpha = 1F
                    project_play.startAnimation(fadeOut)
                }
            }
            true
        }
    }

    private val playButtonClickListenerObject = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == ExoPlayer.STATE_READY) {
                video_display.postDelayed({ startVideo() }, 100)
                project_play.alpha = 1F
                player.removeListener(this)
            }
        }
    }

    internal fun startVideo() {
        player.playWhenReady = true
        project_play.isActivated = true
        videoBinder()
    }

    private fun stopVideo() {
        player.playWhenReady = false
        project_play.isActivated = false
    }

    private fun videoBinder() = launch {
        do {
            try {
                setCurrentTime(player.currentPosition.toInt().coerceIn(0, videoDuration))
                delay(10)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        } while (true)//player.isPlaying)
    }

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
    private lateinit var zoomRange: Range<Float>

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
                    val delta =
                        (posX2 - posX1) / resources.displayMetrics.density / zoomLevel
                    val time = (player.currentPosition - delta).toInt()
                        .coerceIn(0, videoDuration)
                    player.seekTo(time.toLong())
                    setCurrentTime(time)
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

    private fun distance(event: MotionEvent): Float {
        return abs(event.getX(0) - event.getX(1))
    }
}
