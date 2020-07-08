package com.teamisland.zzazz.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.lb.video_trimmer_library.interfaces.VideoTrimmingListener
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.VideoIntent
import kotlinx.android.synthetic.main.activity_trimming.*
import java.io.File

/**
 * Activity for video trimming.
 */
class TrimmingActivity : AppCompatActivity(), VideoTrimmingListener {

    private var video_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimming)

        val intent = Intent(this, ProjectActivity::class.java)
        // This should be edited.
        // duration is duration of video, uri is uri parse of video
        val value =
            VideoIntent(
                5184,
                "android.resource://" + packageName + "/" + R.raw.test_5s
            )
        intent.putExtra("value", value)

        video_uri = getIntent().getParcelableExtra(getString(R.string.selected_video_uri))
        gotoProjectActivity.setOnClickListener { startActivity(intent) }

        videoTrimmer.setMaxDurationInMs(resources.getInteger(R.integer.length_limit))
        videoTrimmer.setOnK4LVideoListener(this)
        takePermission()
        val parentFolder = getExternalFilesDir(null) ?: return
        parentFolder.mkdirs()
        val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"
        val trimmedVideoFile = File(parentFolder, fileName)
        videoTrimmer.setDestinationFile(trimmedVideoFile)
        videoTrimmer.setVideoURI(video_uri ?: return)
        videoTrimmer.setVideoInformationVisibility(true)
        backButton.setOnClickListener { onBackPressed() }
    }

    private fun takePermission() {
        if (ActivityCompat.checkSelfPermission(
                this@TrimmingActivity,
                READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this@TrimmingActivity,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                val builder = AlertDialog.Builder(this@TrimmingActivity)
                builder.setMessage("Permission please...")
                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    ActivityCompat.requestPermissions(
                        this@TrimmingActivity,
                        arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE),
                        1
                    )
                }
                builder.setNegativeButton(android.R.string.cancel, null)
                builder.show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        }
    }

    override fun onVideoPrepared() {
        Toast.makeText(this@TrimmingActivity, "Video is prepared!", Toast.LENGTH_SHORT).show()
    }

    override fun onTrimStarted() {
        //trimmingProgressView.visibility = View.VISIBLE
    }

    /**
     * @param uri the result, trimmed video, or null if failed
     */
    override fun onFinishedTrimming(uri: Uri?) {
        //trimmingProgressView.visibility = View.GONE
        if (uri == null) {
            Toast.makeText(this@TrimmingActivity, "failed trimming", Toast.LENGTH_SHORT).show()
        } else {
            val msg = getString(R.string.video_saved_at, uri.path)
            Toast.makeText(this@TrimmingActivity, msg, Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setDataAndType(uri, "video/mp4")
            startActivity(intent)
        }
        finish()
    }

    /**
     * check_green {[android.media.MediaPlayer.OnErrorListener]}
     */
    override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
        //trimmingProgressView.visibility = View.GONE
        Toast.makeText(this@TrimmingActivity, "error while previewing video", Toast.LENGTH_SHORT)
            .show()
    }
}
