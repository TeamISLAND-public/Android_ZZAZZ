package com.teamisland.zzazz.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.teamisland.zzazz.R
import kotlinx.android.synthetic.main.save_project.*

/**
 * Save project dialog
 */
class SaveProjectActivity : AppCompatActivity() {

    companion object {
        /**
         * Name of project
         */
        const val PROJECT_NAME = "NAME"
    }

    /**
     * [AppCompatActivity.onCreate]
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.save_project)

        project_name.requestFocus()

        val customKeyboard = CustomKeyboard(this, R.id.keyboard, R.xml.keyboard)
        customKeyboard.registerEditText(R.id.project_name)

        stop_save.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    stop_save.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    stop_save.alpha = 1F
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
            true
        }
    }
}