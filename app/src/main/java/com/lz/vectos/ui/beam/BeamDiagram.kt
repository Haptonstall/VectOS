package com.lz.vectos.ui.beam

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.lz.model.units.inInches
import com.lz.vectos.domain.structural.Load
import com.lz.model.structural.StructuralMember
import com.lz.model.structural.SupportCondition
import com.lz.model.units.*

@Composable
fun BeamDiagram(
    member: StructuralMember,
    loads: List<Load>,
    modifier: Modifier = Modifier
) {
    val beamColor = MaterialTheme.colorScheme.primary
    val supportColor = MaterialTheme.colorScheme.secondary
    val loadColor = MaterialTheme.colorScheme.error

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f + 20f
        val totalLengthInches = member.spans.sumOf { it.length.inInches }
        if (totalLengthInches <= 0) return@Canvas

        val supportSize = 30f

        // 1. Draw Beam Line and Supports
        var currentXInches = 0.0
        
        // Initial joint
        drawStructuralJoint(0f, midY, null, supportColor, false)
        member.spans.firstOrNull()?.let { firstSpan ->
            drawStructuralSupport(firstSpan.startSupport, 0f, midY, supportSize, supportColor)
        }

        member.spans.forEach { span ->
            val startX = (currentXInches / totalLengthInches).toFloat() * width
            currentXInches += span.length.inInches
            val endX = (currentXInches / totalLengthInches).toFloat() * width

            drawLine(
                color = beamColor,
                start = Offset(startX, midY),
                end = Offset(endX, midY),
                strokeWidth = 8f
            )

            drawStructuralJoint(endX, midY, null, supportColor, false)
            drawStructuralSupport(span.endSupport, endX, midY, supportSize, supportColor)
        }

        // 2. Draw Loads
        loads.forEach { load ->
            val span = member.spans.find { it.id == load.spanId } ?: return@forEach
            val spanIdx = member.spans.indexOf(span)
            val accumulatedInches = member.spans.take(spanIdx).sumOf { it.length.inInches }
            
            val spanStartX = (accumulatedInches / totalLengthInches).toFloat() * width
            val spanW = (span.length.inInches / totalLengthInches).toFloat() * width

            when (load) {
                is Load.PointLoad -> {
                    val x = spanStartX + (load.locationStart.inInches / span.length.inInches).toFloat() * spanW
                    val arrowLen = 50f
                    val headSize = 15f
                    
                    drawLine(
                        color = loadColor,
                        start = Offset(x, midY - arrowLen),
                        end = Offset(x, midY - 4f),
                        strokeWidth = 6f
                    )
                    val headPath = Path().apply {
                        moveTo(x, midY - 4f)
                        lineTo(x - headSize / 2, midY - headSize - 4f)
                        lineTo(x + headSize / 2, midY - headSize - 4f)
                        close()
                    }
                    drawPath(path = headPath, color = loadColor)
                }
                is Load.UniformDistributedLoad -> {
                    val x1 = spanStartX + (load.locationStart.inInches / span.length.inInches).toFloat() * spanW
                    val x2 = spanStartX + (load.locationEnd.inInches / span.length.inInches).toFloat() * spanW
                    val arrowLen = 30f
                    val headSize = 8f
                    val arrowCount = ((x2 - x1) / 20f).toInt().coerceIn(2, 10)
                    val spacing = if (arrowCount > 1) (x2 - x1) / (arrowCount - 1) else 0f

                    drawLine(
                        color = loadColor,
                        start = Offset(x1, midY - arrowLen),
                        end = Offset(x2, midY - arrowLen),
                        strokeWidth = 2f
                    )

                    for (i in 0 until arrowCount) {
                        val x = x1 + i * spacing
                        drawLine(
                            color = loadColor,
                            start = Offset(x, midY - arrowLen),
                            end = Offset(x, midY - 2f),
                            strokeWidth = 3f
                        )
                        val headPath = Path().apply {
                            moveTo(x, midY - 2f)
                            lineTo(x - headSize / 2, midY - headSize - 2f)
                            lineTo(x + headSize / 2, midY - headSize - 2f)
                            close()
                        }
                        drawPath(path = headPath, color = loadColor)
                    }
                }
                else -> {}
            }
        }
    }
}
