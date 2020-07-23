package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.teamisland.zzazz.R
import com.teamisland.zzazz.ui.ProjectActivity

/**
 * Override [RecyclerView.Adapter] for effect tab
 */
class CustomAdapter(
    private val list: ArrayList<Int>,
    private val context: Context,
    private val frame: Int
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
        holder.imageView.setOnClickListener {
            val bitmap = (context.resources.getDrawable(R.drawable.load) as BitmapDrawable).bitmap
            val point = Effect.Point(30, 30)
            val dataArrayList: MutableList<Effect.Data> = mutableListOf()

            // for test
            for (i in 0 until 30) {
                dataArrayList.add(Effect.Data(bitmap, point, 30, 30))
            }
            ProjectActivity.tempList.add(
                Effect(
                    frame,
                    frame + 29,
                    0,
                    0xFFFFFF,
                    dataArrayList
                )
            )
            Log.d("temporary add", "${ProjectActivity.tempList.size}")
        }
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
