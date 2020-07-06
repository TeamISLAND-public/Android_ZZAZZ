package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.content.Intent
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
import kotlin.properties.Delegates

class ProjectActivity : AppCompatActivity() {

    private var maxFrame by Delegates.notNull<Int>()
    private var fps by Delegates.notNull<Long>()
    private lateinit var playing: Job
    private var isPlaying: Boolean = true
    private var frame: Int = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        fps = 30L
        maxFrame = 100

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
                    save_project.alpha = 1F
                }
            }
            true
        }

//        playBitmap()

        val intent = Intent(this, ExportActivity::class.java)
        gotoExportActivity.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    gotoExportActivity.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    gotoExportActivity.alpha = 1F
                    playing.cancel()
                    startActivity(intent)
                }
            }
            true
        }
    }

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
//                        var bitmap: Bitmap
//                        video_display.setImageBitmap(bitmap)
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
}
