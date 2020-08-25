package com.teamisland.zzazz.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.Window
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.startActivity
import com.arthenica.mobileffmpeg.FFmpeg
import com.bumptech.glide.Glide
import com.teamisland.zzazz.R
import com.teamisland.zzazz.ui.ExportActivity
import com.teamisland.zzazz.ui.ProjectActivity
import com.unity3d.player.UnityPlayer
import kotlinx.android.synthetic.main.loading_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.properties.Delegates

/**
 * Dialog for loading
 */
class LoadingDialog(context: Context, private val request: Int) :
    Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {

    private lateinit var path: String
    private var fps by Delegates.notNull<Float>()
    private lateinit var resultPath: String
    private var audioPath: String = ""
    private var originPath: String = ""
    private var capturePath: String = ""

    private var percentage: Int = 0

    /**
     * Constructor for [EXPORT]
     */
    constructor(
        context: Context,
        request: Int,
        path: String,
        fps: Float,
        resultPath: String
    ) : this(context, request) {
        this.path = path
        this.fps = fps
        this.resultPath = resultPath
    }

    companion object {
        /**
         * Add effect.
         */
        const val APPLY_EFFECT: Int = 0

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

        when (request) {
            APPLY_EFFECT -> {
                text.text =
                    context.getString(R.string.apply_effect) + " (" + String.format(
                        "%02d",
                        percentage
                    ) + "%)"
                applyEffect()
            }
            EXPORT -> {
                text.text =
                    context.getString(R.string.export_video) + " (" + String.format(
                        "%02d",
                        percentage
                    ) + "%)"
                exportVideo()
            }
            SAVE -> {
                text.text =
                    context.getString(R.string.save_video) + " (" + String.format(
                        "%02d",
                        percentage
                    ) + "%)"
                saveVideo()
            }
            else -> return
        }
    }

    private fun applyEffect() {}

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun exportVideo() {
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
                "$originPath:$capturePath"
            )
        }
    }


    /**
     * Called in Unity.
     */
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun encodeVideo() {
        CoroutineScope(Dispatchers.Default).launch {
            // images to video
            Log.d("Export", "Convert the images to a video.")
//        val videoPath = filesDir.absolutePath + "/video.mp4"
//        FFmpeg.execute("-i $capturePath/img%08d.png -r $fps -pix_fmt yuv420p $videoPath")
            FFmpeg.execute("-i $capturePath/img%08d.png -r $fps -pix_fmt yuv420p $resultPath")

            // combine audio and video
            Log.d("Export", "Combine the audio and the video.")
//        FFmpeg.execute("-i $videoPath -i $audioPath -vcodec copy -c:a aac $resultPath")

            File(audioPath).delete()
            for (img in File(originPath).listFiles())
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

    private fun saveVideo() {}
}
