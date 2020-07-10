package com.teamisland.zzazz.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.teamisland.zzazz.R
import com.teamisland.zzazz.video_trimmer_library.interfaces.VideoTrimmingListener
import com.teamisland.zzazz.video_trimmer_library.utils.TrimVideoUtils
import kotlinx.android.synthetic.main.activity_trimming.*
import java.io.File

/**
 * Activity for video trimming.
 */
class TrimmingActivity : AppCompatActivity(), VideoTrimmingListener {

    private lateinit var videoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimming)
        videoUri = intent.getParcelableExtra(IntroActivity.VIDEO_URI) ?: return
        takePermission()
        val parentFolder = getExternalFilesDir(null) ?: return
        parentFolder.mkdirs()
        val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"
        val trimmedVideoFile = File(parentFolder, fileName)
        backButton.setOnClickListener { onBackPressed() }
        rangeSeekBarView.setButtons(framePlus, frameMinus)
        timeLineView.setVideo(videoUri)
        gotoProjectActivity.setOnClickListener {
            TrimVideoUtils.startTrim(this, videoUri, trimmedVideoFile, start, end, dur, this)
        }
    }

    /**
     * Goes back to IntroActivity
     */
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, IntroActivity::class.java))
    }

    private fun hasNoPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this@TrimmingActivity,
            permission
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permissionArray: Array<String>) {
        ActivityCompat.requestPermissions(
            this@TrimmingActivity,
            permissionArray,
            1
        )
    }

    private fun takePermission() {
        if (hasNoPermission(READ_EXTERNAL_STORAGE) || hasNoPermission(WRITE_EXTERNAL_STORAGE)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                val builder = AlertDialog.Builder(this@TrimmingActivity)
                builder.setMessage("Permission is needed to read & write the video.")
                builder.setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                    requestPermission(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE))
                }
                builder.setNegativeButton(android.R.string.cancel, null)
                builder.show()
            } else {
                requestPermission(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE))
            }
        }
    }

    override fun onVideoPrepared() {
        TODO("Not yet implemented")
    }

    override fun onTrimStarted() {
        TODO("Not yet implemented")
    }

    /**
     * @param uri the result, trimmed video, or null if failed
     */
    override fun onFinishedTrimming(uri: Uri?) {
        TODO("Not yet implemented")
    }

    /**
     * check [android.media.MediaPlayer.OnErrorListener]
     */
    override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
        TODO("Not yet implemented")
    }
}
