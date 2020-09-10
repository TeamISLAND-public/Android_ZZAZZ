package com.teamisland.zzazz.utils.objects

import android.app.Activity
import android.content.res.Resources
import android.util.DisplayMetrics

internal object DeviceInfo {

    internal fun getStatusBarHeight(resources: Resources): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    internal fun getNavigationBarHeight(resources: Resources): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    fun getHeight(activity: Activity): Int {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) displayMetrics.heightPixels
        else return displayMetrics.heightPixels - getStatusBarHeight(activity.resources)
    }

    fun getWidth(activity: Activity): Int {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

        return displayMetrics.widthPixels
    }
}