package com.teamisland.zzazz.utils.inference

import android.content.Context
import android.graphics.Bitmap
import android.os.Parcelable
import android.os.SystemClock
import android.util.Log
import kotlinx.android.parcel.Parcelize
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.DequantizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


enum class Device {
    CPU,
    NNAPI,
    GPU,
}

/**
 * this BodyPart JointNumber should be equal to the number of inference result
 */
enum class BodyPart {
    Pelvis,
    R_Hip,
    R_Knee,
    R_Ankle,
    L_Hip,

    L_Knee,
    L_Ankle,
    Torso,
    Neck,
    Nose,

    Head,
    L_Shoulder,
    L_Elbow,
    L_Wrist,
    R_Shoulder,

    R_Elbow,
    R_Wrist,
    Thorax
}

@Parcelize
data class Position(
    var x: Float = 0F,
    var y: Float = 0F,
    var z: Float = 0F
) : Parcelable

@Parcelize
data class KeyPoint(
    var bodyPart: BodyPart = BodyPart.Pelvis,
    var position: Position = Position()
) : Parcelable

@Parcelize
data class BBox(
    var x: Int = 0,
    var y: Int = 0,
    var w: Int = 0,
    var h: Int = 0
) : Parcelable

@Parcelize
data class Person(
    var keyPoints: List<KeyPoint> = listOf(),
    var bBox: BBox = BBox()
) : Parcelable

/**
 * ZZAZZ Core model class
 *
 * This class is the deep learning model for ZZAZZ. It requires
 * tensorflow lite model.
 *
 * @property context the android context.
 * @property filename the file path of the model file
 * @property device CPU, GPU, or NNAPI.
 * @constructor Creates
 */
class PoseEstimation(
    val context: Context,
    private val filename: String = "optimize_default.tflite",
    private val device: Device = Device.CPU
) : AutoCloseable {
    private var lastInferenceTimeNanoSeconds: Long = -1

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private val NUM_LITE_THREADS = 4

    private fun getInterpreter(): Interpreter {

        if (interpreter != null) {
            return interpreter as Interpreter
        }
        val options = Interpreter.Options()
        options.setNumThreads(NUM_LITE_THREADS)
        when (device) {
            Device.CPU -> { }
            Device.GPU -> {
                gpuDelegate = GpuDelegate()
                options.addDelegate(gpuDelegate) }
            Device.NNAPI -> { options.setUseNNAPI(true) }
        }
        val tfliteModel = FileUtil.loadMappedFile(context, filename)
        interpreter = Interpreter(tfliteModel, options)
        return interpreter as Interpreter
    }

    /**
     * Scale the image to a byteBuffer of [-1, 1] values.
     */
    private fun initInputArray(bitmap: Bitmap): ByteBuffer {

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(256, 256, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        val tImage = TensorImage(DataType.FLOAT32)
        tImage.load(bitmap)
        Log.i("tImage_before", String.format("shape %d, %d", tImage.height, tImage.bitmap.height))

        imageProcessor.process(tImage)
        Log.i("tImage_after", String.format("shape %d, %d", tImage.height, tImage.bitmap.height))

        return tImage.buffer
    }

    private fun initOutputMap(interpreter: Interpreter): TensorBuffer {
        val arrayShape = interpreter.getOutputTensor(0).shape()
        val locationBuffer = TensorBuffer.createFixedSize(arrayShape, DataType.FLOAT32)
        Log.i(
            "initial array shape", "%s, %s, %s, shape".format(
                arrayShape[0].toString(),
                arrayShape[1].toString(),
                arrayShape[2].toString()
            )
        )
        return locationBuffer
    }

    @Suppress("UNCHECKED_CAST")
    fun estimatePose(bitmap: Bitmap): Person {
        val estimationStartTimeNanoSeconds = SystemClock.elapsedRealtimeNanos()
        val inputArray = initInputArray(bitmap)

        Log.i("inputarray shape", "%s shape".format(inputArray.toString()))
        Log.i(
            "scaling time", String.format(
                "Scaling to [-1, 1] took %.2f ms",
                1.0f * (SystemClock.elapsedRealtimeNanos() - estimationStartTimeNanoSeconds) / 1_000_000
            )
        )

        val outputBuffer = initOutputMap(getInterpreter())
        val person = Person()

        val inferenceStartTimeNanoSeconds = SystemClock.elapsedRealtimeNanos()
        getInterpreter().run(inputArray, outputBuffer.buffer)
        lastInferenceTimeNanoSeconds = SystemClock.elapsedRealtimeNanos() - inferenceStartTimeNanoSeconds

        Log.i(
            "estimation_time", String.format("Interpreter took %.2f ms", 1.0f * lastInferenceTimeNanoSeconds / 1_000_000)
        )

        val coordinate = outputBuffer.floatArray
        val numKeypoints = coordinate.size / 3  // divide by 3 for x, y, z index

        // Find also max and min point of total keypoints
        var minRow = 0
        var maxRow = 1080
        var minCol = 0
        var maxCol = 1920

        person.bBox.x = minCol
        person.bBox.y = minRow
        person.bBox.w = maxCol
        person.bBox.h = maxRow

        val keypointList = Array(numKeypoints) { KeyPoint() }
        enumValues<BodyPart>().forEachIndexed { idx, it ->
            keypointList[idx].bodyPart = it
            keypointList[idx].position.x = (coordinate[3 * idx])
            keypointList[idx].position.y = (coordinate[3 * idx + 1])
            keypointList[idx].position.z = (coordinate[3 * idx + 2])
        }
        person.keyPoints = keypointList.toList()
        return person
    }

    override fun close() {
        interpreter?.close()
        interpreter = null
        gpuDelegate?.close()
        gpuDelegate = null
    }
}

private typealias TripleFloatArray = Array<Array<FloatArray>>
