package com.teamisland.zzazz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.teamisland.zzazz.R

/**
 * Show saved project
 */
class IntroProjectActivity : Fragment() {

    /**
     * Override [Fragment.onCreateView]
     */
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.activity_intro_project, container, false)
    }
}