package com.teamisland.zzazz.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.util.Range
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.LogMessage
import com.teamisland.zzazz.BuildConfig
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.GetVideoData
import com.teamisland.zzazz.utils.IPositionChangeListener
import com.teamisland.zzazz.video_trimmer_library.interfaces.OnRangeSeekBarListener
import com.teamisland.zzazz.video_trimmer_library.view.RangeSeekBarView
import kotlinx.android.synthetic.main.activity_trimming.*
import java.io.File

/**
 * Activity for video trimming.
 */
class TrimmingActivity : AppCompatActivity() {

    companion object {
        /**
         * Uri of the trimmed video.
         */
        const val VIDEO_PATH: String = "PATH"

        /**
         * FPS of the trimmed video.
         */
        const val VIDEO_FPS: String = "FPS"

        /**
         * Starting frame index of the trimmed video.
         */
        const val VIDEO_START_FRAME: String = "START_F"

        /**
         * End frame index of the trimmed video.
         */
        const val VIDEO_END_FRAME: String = "END_F"

        /**
         * Get a file path from a Uri. This will get the the path for Storage Access
         * Framework Documents, as well as the _data field for the MediaStore and
         * other file-based ContentProviders.
         *
         * @param context The context.
         * @param uri The Uri to query.
         * @author paulburke
         */
        internal fun getPath(context: Context, uri: Uri): String? {
            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }

                    // handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    when (type) {
                        "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(
                        context,
                        contentUri ?: return null,
                        selection,
                        selectionArgs
                    )
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                return getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        /**
         * Get the value of the data column for this Uri. This is useful for
         * MediaStore Uris, and other file-based ContentProviders.
         *
         * @param context The context.
         * @param uri The Uri to query.
         * @param selection (Optional) Filter used in the query.
         * @param selectionArgs (Optional) Selection arguments used in the query.
         * @return The value of the _data column, which is typically a file path.
         */
        private fun getDataColumn(
            context: Context, uri: Uri, selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                cursor = context.contentResolver.query(
                    uri, projection, selection, selectionArgs,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex: Int = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(columnIndex)
                }
            } finally {
                cursor?.close()
            }
            return null
        }


        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        private fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }
    }

    private lateinit var videoUri: Uri
    private lateinit var trimmedVideoFile: File
    internal var videoDuration: Int = 0
    private var videoFps: Int = 0

    internal fun stopVideo() {
        playButton.isActivated = false
        mainVideoView.pause()
    }

    private fun testVideoPositionInRange(): Boolean {
        val now = mainVideoView.currentPosition
        with(rangeSeekBarView) {
            return Range(getStart(), getEnd()).contains(now)
        }
    }

    internal fun testVideoRange() {
        val range = rangeSeekBarView.getRange()
        if (mainVideoView.currentPosition !in range) {
            val desiredPos = range.clamp(mainVideoView.currentPosition)
            currentPositionView.setMarkerPos(desiredPos * 100.0 / videoDuration)
            mainVideoView.seekTo(desiredPos)
        }
    }

    /**
     * [AppCompatActivity.onCreate]
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimming)
        takePermission(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE))

        if (setupVideoProperties()) return

        val parentFolder = getExternalFilesDir(null) ?: return
        parentFolder.mkdirs()
        val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"
        trimmedVideoFile = File(parentFolder, fileName)

        backButton.setOnClickListener { onBackPressed() }

        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        fadeOut.startOffset = 1000
        fadeOut.duration = 500
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                playButton.visibility = VISIBLE
            }

            override fun onAnimationEnd(animation: Animation?) {
                playButton.visibility = GONE
            }

            override fun onAnimationStart(animation: Animation?) {
                playButton.visibility = VISIBLE
            }
        })

        playButton.setOnClickListener { playButtonClickHandler(fadeOut) }
        playButton.startAnimation(fadeOut)

        rangeSeekBarView.setButtons(framePlus, frameMinus)
        val addOnRangeSeekBarListener = object : OnRangeSeekBarListener {
            override fun onCreate(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                println("onCreate")
                trimText.visibility = GONE
                applyTrimRangeChanges()
            }

            override fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                println("onSeek")
                trimText.visibility = VISIBLE
                applyTrimRangeChanges()
                testVideoRange()
                stopVideo()
            }

            override fun onSeekStart(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                println("onSeekStart")
                trimText.visibility = VISIBLE
                applyTrimRangeChanges()
            }

            override fun onSeekStop(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                println("onSeekStop")
                trimText.visibility = GONE
                applyTrimRangeChanges()
            }

            override fun onDeselect(rangeSeekBarView: RangeSeekBarView) = Unit
        }
        val count = GetVideoData.getFrameCount(this, videoUri)
        rangeSeekBarView.setFrameCount(count)
        rangeSeekBarView.addOnRangeSeekBarListener(addOnRangeSeekBarListener)
        rangeSeekBarView.initMaxWidth()
        rangeSeekBarView.videoDuration = videoDuration
        println("asdfasdf")
        println(rangeSeekBarView.getStart())
        println(rangeSeekBarView.getEnd())
        println(count)
        println(videoDuration)

        currentPositionView.setDuration(videoDuration)
        val currentPositionChangeListener = object : IPositionChangeListener {
            /**
             * An event when current position changed.
             */
            override fun onChange(percentage: Double) {
                mainVideoView.seekTo((percentage * videoDuration / 100).toInt())
                stopVideo()
            }
        }
        currentPositionView.setListener(currentPositionChangeListener)
        currentPositionView.setRange(rangeSeekBarView)

        selectedThumbView.setRange(rangeSeekBarView)

        timeLineView.setVideo(videoUri)

        mainVideoView.setVideoURI(videoUri)
        mainVideoView.setOnPreparedListener {
            mainVideoView.start()
            mainVideoView.postDelayed({ mainVideoView.pause() }, 100)
        }
        mainVideoView.setOnCompletionListener { playButton.isActivated = false }
        mainVideoView.setOnClickListener { playButton.startAnimation(fadeOut) }

        gotoProjectActivity.setOnClickListener { startTrimming() }

        Config.enableLogCallback { message: LogMessage ->
            if (BuildConfig.DEBUG) Log.d(Config.TAG, message.text)
        }
        println(videoUri.path)
        println(getPath(this, videoUri))
    }

    private fun startTrimming() {
        Intent(this, ProjectActivity::class.java).apply {
            putExtra(VIDEO_FPS, videoFps)
            val startFrame = rangeSeekBarView.getFrameStart()
            val endFrame = rangeSeekBarView.getFrameEnd()

            putExtra(VIDEO_START_FRAME, startFrame)
            putExtra(VIDEO_END_FRAME, endFrame)

            putExtra(VIDEO_PATH, getPath(this@TrimmingActivity, videoUri))
            startActivity(this)
        }
//        TrimVideoUtils.startTrim(
//            this,
//            videoUri,
//            trimmedVideoFile,
//            rangeSeekBarView.getStart().toLong(),
//            rangeSeekBarView.getEnd().toLong(),
//            GetVideoData.getDuration(this, videoUri).toLong(),
//            object : VideoTrimmingListener {
//                override fun onVideoPrepared() = Unit
//
//                override fun onTrimStarted() = Unit
//
//                override fun onFinishedTrimming(uri: Uri?) {
//                }
//
//                override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
//                    Toast.makeText(
//                        this@TrimmingActivity,
//                        getString(R.string.trimming_error),
//                        LENGTH_SHORT
//                    ).show()
//                }
//            }
//        )
    }

    private fun setupVideoProperties(): Boolean {
        videoUri = intent.getParcelableExtra(IntroActivity.VIDEO_URI) ?: return true
        videoDuration = GetVideoData.getDuration(this, videoUri)
        videoFps = GetVideoData.getFPS(this, videoUri)
        return false
    }

    private fun playButtonClickHandler(fadeOut: Animation) {
        playButton.startAnimation(fadeOut)
        if (playButton.isActivated) {
            playButton.isActivated = false
            mainVideoView.pause()
        } else {
            playButton.isActivated = true
            if (!testVideoPositionInRange())
                mainVideoView.seekTo(rangeSeekBarView.getStart())
            mainVideoView.start()
            Thread(Runnable {
                do {
                    val now = rangeSeekBarView.getRange().clamp(mainVideoView.currentPosition)
                    with(currentPositionView) {
                        setMarkerPos(now * 100.0 / videoDuration)
                    }
                    if (rangeSeekBarView.getEnd() < mainVideoView.currentPosition)
                        stopVideo()
                    try {
                        Thread.sleep(10)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                } while (mainVideoView.isPlaying)
            }).start()
        }
    }

    internal fun applyTrimRangeChanges() {
        val seekDur = rangeSeekBarView.getEnd() - rangeSeekBarView.getStart()
        if (seekDur <= resources.getInteger(R.integer.length_limit)) {
            val totalSeconds = seekDur / 1000
            val seconds = totalSeconds % 60
            val minutes = totalSeconds / 60 % 60
            trimText.text =
                java.util.Formatter().format("%02d:%02d Trimmed", minutes, seconds).toString()
            gotoProjectActivity.isEnabled = true
        } else {
            trimText.text = getString(R.string.video_too_long)
            gotoProjectActivity.isEnabled = false
        }
    }

    private fun hasAllPermission(permission: Array<String>): Boolean {
        return permission.all {
            ActivityCompat.checkSelfPermission(this, it) == PERMISSION_GRANTED
        }
    }

    private fun requestPermission(permission: Array<String>) {
        ActivityCompat.requestPermissions(this, permission, 1)
    }

    private fun takePermission(permission: Array<String>) {
        if (hasAllPermission(permission)) return

        if (permission.any { ActivityCompat.shouldShowRequestPermissionRationale(this, it) }) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Permission is needed to read & write the video.")
            builder.setPositiveButton(android.R.string.ok) { _, _ -> requestPermission(permission) }
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.show()
        } else {
            requestPermission(permission)
        }
    }
}
