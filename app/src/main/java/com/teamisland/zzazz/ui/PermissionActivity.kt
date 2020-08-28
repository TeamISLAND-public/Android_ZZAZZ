package com.teamisland.zzazz.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teamisland.zzazz.R
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

        ok.setOnClickListener {
            Intent(this, IntroActivity::class.java).also { startActivity(it) }
            finish()
        }
    }
}