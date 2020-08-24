package com.teamisland.zzazz.utils

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
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

        goBack.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        textView6.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        alert.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)
        stayHere.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)

        stayHere.setOnClickListener {
            dismiss()
        }
        goBack.setOnClickListener {
            dismiss()
            run_function()
        }
    }
}