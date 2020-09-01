package com.teamisland.zzazz.ui

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.teamisland.zzazz.R
import kotlinx.android.synthetic.main.activity_splash.*
import java.io.File


/**
 * Splash for start application zzazz
 */
class SplashActivity : AppCompatActivity() {

    private val sharedPreferences by lazy { getSharedPreferences("Pref", MODE_PRIVATE) }
    private lateinit var mAppUpdateManager: AppUpdateManager

    private val update = 1

    /**
     * When the activity is created
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.navigationBarColor = getColor(R.color.Background)

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

        mAppUpdateManager = AppUpdateManagerFactory.create(applicationContext)

        mAppUpdateManager.registerListener {
            if (it.installStatus() == InstallStatus.DOWNLOADED)
                mAppUpdateManager.completeUpdate() // If user call this, app will be updated
        }
        val appUpdateInfoTask = mAppUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                if (it.isUpdateTypeAllowed(
                        AppUpdateType.IMMEDIATE
                    )
                ) {
                    try {
                        mAppUpdateManager.startUpdateFlowForResult(
                            it,
                            AppUpdateType.IMMEDIATE,
                            this,
                            update
                        )
                    } catch (exception: SendIntentException) {
                        Log.e("AppUpdater", "AppUpdateManager Error", exception)
                        exception.printStackTrace()
                    }
                } else if (it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    try {
                        mAppUpdateManager.startUpdateFlowForResult(
                            it,
                            AppUpdateType.FLEXIBLE,
                            this,
                            update
                        )
                    } catch (exception: SendIntentException) {
                        Log.e("AppUpdater", "AppUpdateManager Error", exception)
                        exception.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * When the activity is resumed
     */
    override fun onResume() {
        super.onResume()
        mAppUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                        it,
                        AppUpdateType.IMMEDIATE,
                        this,
                        update
                    )
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            }
        }

    }

    /**
     * When the activity get result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == update)
            if (resultCode != RESULT_OK) {
                Log.d("AppUpdate", "Update flow failed! Result code: $resultCode")
                // TODO: Show Dialog.
                finishAffinity()
            }
    }
}