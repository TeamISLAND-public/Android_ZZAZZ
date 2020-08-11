package com.teamisland.zzazz.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.teamisland.zzazz.ui.ProjectActivity

/**
 * Override [FragmentStatePagerAdapter] to connect tab and fragment.
 */
class AddFragmentPagerAdapter(
    fragmentManager: FragmentManager,
    private val tabCount: Int,
    private val activity: ProjectActivity
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    /**
     * [FragmentStatePagerAdapter.getItem]
     */
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> HeadEffect(activity)
            1 -> LATab()
            2 -> RATab()
            3 -> LLTab()
            4 -> RLTab()
            else -> HeadEffect(activity)
        }
    }

    /**
     * [FragmentStatePagerAdapter.getCount]
     */
    override fun getCount(): Int = tabCount
}
