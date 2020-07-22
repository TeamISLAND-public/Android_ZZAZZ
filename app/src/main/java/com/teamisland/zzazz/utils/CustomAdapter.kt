package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.teamisland.zzazz.R
import com.teamisland.zzazz.ui.ProjectActivity

// RecyclerView Adapter for effect tab (horizontal list)
class CustomAdapter(
    private val list: ArrayList<Int>,
    private val context: Context,
    private val frame: Int
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageResource(list[position])
        holder.imageView.setOnClickListener {
            val bitmap = (context.resources.getDrawable(R.drawable.check) as BitmapDrawable).bitmap
            val bitmapArrayList: ArrayList<Bitmap> = ArrayList(30)
            val point = Effect.Point(30, 30)
            val pointArrayList: ArrayList<Effect.Point> = ArrayList(30)
            val widthArrayList: ArrayList<Float> = ArrayList(30)
            val heightArrayList: ArrayList<Float> = ArrayList(30)

            // for test
            for (i in 0 until 30) {
                pointArrayList[i] = point
                widthArrayList[i] = 30F
                heightArrayList[i] = 30F
            }
            ProjectActivity.effectList.add(
                Effect(
                    frame,
                    frame + 29,
                    0,
                    0xFFFFFF,
                    bitmapArrayList,
                    pointArrayList,
                    widthArrayList,
                    heightArrayList
                )
            )
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.item)!!
    }
}
