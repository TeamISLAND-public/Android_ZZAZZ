package com.teamisland.zzazz.utils.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import com.teamisland.zzazz.R
import kotlinx.android.synthetic.main.activity_project_alertdialog.*

class ProjectAlertDialog(context: Context?, val run_function: () -> Unit) : AlertDialog(context) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_alertdialog)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setGravity(Gravity.CENTER)

        window?.setLayout(
            (256 * Resources.getSystem().displayMetrics.density).toInt(),
            (160 * Resources.getSystem().displayMetrics.density).toInt()
        )

        stayHere.setOnClickListener {
            dismiss()
        }
        goBack.setOnClickListener {
            dismiss()
            run_function()
        }
    }
}