package com.teamisland.zzazz.utils

internal interface ITrimmingData {
    fun updateUI()

    var rangeStartIndex: Long
    var rangeExclusiveEndIndex: Long
    val startMs: Long
    val endMs: Long
    var currentVideoPosition: Long
    val duration: Int
    val frameCount: Long
    val endExcludeMs: Long
}