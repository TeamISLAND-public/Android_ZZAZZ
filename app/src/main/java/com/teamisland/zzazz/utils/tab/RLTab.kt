package com.teamisland.zzazz.utils.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.teamisland.zzazz.R

/**
 * Right leg Tab
 */
class RLTab : Fragment() {

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