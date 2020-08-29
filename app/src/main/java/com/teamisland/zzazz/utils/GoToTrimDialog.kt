package com.teamisland.zzazz.utils

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import com.teamisland.zzazz.R
import kotlinx.android.synthetic.main.dialog_totrim.*

class GoToTrimDialog(context: Context?, val run_function: () -> Unit) : AlertDialog(context) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_totrim)

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