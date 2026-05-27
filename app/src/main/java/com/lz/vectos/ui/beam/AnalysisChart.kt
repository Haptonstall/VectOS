package com.lz.vectos.ui.beam

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lz.vectos.domain.structural.analysis.AnalysisPoint
import com.lz.vectos.domain.units.*
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs

@Composable
fun AnalysisChart(
    title: String,
    points: List<AnalysisPoint>,
    unitLabel: String,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val onSurface = MaterialTheme.colorScheme.onSurface
    
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 16.dp)
        ) {
            val w = size.width
            val h = size.height
            val margin = 20f
            val chartW = w - 2 * margin
            val chartH = h
            val centerY = h / 2f

            val minX = points.first().x.inches
            val maxX = points.last().x.inches
            val totalL = maxX - minX

            val absMaxVal = points.maxOfOrNull { abs(it.value) } ?: 1.0
            val scaleY = (chartH / 2f) / (absMaxVal.coerceAtLeast(0.1))

            // Baseline
            drawLine(onSurface.copy(alpha = 0.3f), Offset(margin, centerY), Offset(w - margin, centerY), 1f)

            val path = Path()
            points.forEachIndexed { i, p ->
                val x = margin + ((p.x.inches - minX) / totalL).toFloat() * chartW
                val y = centerY - (p.value.toFloat() * scaleY.toFloat())
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(path, lineColor, style = Stroke(width = 3f))

            // Highlight Max and Min
            val maxPt = points.maxByOrNull { it.value }
            val minPt = points.minByOrNull { it.value }

            fun drawValueLabel(p: AnalysisPoint, isMax: Boolean) {
                val x = margin + ((p.x.inches - minX) / totalL).toFloat() * chartW
                val y = centerY - (p.value.toFloat() * scaleY.toFloat())
                
                drawCircle(lineColor, 4f, Offset(x, y))
                
                val label = String.format("%.1f %s", p.value, unitLabel)
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x,
                    if (p.value >= 0) y - 10f else y + 30f,
                    Paint().apply {
                        color = onSurface.toArgb()
                        textSize = 28f
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.DEFAULT_BOLD
                    }
                )
            }

            maxPt?.let { drawValueLabel(it, true) }
            minPt?.let { if (abs(it.value - (maxPt?.value ?: 0.0)) > absMaxVal * 0.1) drawValueLabel(it, false) }
        }
    }
}
