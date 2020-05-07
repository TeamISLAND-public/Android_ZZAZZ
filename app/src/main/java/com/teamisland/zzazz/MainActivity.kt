package com.teamisland.zzazz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main1.*

/**
 * Main activity of the app.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)

        video_selection.setOnClickListener {
            startActivity(Intent(this@MainActivity, BeforeSelectionActivity::class.java))
        }
    }
}
