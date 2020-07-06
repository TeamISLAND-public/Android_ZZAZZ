package com.teamisland.zzazz.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemDecoration {

    companion object ItemDeco : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.top = 20
            outRect.left = 10
            view.layoutParams.width = 80
            view.layoutParams.height = 80
        }
    }
}