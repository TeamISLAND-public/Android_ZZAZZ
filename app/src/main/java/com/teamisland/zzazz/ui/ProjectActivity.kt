package com.teamisland.zzazz.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.teamisland.zzazz.R
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        fps = 30L
        maxFrame = 100

        project_back.setImageDrawable(getDrawable(R.drawable.video_back))
        project_next.setImageDrawable(getDrawable(R.drawable.video_next))

        var end = false
        project_play.setImageDrawable(getDrawable(R.drawable.video_play_small))
        project_play.setOnClickListener {
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
        }

        slide.anchorPoint = 0.5f
        slide.getChildAt(1).setOnClickListener(null)
        slide.panelState = SlidingUpPanelLayout.PanelState.HIDDEN

        add_effect_button.setOnClickListener {
            slide.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
        }

        check_effect.setOnClickListener {
            slide.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        }

//        playBitmap()

        val intent = Intent(this, ExportActivity::class.java)
        gotoExportActivity.setOnClickListener {
            playing.cancel()
            startActivity(intent)
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
//        effect_tab.addTab(effect_tab.newTab().setText(getString(R.string.tab1_name)))
//        effect_tab.addTab(effect_tab.newTab().setText(getString(R.string.tab2_name)))
//
//        val pagerAdapter =
//            FragmentPagerAdapter(
//                supportFragmentManager,
//                3
//            )
//        view_pager.adapter = pagerAdapter
//
//        effect_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                view_pager.currentItem = tab!!.position
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {
//            }
//
//            override fun onTabReselected(tab: TabLayout.Tab?) {
//            }
//        })
//        view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(effect_tab))
    }
}
