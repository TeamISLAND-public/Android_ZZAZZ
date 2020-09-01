package com.teamisland.zzazz.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Manage Permission of an application.
 */
class PermissionManager(private val context: Context, private val activity: Activity) {

    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.INTERNET,
        Manifest.permission.VIBRATE
    )

    private val permissionList = ArrayList<String>()
    private val permissionResult = 1

    /**
     * Check the permissions of an application.
     *
     * @return is every permissions are granted.
     */
    fun checkPermission(): Boolean {
        permissionList.clear()
        for (permission in permissions)
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            )
                permissionList.add(permission)

        if ((permissionList.size == 1 && permissionList[0] == Manifest.permission.CAMERA) || permissionList.isEmpty())
            return true
        return false
    }

    /**
     * Request the permissions to use an application.
     */
    fun requestPermission(): Unit =
        ActivityCompat.requestPermissions(
            activity,
            permissionList.toTypedArray(),
            permissionResult
        )

    /**
     * Check a user permits an every permissions.
     */
    fun permissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResult: IntArray
    ): Boolean {
        if (requestCode == permissionResult && grantResult.isNotEmpty())
            for (i in permissions.indices) {
                if (permissions[i] == Manifest.permission.CAMERA)
                    continue
                if (grantResult[i] == -1)
                    return false
            }
        return true
    }
}