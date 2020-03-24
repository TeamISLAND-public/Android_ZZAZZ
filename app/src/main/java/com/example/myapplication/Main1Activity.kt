package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main1.*

class Main1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)

        goto2.setOnClickListener {
            startActivity(Intent(this@Main1Activity, Main2Activity::class.java))
        }

        goto3.setOnClickListener {
            startActivity(Intent(this@Main1Activity, Main3Activity::class.java))
        }
    }
}
