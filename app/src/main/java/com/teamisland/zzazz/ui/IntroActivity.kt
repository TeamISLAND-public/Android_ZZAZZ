package com.teamisland.zzazz.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.teamisland.zzazz.R
import kotlinx.android.synthetic.main.activity_intro.*

/**
 * Activity before video trimming.
 * Here you can choose video, or take it yourself.
 * Hands the uri of the video to next activity.
 */
class IntroActivity : AppCompatActivity() {

    companion object {
        /**
         * The number of pages in Intro Activity
         */
        const val NUM_PAGES = 2
    }

    /**
     * [AppCompatActivity.onCreate]
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)


        val pagerAdapter = IntroFragmentPagerAdapter(this)
        intro_view_pager.adapter = pagerAdapter
    }

    /**
     * Override [AppCompatActivity.onBackPressed]
     */
    override fun onBackPressed() {
        if (intro_view_pager.currentItem == 0) {
            super.onBackPressed()
        } else {
            intro_view_pager.currentItem--
        }
    }

    private inner class IntroFragmentPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> IntroLoadActivity(packageManager)
                1 -> IntroProjectActivity()
                else -> IntroLoadActivity(packageManager)
            }
        }

    }
}
