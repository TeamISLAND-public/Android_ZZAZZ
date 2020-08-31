package com.teamisland.zzazz.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import com.unity3d.player.UnityPlayer
import kotlinx.android.synthetic.main.loading_dialog.*
import kotlinx.coroutines.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

/**
 * Dialog for loading
 */
class LoadingDialog(context: Context, private val request: Int) :
    Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {

    // Variable for trim
    private var dataBinder: ITrimmingData? = null
    private var uri: Uri? = null

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
        window?.setBackgroundDrawable(ColorDrawable(getColor(context, R.color.LoadingBackground)))
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
                FFmpeg.cancel()
            dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("BlockingMethodInNonBlockingContext")
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
            val frameCount =
                (dataBinder.rangeExclusiveEndIndex - dataBinder.rangeStartIndex + 1).toInt()

            val start = dataBinder.startMs / 1000.0
            val end = dataBinder.endMs / 1000.0
            Thread {
                FFmpegDelegate.exec("-i $inPath -ss $start -t ${end - start} $outPath") {
                    if (it == Config.RETURN_CODE_SUCCESS)
                        percentage = 100
                }
            }.start()

            val command = "logcat -d -v process -t 1 mobile-ffmpeg:I *:S"
            val find = "frame"
            val pid = android.os.Process.myPid()
            var process = Runtime.getRuntime().exec(command)
            var reader = BufferedReader(InputStreamReader(process.inputStream))
            var currentLine: String?

            percentage = 0
            while (percentage < 100) {
                currentLine = reader.readLine()
                if (currentLine == null) {
                    process = Runtime.getRuntime().exec(command)
                    reader = BufferedReader(InputStreamReader(process.inputStream))
                    continue
                }
                if (currentLine.contains(java.lang.String.valueOf(pid))) {
                    if (currentLine.contains("$find=")) {
                        val arr1 = currentLine.split("$find=")
                        val arr2 = arr1[1].trim().split(" ")

                        if (percentage < 100 * arr2[0].toInt() / frameCount)
                            percentage = 100 * arr2[0].toInt() / frameCount
                        else
                            continue
                    }
                }
                progress.text = String.format("%02d", percentage) + "%"
                yield()
            }

            FFmpeg.execute("-i $inPath -ss ${dataBinder.startMs} -t ${dataBinder.endMs - dataBinder.startMs} ${context.filesDir.absolutePath}/audio.mp3")
            Intent(context, ProjectActivity::class.java).apply {
                putExtra(
                    TrimmingActivity.AUDIO_PATH,
                    context.filesDir.absolutePath + "/audio.mp3"
                )
                putExtra(
                    TrimmingActivity.IMAGE_PATH,
                    "${context.filesDir.absolutePath}/video_image"
                )
                putExtra(TrimmingActivity.VIDEO_FRAME_COUNT, frameCount)
                putExtra(
                    TrimmingActivity.VIDEO_DURATION,
                    (dataBinder.endMs - dataBinder.startMs + 1).toInt()
                )
            }.also { startActivity(context, it, null) }
            dismiss()
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
