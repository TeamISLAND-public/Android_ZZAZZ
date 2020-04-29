package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment

class Tab1  : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.tab1, container, false)
        val listView = view.findViewById<ListView>(R.id.effect_list)

        val list = ArrayList<String>()
        for (i in 0 until 100)
            list.add(i.toString())

        val adapter = CustomAdapter(list, LayoutInflater.from(this.context))
        listView.adapter = adapter

        return view
    }
}
