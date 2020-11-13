package com.teamisland.zzazz.utils.interfaces


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
        inferencePath: String
    )

    /**
     * Sets play state.
     */
    fun setPlayState(b: Boolean)

    /**
     * Cancel exporting in Unity.
     */
    fun cancelExporting()

    /**
     * Sets frame.
     */
    fun setFrame(frame: Int)

    /**
     * Exports the current project.
     */
    fun exportImage()

    /**
     * Modifies the time of the current effect.
     */
    fun modifyRange(i: Int, startFrame: Int, endFrame: Int)
}