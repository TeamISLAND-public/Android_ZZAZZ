
package com.teamisland.zzazz.utils.dialog

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
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.startActivity
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.bumptech.glide.Glide
import com.teamisland.zzazz.R
import com.teamisland.zzazz.ui.ExportActivity
import com.teamisland.zzazz.ui.ProjectActivity
import com.teamisland.zzazz.ui.TrimmingActivity.Companion.AUDIO_PATH
import com.teamisland.zzazz.ui.TrimmingActivity.Companion.IMAGE_PATH
import com.teamisland.zzazz.ui.TrimmingActivity.Companion.MODEL_OUTPUT
import com.teamisland.zzazz.ui.TrimmingActivity.Companion.VIDEO_DURATION
import com.teamisland.zzazz.ui.TrimmingActivity.Companion.VIDEO_FRAME_COUNT
import com.teamisland.zzazz.utils.inference.*
import com.teamisland.zzazz.utils.interfaces.ITrimmingData
import com.teamisland.zzazz.utils.interfaces.UnityDataBridge
import com.teamisland.zzazz.utils.objects.AbsolutePathRetriever
import com.teamisland.zzazz.utils.objects.FFmpegDelegate
import kotlinx.android.synthetic.main.loading_dialog.*
import kotlinx.coroutines.*
import java.io.*
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

    // Variable for inference
    private var personList = ArrayList<Person?>()
    private var poseEstimation = PoseEstimation(context)
    private var height = 256
    private var width = 256

    // Variable for export
    private var frameCount by Delegates.notNull<Int>()
    private var fps by Delegates.notNull<Float>()
    private var resultPath: String = ""
    private var audioPath: String = ""
    private var capturePath: String = ""
    private var unityDataBridge: UnityDataBridge? = null
    private var isUnity = true

    // Variable for save
    private var input: InputStream? = null
    private var output: FileOutputStream? = null

    private var percentage: Int = 0
    private var job: Job? = null

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
        capturePath: String,
        audioPath: String,
        frameCount: Int,
        fps: Float,
        resultPath: String,
        unityDataBridge: UnityDataBridge?
    ) : this(context, request) {
        this.capturePath = capturePath
        this.audioPath = audioPath
        this.frameCount = frameCount
        this.fps = fps
        this.resultPath = resultPath
        this.unityDataBridge = unityDataBridge
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
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_dialog)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(getColor(context, R.color.LoadingBackground)))
        window?.setGravity(Gravity.CENTER)

        Glide.with(context).load(R.drawable.loading).into(load_gif)
        progress.text = String.format("%02d%%", percentage)

        when (request) {
            TRIM -> {
                text.text = context.getString(R.string.trim_video)
                job = trimVideo(dataBinder ?: return, uri ?: return)
            }
            EXPORT -> {
                isUnity = true
                text.text = context.getString(R.string.export_video)
                File(context.filesDir.absolutePath + "/result.mp4").delete()
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
            if (!isUnity) {
                (job ?: return@setOnClickListener).cancel()
                if (request == SAVE) {
                    input?.close()
                    output?.flush()
                    output?.close()
                } else
                    FFmpeg.cancel()
            } else {
                unityDataBridge?.cancelExporting()
            }
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
            frameCount = (dataBinder.rangeExclusiveEndIndex - dataBinder.rangeStartIndex).toInt()

            val start = dataBinder.startMs / 1000.0
            val end = dataBinder.endMs / 1000.0
            Thread {
                FFmpegDelegate.extractFrames(start, end, inPath, outPath) {
                    if (it == Config.RETURN_CODE_SUCCESS)
                        percentage = 50
                }
            }.start()

            getPercentageFromFFmpeg(0)

            inferenceVideo(dataBinder, parentPath)

            FFmpegDelegate.extractAudio(
                dataBinder.startMs / 1000.0,
                dataBinder.endExcludeMs / 1000.0,
                inPath,
                context
            )
            Intent(context, ProjectActivity::class.java).apply {
                putExtra(IMAGE_PATH, context.filesDir.absolutePath + "/video_image")
                putExtra(AUDIO_PATH, context.filesDir.absolutePath + "/audio.mp3")
                putExtra(VIDEO_FRAME_COUNT, frameCount)
                putExtra(VIDEO_DURATION, (dataBinder.endMs - dataBinder.startMs + 1).toInt())
                putExtra(MODEL_OUTPUT, personList as Serializable)
            }.also { startActivity(context, it, null) }
            dismiss()
        }

    private fun inferenceVideo(dataBinder: ITrimmingData, path: String) {
        val frameCount =
            (dataBinder.rangeExclusiveEndIndex - dataBinder.rangeStartIndex + 1).toInt()
        var currentBox = BBox(0, 0, 0, 0)
        personList.clear()

        // Detecting initial bounding box
        // TODO: 9/9/2020 developers should not use like this mehod
        for (i in 1 until 3) {
            val bitmap: Bitmap? = BitmapFactory.decodeFile(path + "/img%08d.png".format(i))
            if (bitmap == null)
                Log.d("bitmap", "has no bit map")
            if (bitmap != null) {
                // TODO: 9/9/2020 human detector
                currentBox = BBox(0, 0, bitmap?.width, bitmap?.height)
                val croppedBitmap: Bitmap = Bitmap.createBitmap(
                    bitmap,
                    currentBox.x,
                    currentBox.y,
                    currentBox.w,
                    currentBox.h
                )
                val resized = Bitmap.createScaledBitmap(croppedBitmap, width, height, true)
                val person = poseEstimation.estimatePose(resized)
                personList.add(person)
            }
            Log.d(
                "currentBox",
                "%d %d %d %d".format(currentBox.x, currentBox.y, currentBox.w, currentBox.h)
            )
        }

        // Tracking bounding box based on heatmap
        for (i in 3 until frameCount) {
            val bitmap: Bitmap? = BitmapFactory.decodeFile(path + "/img%08d.png".format(i))
            if (bitmap == null)
                Log.d("bitmap", "has no bit map")
            if (bitmap != null) {
                currentBox =
                    personList[i - 2]?.let { BBoxTracker.convert(bitmap, it, currentBox) }!!
                Log.d(
                    "currentBox",
                    "%d %d %d %d %d".format(
                        i,
                        currentBox.x,
                        currentBox.y,
                        currentBox.w,
                        currentBox.h
                    )
                )
                val croppedBitmap: Bitmap = Bitmap.createBitmap(
                    bitmap,
                    currentBox.x,
                    currentBox.y,
                    currentBox.w,
                    currentBox.h
                )
                val resized = Bitmap.createScaledBitmap(croppedBitmap, width, height, true)
                val person = poseEstimation.estimatePose(resized)
                personList.add(person)
            }
        }
        JsonConverter.convert(personList, frameCount, context)
    }

    /**
     * Called in Unity.
     */
    fun encodeVideo() {
        job = CoroutineScope(Dispatchers.IO).launch {
            isUnity = false
            Thread {
                FFmpegDelegate.exportVideo(capturePath, audioPath, fps, resultPath) {
                    if (it == Config.RETURN_CODE_SUCCESS)
                        percentage = 100
                }
            }.start()

            getPercentageFromFFmpeg(50)

            File(audioPath).delete()
            File(capturePath).listFiles()?.forEach { it.delete() }
            dismiss()
            Intent(context, ExportActivity::class.java).apply {
                putExtra("RESULT", resultPath)
                startActivity(context, this, null)
            }
        }
    }

    /**
     * Update percentage.
     */
    fun update(@IntRange(from = 0, to = 100) percentage: Int) {
        this.percentage = percentage
        progress.text = String.format("%02d%%", percentage)
    }

    private fun saveVideo(): Job =
        CoroutineScope(Dispatchers.IO).launch {
            //Video name is depended by time
//            val time = System.currentTimeMillis()
//            val date = Date(time)
//            val nameFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
//            val filename = nameFormat.format(date) + ".mp4"
            dismiss()
        }

    private fun readOneLine(reader: BufferedReader) = reader.readLine()

    private fun process() =
        Runtime.getRuntime().exec("logcat -d -v process -t 1 mobile-ffmpeg:I *:S")

    private suspend fun getPercentageFromFFmpeg(startPercentage: Int) {
        val find = "frame"
        val pid = android.os.Process.myPid()
        var process = process()
        var reader = BufferedReader(InputStreamReader(process.inputStream))
        var currentLine: String?

        while (percentage < 50 + startPercentage) {
            currentLine = readOneLine(reader)
            if (currentLine == null) {
                process = process()
                reader = BufferedReader(InputStreamReader(process.inputStream))
                continue
            }
            if (currentLine.contains(java.lang.String.valueOf(pid))) {
                if (currentLine.contains("$find=")) {
                    val arr1 = currentLine.split("$find=")
                    val arr2 = arr1[1].trim().split(" ")

                    val threshold = 50 * arr2[0].toInt() / frameCount + startPercentage
                    if (percentage < threshold)
                        percentage = threshold
                    else
                        continue
                }
            }
            progress.text = String.format("%02d%%", percentage)
            yield()
        }
    }
}
