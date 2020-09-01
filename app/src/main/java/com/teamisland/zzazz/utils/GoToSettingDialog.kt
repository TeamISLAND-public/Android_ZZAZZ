package com.teamisland.zzazz.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat.startActivity
import com.teamisland.zzazz.R
import kotlinx.android.synthetic.main.dialog_tosetting.*


/**
 * Dialog for changing permission setting.
 */
class GoToSettingDialog(
    context: Context,
    private val activity: Activity,
    private val permissionManager: PermissionManager,
    private val mode: Int
) :
    Dialog(context) {

    companion object {
        /**
         * Get storage permission.
         */
        const val STORAGE: Int = 0

        /**
         * Get camera permission.
         */
        const val CAMERA: Int = 1
    }

    /**
     * [Dialog.onCreate]
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_tosetting)
        (window ?: return).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        script.text =
            when (mode) {
                STORAGE -> context.getString(R.string.setting_storage_script)
                CAMERA -> context.getString(R.string.setting_video_script)
                else -> return
            }

        allow.setOnClickListener {
            Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", context.packageName, null)
            }.also { startActivity(context, it, null) }
            dismiss()
        }
    }

    /**
     * [Dialog.dismiss]
     */
    override fun dismiss() {
        if (!permissionManager.checkPermission())
            finishAffinity(activity)
        super.dismiss()
    }
}