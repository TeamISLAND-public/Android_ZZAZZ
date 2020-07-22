package com.teamisland.zzazz.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.teamisland.zzazz.R

/**
 * Override [Fragment] for each tab
 */
class Tab1(private val frame: Int) : Fragment() {

    /**
     * [Fragment.onCreateView]
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.tab1, container, false)
        val listView = view.findViewById<RecyclerView>(R.id.effect_list)

        listView.layoutManager = GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)

        val list = ArrayList<Int>()
        for (i in 0 until 100)
            list.add(R.drawable.check_white)

        val adapter = CustomAdapter(list, context!!, frame)
        val decoration = ItemDecoration()
        listView.adapter = adapter
        listView.addItemDecoration(decoration)

        return view
    }
}
