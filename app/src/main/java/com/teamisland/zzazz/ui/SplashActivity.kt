package com.teamisland.zzazz.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.teamisland.zzazz.R
import kotlinx.android.synthetic.main.activity_splash.*
import java.io.File

/**
 * Splash for start application zzazz
 */
class SplashActivity : AppCompatActivity() {

    private val sharedPreferences by lazy { getSharedPreferences("Pref", MODE_PRIVATE) }

    /**
     * When the activity is created
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.navigationBarColor = getColor(R.color.Background)

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("token", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get token
                Log.d("asdfasdf", task.result?.token ?: return@OnCompleteListener)
            })

        val files = filesDir
        files.listFiles()?.let {
            for (file in it)
                if (file.extension in arrayOf("mp4", "mp3"))
                    file.delete()
        }
        val images = File(filesDir, "/video_image")
        images.listFiles()?.let {
            for (image in it)
                image.delete()
        }

        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        fadeOut.startOffset = 1000
        fadeOut.duration = 500
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) = Unit
            override fun onAnimationRepeat(animation: Animation?) = Unit

            override fun onAnimationEnd(animation: Animation?) {
                splash.visibility = View.GONE

                if (sharedPreferences.getBoolean("isFirstRun", true)) {
                    Intent(
                        this@SplashActivity,
                        PermissionActivity::class.java
                    ).also { startActivity(it) }
                    sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
                } else
                    Intent(
                        this@SplashActivity,
                        IntroActivity::class.java
                    ).also { startActivity(it) }
                finish()
            }
        })

        splash.startAnimation(fadeOut)
    }
}