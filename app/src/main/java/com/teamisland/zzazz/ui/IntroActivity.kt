package com.teamisland.zzazz.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.dialog.GoToSettingDialog
import com.teamisland.zzazz.utils.PermissionManager
import com.teamisland.zzazz.utils.objects.UnitConverter.float2DP
import kotlinx.android.synthetic.main.activity_intro.*
import java.util.*

/**
 * Main activity of Intro Activity
 */
class IntroActivity : AppCompatActivity() {

    companion object {
        /**
         * Uri of the video retrieved.
         */
        const val VIDEO_URI: String = "origin_video"

        private const val LOAD_VIDEO = 1

        private const val TAKE_VIDEO = 2
    }

    private lateinit var random: Random
    private lateinit var textArray: Array<String>
    private lateinit var circle: ImageView
    private lateinit var underline: ImageView

    private val permissionManager = PermissionManager(this, this)

    @Suppress("SameParameterValue")
    private fun getVideo(requestCode: Int) {
        if (requestCode == LOAD_VIDEO) {
            Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).apply {
                type = "video/*"
                startActivityForResult(this, requestCode)
            }
        } else if (requestCode == TAKE_VIDEO) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (!shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                    val dialog =
                        GoToSettingDialog(this, this, permissionManager, GoToSettingDialog.CAMERA)
                    dialog.create()
                    dialog.show()
                } else
                    requestPermissions(arrayOf(android.Manifest.permission.CAMERA), TAKE_VIDEO)
            } else
                Intent(MediaStore.ACTION_VIDEO_CAPTURE).also {
                    startActivityForResult(
                        it,
                        requestCode
                    )
                }
        }
    }

    /**
     * [AppCompatActivity.onResume]
     */
    override fun onResume() {
        super.onResume()
        setRandomText()
    }

    /**
     * [AppCompatActivity.onCreate]
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        window.navigationBarColor = getColor(R.color.Background)

        random = Random()
        textArray = arrayOf(
            getString(R.string.intro1),
            getString(R.string.intro2),
            getString(R.string.intro3),
            getString(R.string.intro4),
            getString(R.string.intro5)
        )

        checkPermission()

        circle = ImageView(this).apply {
            setImageResource(R.drawable.circle_point)
            id = View.generateViewId()
        }
        underline = ImageView(this).apply {
            setImageResource(R.drawable.underline)
            id = View.generateViewId()
        }
        constraintLayout.apply {
            addView(circle)
            addView(underline)
        }

        val shrink = AnimationUtils.loadAnimation(this, R.anim.shrink)
        zzazz.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    take.visibility = View.INVISIBLE
                    v.startAnimation(shrink)
                }
                MotionEvent.ACTION_UP -> {
                    getVideo(LOAD_VIDEO)
                    v.clearAnimation()
                    take.visibility = View.VISIBLE
                }
            }
            true
        }

        take.setOnClickListener { getVideo(TAKE_VIDEO) }

        val bounce = AnimationUtils.loadAnimation(this, R.anim.bounce)
        linearLayout.startAnimation(bounce)
    }

    private fun setRandomText() {
        val index = random.nextInt(textArray.size - 1)
        random_text.text = textArray[index]

        when (index) {
            0 -> {
                setPosition(circle.id, 116, 7)
                setPosition(underline.id, 0, 135)
            }
            1 -> {
                setPosition(circle.id, 24, 51)
                setPosition(underline.id, 0, 135)
            }
            2 -> {
                setPosition(circle.id, 136, 7)
                setPosition(underline.id, 0, 135)
            }
            3 -> {
                setPosition(circle.id, 0, 7)
                setPosition(underline.id, 0, 135)
            }
            4 -> {
                setPosition(circle.id, 162, 7)
                setPosition(underline.id, 48, 135)
            }
        }
    }

    private fun checkPermission() {
        if (!permissionManager.checkPermission())
            permissionManager.requestPermission()
    }

    /**
     * [AppCompatActivity.onRequestPermissionsResult]
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == TAKE_VIDEO)
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!permissionManager.permissionResult(requestCode, permissions, grantResults)) {
            val dialog = GoToSettingDialog(this, this, permissionManager, GoToSettingDialog.STORAGE)
            dialog.create()
            dialog.show()
        }
    }

    private fun setPosition(
        id: Int,
        startToStartMargin: Int,
        topToTopMargin: Int
    ) {
        ConstraintSet().apply {
            clone(constraintLayout)
            connect(
                id,
                ConstraintSet.START,
                random_text.id,
                ConstraintSet.START,
                float2DP(startToStartMargin.toFloat(), resources).toInt()
            )
            connect(
                id,
                ConstraintSet.TOP,
                random_text.id,
                ConstraintSet.TOP,
                float2DP(topToTopMargin.toFloat(), resources).toInt()
            )
        }.applyTo(constraintLayout)
    }

    /**
     * Retrieve uri from request. Checks whether the uri is valid under the restrictions.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        val videoUri = when (requestCode) {
            LOAD_VIDEO, TAKE_VIDEO -> (data ?: return).data ?: return
            else -> null
        }

        if (videoUri != null) {
            Intent(this, TrimmingActivity::class.java).apply {
                putExtra(VIDEO_URI, videoUri)
            }.also { startActivity(it) }
        }
    }
}
