package com.example.new_app9.detector

import androidx.compose.ui.geometry.Rect

data class DetectionResult(
    val boundingBox: Rect, // Normalized rect [0.0..1.0] relative to input image
    val label: String,
    val score: Float,
    val classId: Int
)

data class InferenceInfo(
    val detections: List<DetectionResult>,
    val inferenceTimeMs: Long,
    val imageWidth: Int,
    val imageHeight: Int
)
