package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        goto2.setOnClickListener {
            val int = Intent(this@MainActivity, Main2Activity::class.java)
            startActivity(int)
        }

        goto3.setOnClickListener {
            val int = Intent(this@MainActivity, Main3Activity::class.java)
            startActivity(int)
        }
    }
}
