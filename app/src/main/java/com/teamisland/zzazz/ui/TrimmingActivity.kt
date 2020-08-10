package com.teamisland.zzazz.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
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
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.LogMessage
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.teamisland.zzazz.BuildConfig
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.GetVideoData
import com.teamisland.zzazz.utils.IPositionChangeListener
import com.teamisland.zzazz.video_trimmer_library.interfaces.OnRangeSeekBarListener
import com.teamisland.zzazz.video_trimmer_library.interfaces.VideoTrimmingListener
import com.teamisland.zzazz.video_trimmer_library.utils.TrimVideoUtils
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
    private val dataSourceFactory: DataSource.Factory by lazy {
        DefaultDataSourceFactory(this, Util.getUserAgent(this, "PlayerSample"))
    }
    internal val player: SimpleExoPlayer by lazy {
        SimpleExoPlayer.Builder(this).build().also {
            it.repeatMode = SimpleExoPlayer.REPEAT_MODE_OFF
            mainVideoView.player = it
        }
    }

    internal fun stopVideo() {
        playButton.isActivated = false
        player.playWhenReady = false
    }

    private fun testVideoPositionInRange(): Boolean {
        val now = player.currentPosition.toInt()
        with(rangeSeekBarView) {
            return Range(getStart(), getEnd()).contains(now)
        }
    }

    internal fun testVideoRange() {
        val range = rangeSeekBarView.getRange()
        if (player.currentPosition.toInt() !in range) {
            val desiredPos = range.clamp(player.currentPosition.toInt())
            currentPositionView.setMarkerPos(desiredPos * 100.0 / videoDuration)
            player.seekTo(desiredPos.toLong())
        }
    }

    /**
     * [AppCompatActivity.dispatchTouchEvent]
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        start_trim.visibility = GONE
        return super.dispatchTouchEvent(ev)
    }

    /**
     * [AppCompatActivity.onCreate]
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimming)
        takePermission(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE))

        if (setupVideoProperties()) return

        val parentFolder = getExternalFilesDir(null) ?: return
        parentFolder.mkdirs()
        val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"
        trimmedVideoFile = File(parentFolder, fileName)

        backButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.alpha = 0.4f
                MotionEvent.ACTION_UP -> {
                    v.alpha = 1f
                    onBackPressed()
                }
            }
            true
        }

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

        framePlus.setOnClickListener {
            rangeSeekBarView.incrementThumbPos(rangeSeekBarView.currentThumb, 1)
            player.seekTo(player.currentPosition + 1)
            val pos = ((rangeSeekBarView.thumbs[rangeSeekBarView.currentThumb].pos - rangeSeekBarView.float2DP(12f)) / (rangeSeekBarView.viewWidth - 2 * rangeSeekBarView.float2DP(12f))).toDouble()
            currentPositionView.setMarkerPos(pos * 100)
        }

        frameMinus.setOnClickListener {
            rangeSeekBarView.incrementThumbPos(rangeSeekBarView.currentThumb, -1)
            player.seekTo(player.currentPosition - 1)
            val pos = ((rangeSeekBarView.thumbs[rangeSeekBarView.currentThumb].pos - rangeSeekBarView.float2DP(12f)) / (rangeSeekBarView.viewWidth - 2 * rangeSeekBarView.float2DP(12f))).toDouble()
            currentPositionView.setMarkerPos(pos * 100)
        }

        rangeSeekBarView.setButtons(framePlus, frameMinus)
        val addOnRangeSeekBarListener = object : OnRangeSeekBarListener {
            override fun onCreate(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                applyTrimRangeChanges()
            }

            override fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                applyTrimRangeChanges()
                testVideoRange()
                stopVideo()
            }

            override fun onSeekStart(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                applyTrimRangeChanges()
            }

            override fun onSeekStop(rangeSeekBarView: RangeSeekBarView, index: Int, value: Int) {
                applyTrimRangeChanges()
            }

            override fun onDeselect(rangeSeekBarView: RangeSeekBarView) = Unit
        }
        val count = GetVideoData.getFrameCount(this@TrimmingActivity, videoUri)
        rangeSeekBarView.setFrameCount(count)
        rangeSeekBarView.addOnRangeSeekBarListener(addOnRangeSeekBarListener)
        rangeSeekBarView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                stopVideo()
                val mThumb: RangeSeekBarView.Thumb
                val mThumb2: RangeSeekBarView.Thumb
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Remember where we started
                        rangeSeekBarView.currentThumb = rangeSeekBarView.getClosestThumb(event.x)
                        rangeSeekBarView.setButtonVisibility()
                        if (rangeSeekBarView.currentThumb == -1) {
                            rangeSeekBarView.onDeselect(rangeSeekBarView)
                            return false
                        }
                        mThumb = rangeSeekBarView.thumbs[rangeSeekBarView.currentThumb]
                        mThumb.lastTouchX = event.x
                        rangeSeekBarView.onSeekStart(rangeSeekBarView, rangeSeekBarView.currentThumb, mThumb.value)
                        currentPositionView.visibleTrimCurrent()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        mThumb = rangeSeekBarView.thumbs[rangeSeekBarView.currentThumb]
                        mThumb2 = rangeSeekBarView.thumbs[if (rangeSeekBarView.currentThumb == RangeSeekBarView.ThumbType.LEFT.index) RangeSeekBarView.ThumbType.RIGHT.index else RangeSeekBarView.ThumbType.LEFT.index]
                        // Calculate the distance moved
                        val dx = event.x - mThumb.lastTouchX
                        val newX = mThumb.pos + dx
                        val pos: Double

                        when (rangeSeekBarView.currentThumb) {
                            0 -> {
                                when {
                                    newX >= (mThumb2.pos - rangeSeekBarView.thumbWidth) -> mThumb.pos = mThumb2.pos - rangeSeekBarView.thumbWidth
                                    newX <= 0 -> mThumb.pos = 0f
                                    else -> {
                                        //Check if thumb is not out of max width
                                        //                            checkPositionThumb(mThumb, mThumb2, dx, true)
                                        // Move the object
                                        mThumb.pos = mThumb.pos + dx
                                        // Remember this touch position for the next move event
                                        mThumb.lastTouchX = event.x
                                    }
                                }
                                pos = (mThumb.pos / (rangeSeekBarView.viewWidth - 2 * rangeSeekBarView.float2DP(12f))).toDouble()
                            }
                            else -> {
                                when {
                                    newX <= (mThumb2.pos + rangeSeekBarView.thumbWidth) -> mThumb.pos = mThumb2.pos + rangeSeekBarView.thumbWidth
                                    newX >= (rangeSeekBarView.viewWidth - rangeSeekBarView.thumbWidth) -> mThumb.pos = (rangeSeekBarView.viewWidth - rangeSeekBarView.thumbWidth).toFloat()
                                    else -> {
                                        //Check if thumb is not out of max width
                                        //                        checkPositionThumb(mThumb2, mThumb, dx, false)
                                        // Move the object
                                        mThumb.pos = mThumb.pos + dx
                                        // Remember this touch position for the next move event
                                        mThumb.lastTouchX = event.x
                                    }
                                }
                                pos = ((mThumb.pos - rangeSeekBarView.float2DP(12f)) / (rangeSeekBarView.viewWidth - 2 * rangeSeekBarView.float2DP(12f))).toDouble()
                            }
                        }
                        player.seekTo((pos * videoDuration / 100).toLong())
                        currentPositionView.setMarkerPos(pos * 100)
                        Log.d("current", "pos: $pos, thumb1: ${mThumb.pos}, thumb2: ${mThumb2.pos}, current: ${currentPositionView.markerPos}")
                        currentPositionView.invalidate()
                        rangeSeekBarView.setThumbPos(rangeSeekBarView.currentThumb, mThumb.pos)
                    }
                    else -> {
                        currentPositionView.visibleMarkerCurrent()
                        if (rangeSeekBarView.currentThumb == -1)
                            return false
                        mThumb = rangeSeekBarView.thumbs[rangeSeekBarView.currentThumb]
                        rangeSeekBarView.onSeekStop(rangeSeekBarView, rangeSeekBarView.currentThumb, mThumb.value)
                    }
                }
                return true
            }
        })
        rangeSeekBarView.initMaxWidth()
        rangeSeekBarView.videoDuration = this.videoDuration

        currentPositionView.setRange(rangeSeekBarView)
        currentPositionView.setDuration(videoDuration)
        currentPositionView.setPlayer(player)
        val currentPositionChangeListener = object : IPositionChangeListener {
            /**
             * An event when current position changed.
             */
            override fun onChange(percentage: Double) {
                player.seekTo((percentage * videoDuration / 100).toLong())
                stopVideo()
            }
        }
        currentPositionView.setListener(currentPositionChangeListener)

        selectedThumbView.setRange(rangeSeekBarView)

        timeLineView.setVideo(videoUri)
        timeLineView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    currentPositionView.markerPos = (event.x * 100 / (currentPositionView.width - 2 * currentPositionView.float2DP(12f))).toDouble()
                    currentPositionView.markerPaint.color = 0xffff3898.toInt()
                    currentPositionView.textView.visibility = VISIBLE
                    currentPositionView.invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    currentPositionView.markerPaint.color = 0xccffffff.toInt()
                    currentPositionView.textView.visibility = GONE
                    currentPositionView.invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    currentPositionView.markerPos = (event.x * 100 / (currentPositionView.width - 2 * currentPositionView.float2DP(12f))).toDouble()
                    if (currentPositionView.markerPos < rangeSeekBarView.getStart() * 100.0 / videoDuration)
                        currentPositionView.markerPos = rangeSeekBarView.getStart() * 100.0 / videoDuration
                    if (currentPositionView.markerPos > rangeSeekBarView.getEnd() * 100.0 / videoDuration)
                        currentPositionView.markerPos = rangeSeekBarView.getEnd() * 100.0 / videoDuration
                    if (currentPositionView.markerPos < 0.0) currentPositionView.markerPos = 0.0
                    if (currentPositionView.markerPos > 100.0) currentPositionView.markerPos = 100.0
                    currentPositionView.listener.onChange(currentPositionView.markerPos)
                    player.seekTo((videoDuration * currentPositionView.markerPos / 100).toLong())
                    currentPositionView.invalidate()
                }
            }
            true
        }

        val mediaSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri)

        player.prepare(mediaSource)
        player.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    playButton.isActivated = false
                }
            }
        })
        frameLayout.setOnClickListener { playButton.startAnimation(fadeOut) }

        gotoProjectActivity.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.alpha = 0.4f
                MotionEvent.ACTION_UP -> {
                    v.alpha = 1f
                    startTrimming()
                }
            }
            true
        }

        Config.enableLogCallback { message: LogMessage ->
            if (BuildConfig.DEBUG) Log.d(Config.TAG, message.text)
        }
        println(videoUri.path)
        println(getPath(this, videoUri))
    }

    private fun startTrimming() {
        TrimVideoUtils.startTrim(
                this,
                videoUri,
                trimmedVideoFile,
                rangeSeekBarView.getStart().toLong(),
                rangeSeekBarView.getEnd().toLong(),
                GetVideoData.getDuration(this, videoUri).toLong(),
                object : VideoTrimmingListener {
                    override fun onVideoPrepared() = Unit

                    override fun onTrimStarted() = Unit

                    override fun onFinishedTrimming(uri: Uri?) {
                    }

                    override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
                    }
                }
        )
        Intent(this, ProjectActivity::class.java).apply {
            putExtra(VIDEO_FPS, videoFps)
            val startFrame = rangeSeekBarView.getFrameStart()
            val endFrame = rangeSeekBarView.getFrameEnd()

            putExtra(VIDEO_START_FRAME, startFrame)
            putExtra(VIDEO_END_FRAME, endFrame)

            putExtra(VIDEO_PATH, getPath(this@TrimmingActivity, Uri.parse(trimmedVideoFile.path)))
            startActivity(this)
        }
    }

    private fun setupVideoProperties(): Boolean {
        videoUri = intent.getParcelableExtra(IntroLoadActivity.VIDEO_URI) ?: return true
        videoDuration = GetVideoData.getDuration(this, videoUri)
        videoFps = GetVideoData.getFPS(this, videoUri)
        return false
    }

    private val playButtonClickListenerObject = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == ExoPlayer.STATE_READY)
                mainVideoView.postDelayed({ startVideo() }, 100)
        }
    }

    private fun playButtonClickHandler(fadeOut: Animation) {
        playButton.startAnimation(fadeOut)
        if (playButton.isActivated) {
            stopVideo()
        } else {
            if (!testVideoPositionInRange()) {
                val start = rangeSeekBarView.getStart()
                player.seekTo(start.toLong())
                currentPositionView.setMarkerPos(start * 100.0 / videoDuration)
                player.addListener(playButtonClickListenerObject)
                return
            }
            player.removeListener(playButtonClickListenerObject)
            startVideo()
        }
    }

    internal fun startVideo() {
        player.playWhenReady = true
        playButton.isActivated = true
        Thread(Runnable {
            do {
                val now = rangeSeekBarView.getRange().clamp(player.currentPosition.toInt())
                println(now)
                println(videoDuration)
                currentPositionView.setMarkerPos(now * 100.0 / videoDuration)
                if (rangeSeekBarView.getEnd() < player.currentPosition)
                    stopVideo()
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            } while (player.isPlaying)
        }).start()
    }

    internal fun applyTrimRangeChanges() {
        val seekDur = rangeSeekBarView.getEnd() - rangeSeekBarView.getStart()
        gotoProjectActivity.isEnabled = seekDur <= resources.getInteger(R.integer.length_limit)
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
