package com.teamisland.zzazz.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.teamisland.zzazz.R

/**
 * Left Leg Tab
 */
class LLTab : Fragment() {

    /**
     * [Fragment.onCreateView]
     */
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_tab, container, false)
    }
}