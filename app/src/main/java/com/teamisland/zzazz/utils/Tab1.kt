package com.teamisland.zzazz.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.teamisland.zzazz.R

class Tab1 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.tab1, container, false)
        val listView = view.findViewById<RecyclerView>(R.id.effect_list)

        listView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        val list = ArrayList<String>()
        for (i in 0 until 100)
            list.add(i.toString())

        val adapter = CustomAdapter(list, context!!, OnClickItem)
        val decoration = ItemDecoration
        listView.adapter = adapter
        listView.addItemDecoration(decoration)

        return view
    }

    private abstract class OnClickItem {

        companion object ClickItem : View.OnClickListener {
            override fun onClick(v: View?) {
            }
        }
    }
}
