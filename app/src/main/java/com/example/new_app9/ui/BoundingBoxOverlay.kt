package com.example.new_app9.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.new_app9.detector.DetectionResult

private val BOX_COLORS = listOf(
    Color(0xFF00E676), // Bright Green
    Color(0xFFFF9100), // Orange
    Color(0xFF00E5FF), // Cyan
    Color(0xFFFF4081), // Pink
    Color(0xFFFFEA00), // Yellow
    Color(0xFF7C4DFF), // Deep Purple
    Color(0xFFFF3D00), // Deep Orange
    Color(0xFF00B0FF), // Light Blue
    Color(0xFF1DE9B6), // Teal
    Color(0xFFAEEA00)  // Lime
)

@Composable
fun BoundingBoxOverlay(
    detections: List<DetectionResult>,
    modifier: Modifier = Modifier,
    isMirrored: Boolean = false
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        val viewWidth = size.width
        val viewHeight = size.height

        for (detection in detections) {
            val color = BOX_COLORS[detection.classId % BOX_COLORS.size]

            var left = detection.boundingBox.left * viewWidth
            var right = detection.boundingBox.right * viewWidth
            val top = detection.boundingBox.top * viewHeight
            val bottom = detection.boundingBox.bottom * viewHeight

            if (isMirrored) {
                val tempLeft = left
                left = viewWidth - right
                right = viewWidth - tempLeft
            }

            val boxWidth = right - left
            val boxHeight = bottom - top

            // Draw bounding box
            drawRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(boxWidth, boxHeight),
                style = Stroke(width = 6f)
            )

            // Label text & score
            val text = "${detection.label} ${(detection.score * 100).toInt()}%"
            val textLayoutResult = textMeasurer.measure(
                text = text,
                style = TextStyle(color = Color.White, fontSize = 13.sp)
            )

            val textWidth = textLayoutResult.size.width.toFloat()
            val textHeight = textLayoutResult.size.height.toFloat()

            val textBackgroundTop = if (top - textHeight - 8f < 0) top else top - textHeight - 8f

            // Draw text background rect
            drawRect(
                color = color.copy(alpha = 0.85f),
                topLeft = Offset(left, textBackgroundTop),
                size = Size(textWidth + 16f, textHeight + 8f)
            )

            // Draw text
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(left + 8f, textBackgroundTop + 4f)
            )
        }
    }
}
