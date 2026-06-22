package com.lz.ui

import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.lz.solver.analysis.AnalysisPoint
import java.util.Locale
import kotlin.math.abs

@Composable
fun AnalysisChart(
    title: String,
    points: List<AnalysisPoint>,
    unitLabel: String,
    lineColor: Color,
    modifier: Modifier = Modifier,
    invertY: Boolean = false
) {
    if (points.isEmpty()) return

    val sortedPoints = remember(points) { points.sortedBy { it.x.inches } }
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp) // Slightly taller for labels
                .padding(vertical = 16.dp)
        ) {
            val w = size.width
            val h = size.height
            val margin = 40f // Increased margin for labels
            val chartW = w - 2 * margin
            val centerY = h / 2f

            val yInvertMultiplier = if (invertY) -1f else 1f

            val minX = sortedPoints.firstOrNull()?.x?.inches ?: 0.0
            val maxX = sortedPoints.lastOrNull()?.x?.inches ?: 1.0
            val totalL = (maxX - minX).coerceAtLeast(1.0)

            val absMaxVal = sortedPoints.maxOfOrNull { abs(it.value) }?.coerceAtLeast(1e-6) ?: 1.0
            val scaleY = (h / 2.5f) / absMaxVal // Scaled to fit within roughly 80% of half-height

            // Baseline
            drawLine(
                color = onSurface.copy(alpha = 0.2f),
                start = Offset(margin, centerY),
                end = Offset(w - margin, centerY),
                strokeWidth = 2f
            )

            val path = Path()
            sortedPoints.forEachIndexed { i, p ->
                val x = margin + ((p.x.inches - minX) / totalL).toFloat() * chartW
                val y = centerY - (p.value.toFloat() * scaleY.toFloat() * yInvertMultiplier)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 4f)
            )

            // Label Max and Min
            val maxPt = sortedPoints.maxByOrNull { it.value }
            val minPt = sortedPoints.minByOrNull { it.value }

            fun drawValueLabel(p: AnalysisPoint) {
                val ptX = margin + ((p.x.inches - minX) / totalL).toFloat() * chartW
                val ptY = centerY - (p.value.toFloat() * scaleY.toFloat() * yInvertMultiplier)

                drawCircle(lineColor, 6f, Offset(ptX, ptY))

                val label = String.format(Locale.US, "%.1f %s", p.value, unitLabel)
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    ptX,
                    if ((p.value >= 0 && !invertY) || (p.value < 0 && invertY)) ptY - 15f else ptY + 35f,
                    android.graphics.Paint().apply {
                        color = onSurface.toArgb()
                        textSize = 32f
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = Typeface.DEFAULT_BOLD
                    }
                )
            }

            maxPt?.let { drawValueLabel(it) }
            minPt?.let {
                if (abs(
                        it.value - (maxPt?.value ?: 0.0)
                    ) > absMaxVal * 0.1
                ) drawValueLabel(it)
            }
        }
    }
}
