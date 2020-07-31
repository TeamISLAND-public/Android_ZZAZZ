package com.teamisland.zzazz.utils

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
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

        project_name.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && v.length() != 0) {
                Intent().apply {
                    putExtra(PROJECT_NAME, v.text.toString())
                    setResult(RESULT_OK, this)
                    finish()
                }
            }
            false
        }

        stop_save.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    stop_save.alpha = 0.5F
                }

                MotionEvent.ACTION_UP -> {
                    stop_save.alpha = 1F
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
            true
        }
    }
}