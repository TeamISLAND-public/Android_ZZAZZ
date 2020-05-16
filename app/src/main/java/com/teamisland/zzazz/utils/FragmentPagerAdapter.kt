package com.teamisland.zzazz.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class FragmentPagerAdapter(
    fragmentManager: FragmentManager,
    val tabCount: Int
) : FragmentStatePagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return Tab1()
            }
            1 -> {
                return Tab2()
            }
            else -> return Tab1()
        }
    }

    override fun getCount(): Int {
        return tabCount
    }
}
