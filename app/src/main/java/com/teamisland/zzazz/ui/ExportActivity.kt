package com.teamisland.zzazz.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.ExportDialog

class ExportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)

        val exportButton = findViewById<Button>(R.id.buttonToExport)
        val instagram = findViewById<Button>(R.id.share_instagram)
        val kakaotalk = findViewById<Button>(R.id.share_kakaotalk)
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.test_5s)

        exportButton.setOnClickListener {
            val dialog = ExportDialog(this@ExportActivity)
            dialog.setCancelable(false)
            dialog.create()
            dialog.show()
        }

        instagram.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "video/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.setPackage("com.instagram.android")
            startActivity(intent)
        }

        kakaotalk.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "video/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.setPackage("com.kakao.talk")
            startActivity(intent)
        }
    }
}
