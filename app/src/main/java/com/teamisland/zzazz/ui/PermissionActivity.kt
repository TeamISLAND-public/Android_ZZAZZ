package com.teamisland.zzazz.ui

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import androidx.appcompat.app.AppCompatActivity
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.CustomTypefaceSpan
import com.teamisland.zzazz.utils.UnitConverter.float2SP
import kotlinx.android.synthetic.main.activity_permission.*

/**
 * Dialog for permission
 */
class PermissionActivity : AppCompatActivity() {

    /**
     * [AppCompatActivity.onCreate]
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        window.navigationBarColor = getColor(R.color.Background)

        val cameraPermission = getString(R.string.camera_permission)
        val optional = getString(R.string.optional)
        val spannable = SpannableString(cameraPermission + optional)
        spannable.setSpan(
            CustomTypefaceSpan("archivo_bold", resources.getFont(R.font.archivo_bold)),
            0,
            cameraPermission.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            CustomTypefaceSpan("archivo_medium", resources.getFont(R.font.archivo_medium)),
            cameraPermission.length,
            cameraPermission.length + optional.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            RelativeSizeSpan(6 / 7.toFloat()),
            cameraPermission.length,
            cameraPermission.length + optional.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        camera.text = spannable

        ok.setOnClickListener {
            Intent(this, IntroActivity::class.java).also { startActivity(it) }
            finish()
        }
    }
}