package com.teamisland.zzazz.inference

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.teamisland.zzazz.utils.LoadingDialog
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate

enum class Device {
    CPU,
    NNAPI,
    GPU,
}

enum class BodyPart {
    NOSE,
    LEFT_EYE,
    RIGHT_EYE,
    LEFT_EAR,
    RIGHT_EAR,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE
}

class Position {
    var x: Int = 0
    var y: Int = 0
    var z: Int = 0
}

class KeyPoint {
    var bodyPart: BodyPart =
        BodyPart.NOSE
    var position: Position = Position()
    var score: Float = 0.0f
}

class Person {
    var keyPoints = listOf<KeyPoint>()
    var score: Float = 0.0f
}

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
    val filename: String = "movnect.tflite",
    val device: Device = Device.CPU
) : AutoCloseable {
    var lastInferenceTimeNanoSeconds: Long = -1
        private set

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private val NUM_LITE_THREADS = 4

    private fun loadModelFile(path: String, context: Context): MappedByteBuffer {
        Log.d("%s".format(path), "zzazz")
        val fileDescriptor = context.assets.openFd(path)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        return inputStream.channel.map(
            FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength
        )
    }
    private fun getInterpreter(): Interpreter {
        if (interpreter != null) {
            return interpreter!!
        }
        val options = Interpreter.Options()
        options.setNumThreads(NUM_LITE_THREADS)
        when (device) {
            Device.CPU -> {}
            Device.GPU -> {
                gpuDelegate = GpuDelegate()
                options.addDelegate(gpuDelegate)
            }
            Device.NNAPI -> {
                options.setUseNNAPI(true)
            }
        }
        interpreter = Interpreter(loadModelFile(filename, context), options)
        return interpreter!!
    }

    /**
     * Scale the image to a byteBuffer of [-1, 1] values.
     */
    private fun initInputArray(bitmap: Bitmap): ByteBuffer {
        val bytesPerChannel = 4 // float32 = 4 bytes
        val inputChannels = 3 // RGB channels
        val batchSize = 1 // single batch inference
        val inputBuffer = ByteBuffer.allocateDirect(
            batchSize * bytesPerChannel * bitmap.height * bitmap.width * inputChannels
        )
        inputBuffer.order(ByteOrder.nativeOrder())
        inputBuffer.rewind()

        val mean = 128.0f
        val std = 128.0f
        val intValues = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (pixelValue in intValues) {
            inputBuffer.putFloat(((pixelValue shr 16 and 0xFF) - mean) / std) // R
            inputBuffer.putFloat(((pixelValue shr 8 and 0xFF) - mean) / std)  // G
            inputBuffer.putFloat(((pixelValue and 0xFF) - mean) / std)  // B
        }
        return inputBuffer
    }

    private fun initOutputMap(interpreter: Interpreter): HashMap<Int, Any> {
        val outputMap = HashMap<Int, Any>()

        val heatmapShape = arrayOf(1, 64, 64, 15)
        Log.i(
            "zzazz_core",
            String.format(
                "******** shape %d %d %d %d", heatmapShape[0], heatmapShape[1], heatmapShape[2], heatmapShape[3]
            )
        )
        outputMap[0] = Array(heatmapShape[0]) { // 1
            Array(heatmapShape[1]) { // 14
                Array(heatmapShape[2]) { // 14
                    FloatArray(heatmapShape[3]) // 96
                }
            }
        }

        val locationXShape = arrayOf(1, 64, 64, 15)
        outputMap[1] = Array(locationXShape[0]) { // 1
            Array(locationXShape[1]) { // 14
                Array(locationXShape[2]) { // 14
                    FloatArray(locationXShape[3]) // 96
                }
            }
        }

        val locationYShape = arrayOf(1, 64, 64, 15)
        outputMap[2] = Array(locationYShape[0]) { // 1
            Array(locationYShape[1]) { // 14
                Array(locationYShape[2]) { // 14
                    FloatArray(locationYShape[3]) // 96
                }
            }
        }

        val locationZShape = arrayOf(1, 64, 64, 15)
        outputMap[3] = Array(locationZShape[0]) { // 1
            Array(locationZShape[1]) { // 14
                Array(locationZShape[2]) { // 14
                    FloatArray(locationZShape[3]) // 96
                }
            }
        }

        return outputMap
    }

    @Suppress("UNCHECKED_CAST")
    fun estimatePose(bitmap: Bitmap) {
        val estimationStartTimeNanoSeconds = SystemClock.elapsedRealtimeNanos()
        val inputArray = arrayOf(initInputArray(bitmap))
        Log.i(
            "zzazz_core",
            String.format(
                "Scaling to [-1, 1] took %.2f ms",
                1.0f * (SystemClock.elapsedRealtimeNanos() - estimationStartTimeNanoSeconds) / 1_000_000
            )
        )

        val outputMap = initOutputMap(getInterpreter())

        val inferenceStartTimeNanoSeconds = SystemClock.elapsedRealtimeNanos()
        getInterpreter().runForMultipleInputsOutputs(inputArray, outputMap)
        lastInferenceTimeNanoSeconds = SystemClock.elapsedRealtimeNanos() - inferenceStartTimeNanoSeconds
        Log.i(
            "zzazz_core",
            String.format("Interpreter took %.2f ms", 1.0f * lastInferenceTimeNanoSeconds / 1_000_000)
        )

        val heatmap = outputMap[0] as Array<Array<Array<FloatArray>>>
        val locationX = outputMap[1] as Array<Array<Array<FloatArray>>>
        val locationY = outputMap[2] as Array<Array<Array<FloatArray>>>
        val locationZ = outputMap[3] as Array<Array<Array<FloatArray>>>

        val height = heatmap[0].size
        val width = heatmap[0][0].size
        val numKeypoints = heatmap[0][0][0].size

        Log.i(
            "zzazz_core",
            String.format("Size: %d %d %d", height, width, numKeypoints)
        )
    }

    override fun close() {
        interpreter?.close()
        interpreter = null
        gpuDelegate?.close()
        gpuDelegate = null
    }
}