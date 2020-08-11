package com.teamisland.zzazz.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Design of each effect items
 */
class ItemDecoration : RecyclerView.ItemDecoration() {

    /**
     * [RecyclerView.ItemDecoration.getItemOffsets]
     */
    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
    ) {
        outRect.left = UnitConverter.float2DP(16F, view.resources).toInt()
        outRect.top = UnitConverter.float2DP(10F, view.resources).toInt()
        view.layoutParams.width = UnitConverter.float2DP(64F, view.resources).toInt()
        view.layoutParams.height = UnitConverter.float2DP(64F, view.resources).toInt()
    }
}