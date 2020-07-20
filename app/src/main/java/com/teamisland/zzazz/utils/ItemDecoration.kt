package com.teamisland.zzazz.utils

import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8F, view.resources.displayMetrics
        ).toInt()
        view.layoutParams.width = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            64F, view.resources.displayMetrics
        ).toInt()
        view.layoutParams.height = parent.layoutParams.height
    }
}