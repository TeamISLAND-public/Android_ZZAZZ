package com.example.test

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_one.*

class Fragment1 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("life_cycle", "F1 onCreateView")

        val view = layoutInflater.inflate(R.layout.fragment_one, container, false)
        val listView = view.findViewById<ListView>(R.id.list_view)

        val list = ArrayList<String>()
        for (i in 0 until 100)
            list.add(i.toString())

        val adapter = CustomAdapter(list, LayoutInflater.from(this.context))
        Log.d("adapter", "CustomAdapter")
        listView.adapter = adapter
        Log.d("adapter", "listView")

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