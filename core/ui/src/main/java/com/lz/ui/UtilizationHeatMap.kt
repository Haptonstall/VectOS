package com.lz.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lz.model.structural.PointCapacityResult
import com.lz.model.structural.SpanGeometry
import com.lz.model.units.inInches
import java.util.UUID
import kotlin.math.abs

/**
 * Renders a color-coded utilization heat map for a member.
 * Ingests the final array of capacity checks and maps UR to colors.
 */
@Composable
fun UtilizationHeatMap(
    results: List<PointCapacityResult>,
    spans: List<SpanGeometry>,
    totalLength: Double,
    modifier: Modifier = Modifier
) {
    var selectedPoint by remember { mutableStateOf<PointCapacityResult?>(null) }
    var touchX by remember { mutableFloatStateOf(-1f) }
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    // Map span IDs to their absolute starting offset along the member
    val spanOffsets = remember(spans) {
        val offsets = mutableMapOf<UUID, Double>()
        var currentOffset = 0.0
        spans.forEach { span ->
            offsets[span.id] = currentOffset
            currentOffset += span.length.inInches
        }
        offsets
    }

    // Helper to get absolute X coordinate for a result point
    val getAbsX = { point: PointCapacityResult ->
        (spanOffsets[point.demand.spanId] ?: 0.0) + point.demand.x.inInches
    }

    // Reset selection when results change
    LaunchedEffect(results) {
        selectedPoint = null
        touchX = -1f
    }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .pointerInput(results, totalLength, spanOffsets) {
                    detectTapGestures { offset ->
                        touchX = offset.x
                        selectedPoint = findClosestPoint(
                            offset.x,
                            this.size.width.toFloat(),
                            results,
                            totalLength,
                            spanOffsets
                        )
                    }
                }
                .pointerInput(results, totalLength, spanOffsets) {
                    detectDragGestures { change, _ ->
                        touchX = change.position.x
                        selectedPoint = findClosestPoint(
                            change.position.x,
                            this.size.width.toFloat(),
                            results,
                            totalLength,
                            spanOffsets
                        )
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                canvasWidth = size.width
                val canvasHeight = size.height
                val beamHeight = 8.dp.toPx() // Thinner beam line
                val centerY = canvasHeight / 2f
                val beamTop = centerY - beamHeight / 2f

                // 1. Draw Supports
                var currentX = 0.0
                drawSupport(x = 0f, y = centerY, isPinned = true) // Start support
                spans.forEach { span ->
                    currentX += span.length.inInches
                    val xPos = (currentX / totalLength).toFloat() * canvasWidth
                    drawSupport(x = xPos, y = centerY, isPinned = false) // Intermediate/End support
                }

                // 2. Draw the Heat Map Segments
                if (results.isNotEmpty()) {
                    for (i in 0 until (results.size - 1)) {
                        val p1 = results[i]
                        val p2 = results[i + 1]

                        // Use absolute X coordinates for segments
                        val startX = (getAbsX(p1) / totalLength).toFloat() * canvasWidth
                        val endX = (getAbsX(p2) / totalLength).toFloat() * canvasWidth

                        // Skip segments that wrap across spans (if results are not filtered by span)
                        if (endX >= startX) {
                            drawRect(
                                color = getHeatMapColor(p1.utilizationRatio),
                                topLeft = Offset(startX, beamTop),
                                size = Size((endX - startX) + 1f, beamHeight)
                            )
                        }
                    }
                }

                // 3. Draw Selection Indicator
                selectedPoint?.let { point ->
                    if (touchX >= 0) {
                        val selX = (getAbsX(point) / totalLength).toFloat() * canvasWidth
                        drawLine(
                            color = Color.Black.copy(alpha = 0.5f),
                            start = Offset(selX, 0f),
                            end = Offset(selX, canvasHeight),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }

            // 4. Interactive Overlay Tooltip
            selectedPoint?.let { point ->
                Surface(
                    modifier = Modifier
                        .align(if (touchX > canvasWidth / 2) Alignment.TopStart else Alignment.TopEnd)
                        .padding(8.dp)
                        .width(200.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            "x = ${"%.2f".format(getAbsX(point))} in",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            "Ratio: ${"%.2f".format(point.utilizationRatio)}",
                            color = if (point.utilizationRatio > 1.0) Color.Red else Color.Unspecified,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text("Limit State: ${point.governingLimitState}", fontSize = 11.sp)
                        Text("Comp. Flange: ${point.compressionFlange}", fontSize = 11.sp)
                        Text("Lb: ${"%.2f".format(point.Lb)} in", fontSize = 11.sp)
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

private fun DrawScope.drawSupport(x: Float, y: Float, isPinned: Boolean) {
    val size = 20f
    val path = Path().apply {
        moveTo(x, y + 4.dp.toPx()) // Offset from beam center
        lineTo(x - size / 2, y + size + 4.dp.toPx())
        lineTo(x + size / 2, y + size + 4.dp.toPx())
        close()
    }
    drawPath(path, color = Color.Gray)
    if (!isPinned) {
        // Draw a small circle below for roller
        drawCircle(color = Color.Gray, radius = 3f, center = Offset(
            x,
            y + size + 8.dp.toPx()
        )
        )
    }
}


private fun findClosestPoint(
    touchX: Float,
    canvasWidth: Float,
    results: List<PointCapacityResult>,
    totalLength: Double,
    spanOffsets: Map<UUID, Double>
): PointCapacityResult? {
    if (results.isEmpty()) return null
    val targetX = (touchX / canvasWidth) * totalLength
    return results.minByOrNull { point ->
        val absX = (spanOffsets[point.demand.spanId] ?: 0.0) + point.demand.x.inInches
        abs(absX - targetX)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(12.dp)
                .background(color, androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
        )
        Text(text = label, modifier = Modifier.padding(start = 4.dp), fontSize = 10.sp)
    }
}

/**
 * Maps utilization ratio to standard engineering status colors.
 * Note: These thresholds match the Legend displayed above.
 */
private fun getHeatMapColor(ratio: Double): Color = when {
    ratio > 1.0 -> Color(0xFFF44336) // Red (Failing)
    ratio > 0.9 -> Color(0xFFFF9800) // Orange (Warning)
    ratio > 0.7 -> Color(0xFFFFC107) // Amber (Caution)
    else -> Color(0xFF4CAF50)        // Green (Safe)
}