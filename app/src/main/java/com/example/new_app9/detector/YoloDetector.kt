package com.example.new_app9.detector

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.ui.geometry.Rect
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min

class YoloDetector(
    context: Context,
    modelPath: String = "yolo26n.tflite",
    private val confThreshold: Float = 0.35f,
    private val iouThreshold: Float = 0.45f
) {
    private val interpreter: Interpreter
    private val inputWidth: Int
    private val inputHeight: Int
    private val inputChannels: Int
    private val isModelQuantized: Boolean
    private val outputShape: IntArray

    init {
        val assetFileDescriptor = context.assets.openFd(modelPath)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }
        interpreter = Interpreter(mappedByteBuffer, options)

        val inputTensor = interpreter.getInputTensor(0)
        val shape = inputTensor.shape() // [1, height, width, channels] or [1, channels, height, width]
        if (shape.size == 4) {
            if (shape[1] == 3) { // [1, 3, H, W]
                inputChannels = shape[1]
                inputHeight = shape[2]
                inputWidth = shape[3]
            } else { // [1, H, W, 3]
                inputHeight = shape[1]
                inputWidth = shape[2]
                inputChannels = shape[3]
            }
        } else {
            inputWidth = 640
            inputHeight = 640
            inputChannels = 3
        }

        isModelQuantized = inputTensor.dataType() == org.tensorflow.lite.DataType.UINT8 ||
                inputTensor.dataType() == org.tensorflow.lite.DataType.INT8

        val outputTensor = interpreter.getOutputTensor(0)
        outputShape = outputTensor.shape()
    }

    fun detect(bitmap: Bitmap): InferenceInfo {
        val startTime = SystemClock.uptimeMillis()
        val origWidth = bitmap.width
        val origHeight = bitmap.height

        val resizedBitmap = if (bitmap.width != inputWidth || bitmap.height != inputHeight) {
            Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
        } else {
            bitmap
        }

        val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)

        // Dynamically allocate output buffer based on model output shape
        val outputs = Array(1) { createOutputArray(outputShape) }
        val outputMap = HashMap<Int, Any>()
        outputMap[0] = outputs[0]

        interpreter.runForMultipleInputsOutputs(arrayOf(byteBuffer), outputMap)

        val detections = parseOutputs(outputs[0], outputShape, origWidth, origHeight)
        val nmsDetections = nms(detections, iouThreshold)

        val endTime = SystemClock.uptimeMillis()
        val inferenceTime = endTime - startTime

        return InferenceInfo(
            detections = nmsDetections,
            inferenceTimeMs = inferenceTime,
            imageWidth = origWidth,
            imageHeight = origHeight
        )
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(
            (if (isModelQuantized) 1 else 4) * inputWidth * inputHeight * inputChannels
        )
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0 until inputHeight) {
            for (j in 0 until inputWidth) {
                val valPixel = intValues[pixel++]
                val r = (valPixel shr 16 and 0xFF)
                val g = (valPixel shr 8 and 0xFF)
                val b = (valPixel and 0xFF)

                if (isModelQuantized) {
                    byteBuffer.put(r.toByte())
                    byteBuffer.put(g.toByte())
                    byteBuffer.put(b.toByte())
                } else {
                    byteBuffer.putFloat(r / 255.0f)
                    byteBuffer.putFloat(g / 255.0f)
                    byteBuffer.putFloat(b / 255.0f)
                }
            }
        }
        return byteBuffer
    }

    private fun createOutputArray(shape: IntArray): Any {
        return when (shape.size) {
            2 -> Array(shape[0]) { FloatArray(shape[1]) }
            3 -> Array(shape[0]) { Array(shape[1]) { FloatArray(shape[2]) } }
            else -> Array(1) { Array(84) { FloatArray(8400) } }
        }
    }

    private fun parseOutputs(outputData: Any, shape: IntArray, origWidth: Int, origHeight: Int): List<DetectionResult> {
        val results = mutableListOf<DetectionResult>()

        if (shape.size == 3) {
            val dim1 = shape[1]
            val dim2 = shape[2]

            // Case 1: [1, 84, 8400] or [1, 85, 8400] -> channels x predictions
            if (dim1 < dim2) {
                val numChannels = dim1
                val numAnchors = dim2
                val array = outputData as Array<Array<FloatArray>>
                val batch = array[0] // shape [numChannels][numAnchors]

                for (anchorIdx in 0 until numAnchors) {
                    var maxScore = 0f
                    var maxClassId = -1

                    val numClasses = numChannels - 4
                    for (c in 0 until numClasses) {
                        val score = batch[4 + c][anchorIdx]
                        if (score > maxScore) {
                            maxScore = score
                            maxClassId = c
                        }
                    }

                    if (maxScore >= confThreshold) {
                        val cx = batch[0][anchorIdx] / inputWidth
                        val cy = batch[1][anchorIdx] / inputHeight
                        val w = batch[2][anchorIdx] / inputWidth
                        val h = batch[3][anchorIdx] / inputHeight

                        val left = max(0f, cx - w / 2f)
                        val top = max(0f, cy - h / 2f)
                        val right = min(1f, cx + w / 2f)
                        val bottom = min(1f, cy + h / 2f)

                        val labelName = if (maxClassId in CocoLabels.LABELS.indices) CocoLabels.LABELS[maxClassId] else "Class $maxClassId"
                        results.add(
                            DetectionResult(
                                boundingBox = Rect(left, top, right, bottom),
                                label = labelName,
                                score = maxScore,
                                classId = maxClassId
                            )
                        )
                    }
                }
            }
            // Case 2: [1, 8400, 84] or [1, N, 6] (End-to-End NMS output)
            else {
                val numAnchors = dim1
                val numChannels = dim2
                val array = outputData as Array<Array<FloatArray>>
                val batch = array[0] // shape [numAnchors][numChannels]

                for (anchorIdx in 0 until numAnchors) {
                    val row = batch[anchorIdx]
                    if (numChannels == 6) {
                        // End-to-end NMS output: [x1, y1, x2, y2, score, class_id]
                        val score = row[4]
                        if (score >= confThreshold) {
                            val classId = row[5].toInt()
                            val left = row[0] / inputWidth
                            val top = row[1] / inputHeight
                            val right = row[2] / inputWidth
                            val bottom = row[3] / inputHeight

                            val labelName = if (classId in CocoLabels.LABELS.indices) CocoLabels.LABELS[classId] else "Class $classId"
                            results.add(
                                DetectionResult(
                                    boundingBox = Rect(left, top, right, bottom),
                                    label = labelName,
                                    score = score,
                                    classId = classId
                                )
                            )
                        }
                    } else {
                        // Standard [1, 8400, 84]
                        var maxScore = 0f
                        var maxClassId = -1
                        val numClasses = numChannels - 4
                        for (c in 0 until numClasses) {
                            val score = row[4 + c]
                            if (score > maxScore) {
                                maxScore = score
                                maxClassId = c
                            }
                        }

                        if (maxScore >= confThreshold) {
                            val cx = row[0] / inputWidth
                            val cy = row[1] / inputHeight
                            val w = row[2] / inputWidth
                            val h = row[3] / inputHeight

                            val left = max(0f, cx - w / 2f)
                            val top = max(0f, cy - h / 2f)
                            val right = min(1f, cx + w / 2f)
                            val bottom = min(1f, cy + h / 2f)

                            val labelName = if (maxClassId in CocoLabels.LABELS.indices) CocoLabels.LABELS[maxClassId] else "Class $maxClassId"
                            results.add(
                                DetectionResult(
                                    boundingBox = Rect(left, top, right, bottom),
                                    label = labelName,
                                    score = maxScore,
                                    classId = maxClassId
                                )
                            )
                        }
                    }
                }
            }
        }
        return results
    }

    private fun nms(detections: List<DetectionResult>, iouThresh: Float): List<DetectionResult> {
        val sorted = detections.sortedByDescending { it.score }.toMutableList()
        val selected = mutableListOf<DetectionResult>()

        while (sorted.isNotEmpty()) {
            val first = sorted.removeAt(0)
            selected.add(first)

            val iterator = sorted.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (calculateIoU(first.boundingBox, next.boundingBox) >= iouThresh) {
                    iterator.remove()
                }
            }
        }
        return selected
    }

    private fun calculateIoU(box1: Rect, box2: Rect): Float {
        val x1 = max(box1.left, box2.left)
        val y1 = max(box1.top, box2.top)
        val x2 = min(box1.right, box2.right)
        val y2 = min(box1.bottom, box2.bottom)

        val intersectionArea = max(0f, x2 - x1) * max(0f, y2 - y1)
        val area1 = (box1.right - box1.left) * (box1.bottom - box1.top)
        val area2 = (box2.right - box2.left) * (box2.bottom - box2.top)

        val unionArea = area1 + area2 - intersectionArea
        return if (unionArea <= 0f) 0f else intersectionArea / unionArea
    }

    fun close() {
        interpreter.close()
    }
}
