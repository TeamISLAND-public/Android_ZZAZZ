package com.teamisland.zzazz.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.teamisland.zzazz.R
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val intent = Intent(this, IntroActivity::class.java)

        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        fadeOut.startOffset = 0
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                splash.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
                Handler().postDelayed(Runnable {
                    startActivity(intent)
                    finish()
                }, 1000)
            }

        })

        splash.startAnimation(fadeOut)
    }
}