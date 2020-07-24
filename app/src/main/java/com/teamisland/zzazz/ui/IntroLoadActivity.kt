package com.teamisland.zzazz.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.teamisland.zzazz.R
import com.teamisland.zzazz.utils.GetVideoData
import com.teamisland.zzazz.utils.IntroAlertDialog
import kotlinx.android.synthetic.main.activity_intro_load.view.*

/**
 * Main activity of Intro Activity
 */
class IntroLoadActivity(private val packageManager: PackageManager) : Fragment() {

    companion object {
        /**
         * Uri of the video retrieved.
         */
        const val VIDEO_URI: String = "pre_trim_video_uri"

        private const val REQUEST_VIDEO_SELECT = 1
    }

    private fun dispatchGetVideoIntent() {
        Intent(
            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).also { getVideoIntent ->
            getVideoIntent.type = "video/*"
            getVideoIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(
                    Intent.createChooser(getVideoIntent, "Select Video"),
                    REQUEST_VIDEO_SELECT
                )
            }
        }
    }

    private fun warnAndRun(run_function: () -> (Unit)) {
        val builder = IntroAlertDialog(context, run_function)
        builder.create()
        builder.show()
    }

    /**
     * [Fragment.onCreateView]
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.activity_intro_load, container, false)

        view.take_video_from_gallery_button.setOnClickListener { warnAndRun { dispatchGetVideoIntent() } }

        return view
    }

    /**
     * Retrieve uri from request. Checks whether the uri is valid under the restrictions.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        val videoUri = (data ?: return).data ?: return

        val videoFps = context?.let { GetVideoData.getFPS(it, videoUri) }
        if (videoFps!! > resources.getInteger(R.integer.fps_limit)) {
            Toast.makeText(context, getString(R.string.fps_exceeded), Toast.LENGTH_LONG).show()
            return
        }

        val videoDuration = context?.let { GetVideoData.getDuration(it, videoUri) }
        if (videoDuration!! > resources.getInteger(R.integer.length_limit) * 1000) {
            Toast.makeText(context, getString(R.string.length_exceeded), Toast.LENGTH_LONG).show()
            //return
        }
        println(videoUri)

        Intent(context, TrimmingActivity::class.java).also {
            it.putExtra(VIDEO_URI, videoUri)
            startActivity(it)
        }
    }
}