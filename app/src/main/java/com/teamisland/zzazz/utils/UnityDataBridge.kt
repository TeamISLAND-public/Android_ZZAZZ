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
     * Retrieving video path to unity.
     */
    fun onPathRetrieve(path: String)

    /**
     * Retrieving video frame count to unity.
     */
    fun onFrameCountRetrieve(count: Int)

    /**
     * Retrieving video frame rate to unity.
     */
    fun onFrameRateRetrieve(fps: Float)
}