package com.teamisland.zzazz.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.teamisland.zzazz.R
import kotlinx.android.synthetic.main.activity_splash.*

/**
 * Splash for start application zzazz
 */
class SplashActivity : AppCompatActivity() {

    /**
     * When the activity is created
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.navigationBarColor = getColor(R.color.Background)

        val intent = Intent(this, IntroActivity::class.java)

        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        fadeOut.startOffset = 1000
        fadeOut.duration = 1000
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                splash.visibility = View.GONE
                startActivity(intent)
                finish()
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })

        splash.startAnimation(fadeOut)
    }
}