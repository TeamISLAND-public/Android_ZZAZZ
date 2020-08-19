package com.teamisland.zzazz.utils

import android.util.Range

internal interface ITrimmingData {
    fun updateUI()

    var rangeStartIndex: Long
    var rangeExclusiveEndIndex: Long
    val frameRange: Range<Long>
    val startMs: Long
    val endMs: Long
    var currentVideoPosition: Long
    val millisecondRange: Range<Long>
    val startFrameIndexFraction: Double
    val endFrameIndexFraction: Double
    val fps: Int
    val duration: Int
    val frameCount: Long
}