package com.teamisland.zzazz.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.teamisland.zzazz.R

/**
 * Override [RecyclerView.Adapter] for effect tab
 */
class CustomAdapter(
    private val list: ArrayList<Int>,
    private val context: Context,
    private val onClickItem: View.OnClickListener
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    /**
     * [RecyclerView.Adapter.onCreateViewHolder]
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false)

        return ViewHolder(view)
    }

    /**
     * [RecyclerView.Adapter.getItemCount]
     */
    override fun getItemCount(): Int = list.size

    /**
     * [RecyclerView.Adapter.onBindViewHolder]
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageResource(list[position])
        holder.imageView.setOnClickListener(onClickItem)
    }

    /**
     * Class for each effects
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * Effect Image View
         */
        val imageView = itemView.findViewById<ImageView>(R.id.item)!!
    }
}
