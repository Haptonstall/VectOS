package com.lz.vectos.ui.beam

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lz.vectos.domain.structural.PointCapacityResult
import com.lz.vectos.domain.structural.Flange
import java.util.UUID

/**
 * Renders a color-coded utilization heat map for a beam.
 * Ingests the final array of capacity checks and maps UR to colors.
 */
@Composable
fun UtilizationHeatMap(
    results: List<PointCapacityResult>,
    topBraces: List<Double>, // x-coordinates of top braces
    bottomBraces: List<Double>, // x-coordinates of bottom braces
    totalLength: Double,
    modifier: Modifier = Modifier
) {
    var selectedPoint by remember { mutableStateOf<PointCapacityResult?>(null) }
    var touchX by remember { mutableStateOf(-1f) }

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Utilization Heat Map (Capacity Check)",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .pointerInput(results) {
                    detectTapGestures { offset ->
                        touchX = offset.x
                        selectedPoint = findClosestPoint(offset.x, size.width, results, totalLength)
                    }
                }
                .pointerInput(results) {
                    detectDragGestures { change, _ ->
                        touchX = change.position.x
                        selectedPoint = findClosestPoint(change.position.x, size.width, results, totalLength)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val beamHeight = 40.dp.toPx()
                val beamTop = (canvasHeight - beamHeight) / 2

                // 1. Draw the Heat Map Segments
                if (results.isNotEmpty()) {
                    val stepWidth = canvasWidth / (results.size - 1).coerceAtLeast(1)
                    
                    for (i in 0 until results.size - 1) {
                        val p1 = results[i]
                        val p2 = results[i + 1]
                        
                        val startX = (p1.x / totalLength).toFloat() * canvasWidth
                        val endX = (p2.x / totalLength).toFloat() * canvasWidth
                        
                        drawRect(
                            color = getUtilizationColor(p1.utilizationRatio),
                            topLeft = Offset(startX, beamTop),
                            size = Size(endX - startX + 1f, beamHeight)
                        )
                    }
                }

                // 2. Draw Brace Indicators
                drawBraces(topBraces, totalLength, canvasWidth, beamTop, isTop = true)
                drawBraces(bottomBraces, totalLength, canvasWidth, beamTop + beamHeight, isTop = false)

                // 3. Draw Selection Indicator
                if (selectedPoint != null && touchX >= 0) {
                    val selX = (selectedPoint!!.x / totalLength).toFloat() * canvasWidth
                    drawLine(
                        color = Color.Black,
                        start = Offset(selX, 0f),
                        end = Offset(selX, canvasHeight),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // 4. Interactive Overlay Tooltip
            selectedPoint?.let { point ->
                Surface(
                    modifier = Modifier
                        .align(if (touchX > size.width / 2) Alignment.TopStart else Alignment.TopEnd)
                        .padding(8.dp)
                        .width(200.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("x = ${"%.2f".format(point.x)} m", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Ratio: ${"%.2f".format(point.utilizationRatio)}", 
                            color = if (point.utilizationRatio > 1.0) Color.Red else Color.Unspecified,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text("Limit State: ${point.governingLimitState}", fontSize = 11.sp)
                        Text("Comp. Flange: ${point.compressionFlange}", fontSize = 11.sp)
                        Text("Lb: ${"%.2f".format(point.Lb)} m", fontSize = 11.sp)
                    }
                }
            }
        }
        
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem("Safe (<0.6)", Color(0xFF4CAF50))
            LegendItem("Caution (0.8)", Color(0xFFFFC107))
            LegendItem("Warning (1.0)", Color(0xFFFF9800))
            LegendItem("Failing (>1.0)", Color(0xFFF44336))
        }
    }
}

private fun DrawScope.drawBraces(braces: List<Double>, totalLength: Double, canvasWidth: Float, y: Float, isTop: Boolean) {
    val braceSize = 10.dp.toPx()
    braces.forEach { braceX ->
        val x = (braceX / totalLength).toFloat() * canvasWidth
        val path = Path().apply {
            if (isTop) {
                moveTo(x, y)
                lineTo(x - braceSize / 2, y - braceSize)
                lineTo(x + braceSize / 2, y - braceSize)
            } else {
                moveTo(x, y)
                lineTo(x - braceSize / 2, y + braceSize)
                lineTo(x + braceSize / 2, y + braceSize)
            }
            close()
        }
        drawPath(path, color = Color.DarkGray)
    }
}

private fun getUtilizationColor(ratio: Double): Color {
    return when {
        ratio < 0.6 -> Color(0xFF4CAF50) // Green
        ratio < 0.8 -> Color(0xFFFFC107) // Yellow
        ratio < 1.0 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}

private fun findClosestPoint(touchX: Float, canvasWidth: Float, results: List<PointCapacityResult>, totalLength: Double): PointCapacityResult? {
    if (results.isEmpty()) return null
    val targetX = (touchX / canvasWidth) * totalLength
    return results.minByOrNull { Math.abs(it.x - targetX) }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Text(text = label, modifier = Modifier.padding(start = 4.dp), fontSize = 10.sp)
    }
}
