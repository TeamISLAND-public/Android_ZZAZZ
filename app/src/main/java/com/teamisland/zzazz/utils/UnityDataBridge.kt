package com.teamisland.zzazz.utils


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
     * Method for testing if the connection has been established.
     */
    fun isUserAGoat(any: Any)

    /**
     * Retrieves video data.
     */
    fun retrieveMetadata(path: String, count: Int, fps: Float)

    /**
     * Toggles play state. If playing, it'll pause, vice versa.
     */
    fun togglePlayState()
}