package com.teamisland.zzazz.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.teamisland.zzazz.R
import com.teamisland.zzazz.ui.ProjectActivity

/**
 * Override [Fragment] for each tab
 */
class HeadEffect(private val activity: ProjectActivity) : Fragment() {

    /**
     * [Fragment.onCreateView]
     */
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.add_tab, container, false)
        val listView = view.findViewById<RecyclerView>(R.id.effect_list)

        val list = ArrayList<Int>()
        for (i in 0 until 10)
            list.add(R.drawable.check_white)

        val adapter = CustomAdapter(list, context ?: return null, activity)
        val decoration = ItemDecoration()
        listView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        listView.adapter = adapter
        listView.addItemDecoration(decoration)

        return view
    }
}
