package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
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

class CustomAdapter(
    val list: ArrayList<String>,
    val layoutInflater: LayoutInflater
) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = layoutInflater.inflate(R.layout.item_view, null, true)
        val item = view.findViewById<TextView>(R.id.item)

        item.setText(list[position])
        Log.d("itemText", list[position])

        return view
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }
}
