package com.example.new_app9

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
import org.tensorflow.lite.Interpreter

class ExampleUnitTest {
    @Test
    fun inspectModel() {
        val modelFile = File("src/main/assets/yolo26n.tflite")
        assertTrue("Model file exists", modelFile.exists())
        val interpreter = Interpreter(modelFile)
        val inputTensorCount = interpreter.inputTensorCount
        val outputTensorCount = interpreter.outputTensorCount
        kotlin.io.println("=== TFLITE MODEL INFO ===")
        kotlin.io.println("Input tensors: $inputTensorCount")
        for (i in 0 until inputTensorCount) {
            val tensor = interpreter.getInputTensor(i)
            kotlin.io.println("Input $i: shape=${tensor.shape().contentToString()}, type=${tensor.dataType()}")
        }
        kotlin.io.println("Output tensors: $outputTensorCount")
        for (i in 0 until outputTensorCount) {
            val tensor = interpreter.getOutputTensor(i)
            kotlin.io.println("Output $i: shape=${tensor.shape().contentToString()}, type=${tensor.dataType()}")
        }
        interpreter.close()
    }
}
