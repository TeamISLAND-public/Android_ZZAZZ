package com.teamisland.zzazz.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.teamisland.zzazz.R

class ExportDialog(context: Context) : Dialog(context) {

    private lateinit var textView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressTextView: TextView
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.export_dialog)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setGravity(Gravity.CENTER)

        textView = findViewById(R.id.export_text)
        progressBar = findViewById(R.id.export_progress)
        progressTextView = findViewById(R.id.progress_text)
        button = findViewById(R.id.export_dialog_button)

        textView.text = context.getString(R.string.export_text)
        button.text = context.getString(R.string.export_stop)
    }
}