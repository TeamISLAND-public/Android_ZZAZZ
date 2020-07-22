package com.teamisland.zzazz.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * Override [FragmentStatePagerAdapter] to connect tab and fragment.
 */
class FragmentPagerAdapter(
    fragmentManager: FragmentManager,
    private val tabCount: Int
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    /**
     * [FragmentStatePagerAdapter.getItem]
     */
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                Tab1()
            }
            1 -> {
                Tab2()
            }
            else -> Tab1()
        }
    }

    /**
     * [FragmentStatePagerAdapter.getCount]
     */
    override fun getCount(): Int = tabCount
}
