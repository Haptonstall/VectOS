package com.lz.beam.ui

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lz.model.structural.SupportCondition

/**
 * Shared drawing utilities for structural visualization.
 */

fun DrawScope.drawStructuralSupport(
    condition: SupportCondition,
    x: Float,
    y: Float,
    size: Float,
    color: Color
) {
    when (condition) {
        SupportCondition.PINNED -> {
            // Triangle symbol
            val path = Path().apply {
                moveTo(x, y)
                lineTo(x - size / 2, y + size)
                lineTo(x + size / 2, y + size)
                close()
            }
            drawPath(path = path, color = color)
        }
        SupportCondition.ROLLER -> {
            // Circle on a line
            val radius = size / 3
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(x, y + radius),
                style = Stroke(width = 2.dp.toPx())
            )
            drawLine(
                color = color,
                start = Offset(x - size / 2, y + size),
                end = Offset(x + size / 2, y + size),
                strokeWidth = 2.dp.toPx()
            )
        }
        SupportCondition.FIXED -> {
            // Vertical line with hatching
            val lineLen = size
            drawLine(
                color = color,
                start = Offset(x, y - lineLen / 2),
                end = Offset(x, y + lineLen / 2),
                strokeWidth = 4.dp.toPx()
            )
            // Hatching
            val hatchLen = 8.dp.toPx()
            val spacing = 4.dp.toPx()
            val count = (lineLen / spacing).toInt()
            for (i in 0..count) {
                val hatchY = (y - lineLen / 2) + i * spacing
                drawLine(
                    color = color,
                    start = Offset(x, hatchY),
                    end = Offset(x - hatchLen, hatchY - hatchLen),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        SupportCondition.FREE -> {
            // No symbol for free, but we draw a joint dot elsewhere if needed
        }
        else -> {}
    }
}

fun DrawScope.drawStructuralJoint(
    x: Float,
    y: Float,
    label: String?,
    color: Color,
    isVisible: Boolean
) {
    // Always draw a small dot for the joint
    drawCircle(
        color = color,
        radius = 3.dp.toPx(),
        center = Offset(x, y)
    )

    if (isVisible && label != null) {
        val labelY = y + 24.dp.toPx()

        // Background for label
        drawCircle(
            color = color.copy(alpha = 0.1f),
            radius = 10.dp.toPx(),
            center = Offset(x, labelY)
        )

        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                this.color = color.toArgb()
                this.textSize = 14.sp.toPx()
                this.textAlign = Paint.Align.CENTER
                this.isFakeBoldText = true
            }
            canvas.nativeCanvas.drawText(
                label,
                x,
                labelY + 4.dp.toPx(),
                paint
            )
        }
    }
}