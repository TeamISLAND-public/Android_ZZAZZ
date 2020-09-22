package com.teamisland.zzazz.utils.interfaces

import com.teamisland.zzazz.utils.inference.Person
import java.util.ArrayList


/**
 * !!! IMPORTANT !!! KEEP SAME SIGNATURE WITH UNITY: AndroidDataBridge.
 *
 * Interface between unity and android.
 */
interface UnityDataBridge {
    /**
     * Calls upon the activity has accepted the interface.
     */
    fun onSuccessfulAccept()

    /**
     * Retrieves video data.
     */
    fun retrieveMetadata(
        importPath: String,
        exportPath: String,
        count: Int,
        fps: Float,
        inferencePath: String)

    /**
     * Sets play state.
     */
    fun setPlayState(b: Boolean)

    /**
     * Cancel exporting in Unity.
     */
    fun cancelExporting()
}