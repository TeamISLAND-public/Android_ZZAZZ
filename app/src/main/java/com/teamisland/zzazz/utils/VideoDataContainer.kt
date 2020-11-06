package com.teamisland.zzazz.utils

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 *
 */
@Parcelize
data class VideoDataContainer(
    /**
     *
     */
    val imagePath: String,
    /**
     *
     */
    val audioPath: String,
    /**
     *
     */
    val frameCount: Int,
    /**
     *
     */
    val videoDuration: Int,
) : Parcelable