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
    public fun checkPermission(): Boolean {
        for (permission in permissions)
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            )
                permissionList.add(permission)

        if (permissionList.isEmpty())
            return false
        return true
    }

    /**
     * Request the permissions to use an application.
     */
    public fun requestPermission() {
        ActivityCompat.requestPermissions(activity, permissionList.toTypedArray(), permissionResult)
    }

    /**
     * Check a user permits an every permissions.
     */
    public fun permissionResult(
        requestCode: Int,
        grantResult: IntArray
    ): Boolean {
        if (requestCode == permissionResult && grantResult.isNotEmpty())
            for (result in grantResult)
                if (result == -1)
                    return false
        return true
    }
}