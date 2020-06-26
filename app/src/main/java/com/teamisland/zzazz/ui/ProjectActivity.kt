package com.teamisland.zzazz.ui

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teamisland.zzazz.R
import kotlinx.android.synthetic.main.activity_project.*

class ProjectActivity : AppCompatActivity() {

    private lateinit var uri: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        uri = "android.resource://$packageName/" + R.raw.test

        video_display.setMediaController(null)
        video_display.setVideoURI(Uri.parse(uri))
        video_display.requestFocus()

        project_back.setImageDrawable(getDrawable(R.drawable.video_back))
        project_next.setImageDrawable(getDrawable(R.drawable.video_next))

        var isPlaying: Boolean = true
        var end = false
        project_play.setImageDrawable(getDrawable(R.drawable.video_play_small))
        project_play.setOnClickListener {
            isPlaying = if (isPlaying) {
                if (end) {
                    video_display.seekTo(0)
                    end = false
                }
                project_play.setImageDrawable(getDrawable(R.drawable.video_pause_small))
                video_display.start()
                false
            } else {
                project_play.setImageDrawable(getDrawable(R.drawable.video_play_small))
                video_display.pause()
                true
            }
        }

        video_display.setOnCompletionListener {
            video_display.pause()
            project_play.setImageDrawable(getDrawable(R.drawable.video_play_small))
            end = true
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
