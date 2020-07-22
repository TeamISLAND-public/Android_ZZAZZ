package com.teamisland.zzazz.utils

import android.content.res.Resources
import android.util.TypedValue
import android.util.TypedValue.*

object UnitConverter {
    /**
     * Converts given float to DP.
     * ex) input is 9f => 9dp-equivalent pixel output.
     */
    fun float2DP(float: Float, resources: Resources): Float {
        return applyDimension(
            COMPLEX_UNIT_DIP,
            float,
            resources.displayMetrics
        )
    }

    /**
     * Converts given float to SP.
     * ex) input is 9f => 9sp-equivalent pixel output.
     */
    fun float2SP(float: Float, resources: Resources): Float {
        return applyDimension(
            COMPLEX_UNIT_SP,
            float,
            resources.displayMetrics
        )
    }

    /**
     * Converts given float to [dimension].
     */
    fun float2Unit(float: Float, resources: Resources, dimension: Int): Float {
        return applyDimension(
            dimension,
            float,
            resources.displayMetrics
        )
    }
}