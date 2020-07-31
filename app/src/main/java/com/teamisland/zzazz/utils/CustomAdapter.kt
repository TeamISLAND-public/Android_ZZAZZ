package com.teamisland.zzazz.utils

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.teamisland.zzazz.R
import com.teamisland.zzazz.ui.ProjectActivity
import kotlinx.android.synthetic.main.activity_project.*

/**
 * Override [RecyclerView.Adapter] for effect tab
 */
class CustomAdapter(
    private val list: ArrayList<Int>,
    private val context: Context,
    private val activity: ProjectActivity
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    companion object {
        /**
         * Selected effect
         */
        var selectedEffect: ImageView? = null
    }

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
            if (selectedEffect != null && selectedEffect != it) {
                (selectedEffect ?: return@setOnClickListener).setBackgroundColor(Color.TRANSPARENT)
                selectedEffect = it as ImageView
                it.isActivated = true
                it.setBackgroundColor(Color.parseColor("#8E359C"))
            } else {
                if (it.isActivated) {
                    selectedEffect = null
                    it.isActivated = false
                    it.setBackgroundColor(Color.TRANSPARENT)
                } else {
                    activity.video_display.pause()
                    activity.project_play.isActivated = false

                    selectedEffect = it as ImageView
                    it.isActivated = true
                    it.setBackgroundColor(Color.parseColor("#8E359C"))
                }
            }
        }
    }

    /**
     * Class for each effects
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * Effect Image View
         */
        val imageView: ImageView = itemView.findViewById(R.id.item)!!
    }
}
