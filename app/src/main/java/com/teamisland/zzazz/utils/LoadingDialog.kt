package com.teamisland.zzazz.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Window
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.startActivity
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.bumptech.glide.Glide
import com.teamisland.zzazz.R
import com.teamisland.zzazz.ui.ExportActivity
import com.teamisland.zzazz.ui.ProjectActivity
import com.teamisland.zzazz.ui.TrimmingActivity
import com.teamisland.zzazz.inference.PoseEstimation
import com.unity3d.player.UnityPlayer
import kotlinx.android.synthetic.main.loading_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.properties.Delegates

/**
 * Dialog for loading
 */
class LoadingDialog(context: Context, private val request: Int) :
    Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {

    // Variable for trim
    private var dataBinder: ITrimmingData? = null
    private var uri: Uri? = null
    private var poseEstimation = PoseEstimation(context)

    // Variable for export
    private var fps by Delegates.notNull<Float>()
    private var resultPath: String = ""
    private var audioPath: String = ""
    private var imagePath: String = ""
    private var capturePath: String = ""

    // Variable for save
    private var input: InputStream? = null
    private var output: FileOutputStream? = null

    private var percentage: Int = 0

    /**
     * Constructor for [TRIM]
     */
    constructor(
        context: Context,
        request: Int,
        dataBinder: ITrimmingData,
        uri: Uri
    ) : this(
        context,
        request
    ) {
        this.dataBinder = dataBinder
        this.uri = uri
    }

    /**
     * Constructor for [EXPORT]
     */
    constructor(
        context: Context,
        request: Int,
        imagePath: String,
        fps: Float,
        resultPath: String
    ) : this(context, request) {
        this.imagePath = imagePath
        this.fps = fps
        this.resultPath = resultPath
    }

    companion object {
        /**
         * Trim video.
         */
        const val TRIM: Int = 0

        /**
         * Export video.
         */
        const val EXPORT: Int = 1

        /**
         * Save video.
         */
        const val SAVE: Int = 2
    }

    /**
     * [Dialog.onCreate]
     */
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_dialog)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(getColor(context, R.color.DialogBackground)))
        window?.setGravity(Gravity.CENTER)

        Glide.with(context).load(R.drawable.loading).into(load_gif)
        progress.text = String.format("%02d", percentage) + "%"

        val job: Job
        when (request) {
            TRIM -> {
                text.text = context.getString(R.string.trim_video)
                job = trimVideo(dataBinder ?: return, uri ?: return)
            }
            EXPORT -> {
                text.text = context.getString(R.string.export_video)
                job = exportVideo()
            }
            SAVE -> {
                text.text = context.getString(R.string.save_video)
                job = saveVideo()
            }
            else -> {
                dismiss()
                return
            }
        }

        cancel.setOnClickListener {
            job.cancel()
            if (request == SAVE) {
                input?.close()
                output?.flush()
                output?.close()
            } else
            dismiss()
        }
    }

    private fun trimVideo(dataBinder: ITrimmingData, uri: Uri): Job =
        CoroutineScope(Dispatchers.IO).launch {
            val inPath = AbsolutePathRetriever.getPath(context, uri) ?: return@launch
            val parentPath = context.filesDir.absolutePath + "/video_image"
            val outPath = run {
                val parentFolder = File(parentPath)
                if (!parentFolder.exists())
                    parentFolder.mkdir()
                val fileName = "img%08d.png"
                File(parentFolder, fileName)
            }.absolutePath
            FFmpegDelegate.trimVideo(inPath, dataBinder.startMs, dataBinder.endMs, outPath) { i ->
                if (i == Config.RETURN_CODE_SUCCESS) {
                    FFmpeg.execute("-i $inPath -ss ${dataBinder.startMs} -t ${dataBinder.endMs - dataBinder.startMs} ${context.filesDir.absolutePath}/audio.mp3")
                    inferenceVideo(dataBinder, parentPath)
                    Intent(context, ProjectActivity::class.java).apply {
                        putExtra(
                            TrimmingActivity.AUDIO_PATH,
                            context.filesDir.absolutePath + "/audio.mp3"
                        )
                        putExtra(
                            TrimmingActivity.IMAGE_PATH,
                            "${context.filesDir.absolutePath}/video_image"
                        )
                        putExtra(
                            TrimmingActivity.VIDEO_FRAME_COUNT,
                            (dataBinder.rangeExclusiveEndIndex - dataBinder.rangeStartIndex + 1).toInt()
                        )
                        putExtra(
                            TrimmingActivity.VIDEO_DURATION,
                            (dataBinder.endMs - dataBinder.startMs + 1).toInt()
                        )
                    }.also { startActivity(context, it, null) }
                }
            }
            Log.d("putExtra", "%d".format((dataBinder.endMs - dataBinder.startMs + 1).toInt()))
            dismiss()
        }

    private fun inferenceVideo(dataBinder: ITrimmingData, path: String){
            Log.d("path", "%s".format(path))
            val bitmapList = ArrayList<Bitmap?>()
            val frameCount = (dataBinder.rangeExclusiveEndIndex - dataBinder.rangeStartIndex + 1).toInt()
            bitmapList.clear()
//            BackgroundExecutor.cancelAll("", true)
//            BackgroundExecutor.execute(object : BackgroundExecutor.Task("", 0L, "") {
//                override fun execute() {
//                    try {
            for (i in 0 until frameCount) {
                var bitmap: Bitmap? =
                    BitmapFactory.decodeFile(path + "/img%08d.png".format(i + 1))
                if (bitmap == null)
                    Log.d("bitmap", "has no bit map")
                bitmapList.add(bitmap)
                if (bitmap != null) {
                    Log.d("bitmap_test", String.format("size: %d %d", bitmap.rowBytes, bitmap.height))
//                    poseEstimation.estimatePose(bitmap)
                    poseEstimation.estimatePose(Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888))
                }
            }
            Log.d("arraylistsize", "%d".format(bitmapList.size))
            Log.d("frame_count", "%d".format(frameCount))
        }

        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun exportVideo(): Job =
        CoroutineScope(Dispatchers.Default).launch {
            File(context.filesDir.absolutePath + "/result.mp4").delete()

            capturePath = context.filesDir.absolutePath + "/capture_image"
            val captureFile = File(capturePath)
            if (!captureFile.exists())
                captureFile.mkdir()

            UnityPlayer.UnitySendMessage(
                ProjectActivity.PLAY_MANAGER,
                ProjectActivity.EXPORT,
                "$imagePath:$capturePath"
            )
        }

    /**
     * Called in Unity.
     */
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "unused")
    fun encodeVideo() {
        CoroutineScope(Dispatchers.Default).launch {
            // get result video
            Log.d("Export", "Convert the images to a video and Combine with audio.")
            FFmpeg.execute("-i $capturePath/img%08d.png -i $audioPath -r $fps -pix_fmt yuv420p $resultPath")

            File(audioPath).delete()
            for (img in File(imagePath).listFiles())
                img.delete()
            for (img in File(capturePath).listFiles())
                img.delete()
//        File(videoPath).delete()
            dismiss()
            Intent(context, ExportActivity::class.java).apply {
                Log.d("path", resultPath)
                putExtra("RESULT", resultPath)
                startActivity(context, this, null)
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun saveVideo(): Job =
        CoroutineScope(Dispatchers.IO).launch {
            //Video name is depended by time
            val time = System.currentTimeMillis()
            val date = Date(time)
            val nameFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
            val filename = nameFormat.format(date) + ".mp4"
            dismiss()
        }
}
