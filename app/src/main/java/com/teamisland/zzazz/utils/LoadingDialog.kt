package com.teamisland.zzazz.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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
    private var path: String = ""
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
        path: String,
        imagePath: String,
        fps: Float,
        resultPath: String
    ) : this(context, request) {
        this.path = path
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
                job = saveVideo(path)
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

    private fun trimVideo(dataBinder: ITrimmingData, uri: Uri): Job =
        CoroutineScope(Dispatchers.IO).launch {
            val inPath = AbsolutePathRetriever.getPath(context, uri) ?: return@launch
            val outPath = run {
                // Set destination location.
                val parentFolder = context.filesDir
                val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"
                File(parentFolder, fileName)
            }.absolutePath
            FFmpegDelegate.trimVideo(inPath, dataBinder.startMs, dataBinder.endMs, outPath) { i ->
                if (i == Config.RETURN_CODE_SUCCESS) {
                    // export images from origin video
                    Log.d("Export", "Start exporting the images from an origin video.")
                    val originPath = context.filesDir.absolutePath + "/video_image"
                    val originFile = File(originPath)
                    if (!originFile.exists())
                        originFile.mkdir()
                    FFmpeg.execute("-r 1 -i $outPath $originPath/img%08d.png")
                    Log.d("Export", "Finish exporting the images from an origin video.")

                    Intent(context, ProjectActivity::class.java).apply {
                        putExtra(TrimmingActivity.VIDEO_PATH, outPath)
                        putExtra(TrimmingActivity.IMAGE_PATH, "$originPath/img%08d.png")
                        putExtra(
                            TrimmingActivity.VIDEO_FRAME_COUNT,
                            dataBinder.rangeExclusiveEndIndex - dataBinder.rangeStartIndex
                        )
                    }.also { startActivity(context, it, null) }

                    dismiss()
                }
            }
        }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun exportVideo(): Job =
        CoroutineScope(Dispatchers.Default).launch {
            File(context.filesDir.absolutePath + "/result.mp4").delete()
            // extract audio from video
            Log.d("Export", "Start extracting audio.")
            audioPath = context.filesDir.absolutePath + "/audio.mp3"
//        FFmpeg.execute("-i $path $audioPath")
            Log.d("Export", "Finish extracting audio.")

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
    private fun saveVideo(path: String): Job =
        CoroutineScope(Dispatchers.IO).launch {
            //Video name is depended by time
            val time = System.currentTimeMillis()
            val date = Date(time)
            val nameFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
            val filename = nameFormat.format(date) + ".mp4"

            val contentValues = ContentValues().apply {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.Files.FileColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_MOVIES + "/ZZAZZ"
                    )
                    put(MediaStore.Files.FileColumns.IS_PENDING, 1)
                }
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, filename)
                put(MediaStore.Files.FileColumns.MIME_TYPE, "video/*")
            }

//        val input = context.contentResolver.openInputStream(Uri.fromFile(File(uri.path)))
            input = context.contentResolver.openInputStream(Uri.parse(path))

            val outputUri =
                context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

            val parcelFileDescriptor =
                context.contentResolver.openFileDescriptor(outputUri ?: return@launch, "w", null)

            output = FileOutputStream((parcelFileDescriptor ?: return@launch).fileDescriptor)

            val data = ByteArray(1024)
            var total = 0F
            var count: Int
            val len: Float = (input ?: return@launch).available().toFloat()

            count = try {
                (input ?: return@launch).read(data)
            } catch (e: Exception) {
                -1
            }
            while (count != -1) {
                percentage = (total / len * 100).toInt()
                text.text = String.format("%02d", percentage) + "%"

                try {
                    (output ?: return@launch).write(data, 0, count)
                    count = (input ?: return@launch).read(data)
                    total += count
                } catch (e: Exception) {
                    break
                }
            }

            (input ?: return@launch).close()
            (output ?: return@launch).flush()
            (output ?: return@launch).close()
            contentValues.clear()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 0)
            }
            context.contentResolver.update(outputUri, contentValues, null, null)

            dismiss()
        }
}
