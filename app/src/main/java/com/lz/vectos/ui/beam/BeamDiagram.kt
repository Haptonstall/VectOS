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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.lz.vectos.domain.beam.LoadType

@Composable
fun BeamDiagram(
    loadType: LoadType,
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
        val startX = 0f
        val endX = width
        val supportSize = 30f

        // 1. Draw Beam Line
        drawLine(
            color = beamColor,
            start = Offset(startX, midY),
            end = Offset(endX, midY),
            strokeWidth = 8f
        )

        // 2. Draw Left Support (Pinned)
        val leftSupportPath = Path().apply {
            moveTo(startX, midY)
            lineTo(startX - supportSize / 2, midY + supportSize)
            lineTo(startX + supportSize / 2, midY + supportSize)
            close()
        }
        drawPath(path = leftSupportPath, color = supportColor)

        // 3. Draw Right Support (Roller)
        drawCircle(
            color = supportColor,
            radius = supportSize / 2,
            center = Offset(endX, midY + supportSize / 2),
            style = Stroke(width = 4f)
        )
        drawLine(
            color = supportColor,
            start = Offset(endX - supportSize / 2, midY + supportSize),
            end = Offset(endX + supportSize / 2, midY + supportSize),
            strokeWidth = 4f
        )

        // 4. Draw Loads
        when (loadType) {
            LoadType.POINT_LOAD_MIDSPAN -> {
                val midX = width / 2f
                val arrowLen = 50f
                val headSize = 15f
                
                // Main arrow line
                drawLine(
                    color = loadColor,
                    start = Offset(midX, midY - arrowLen),
                    end = Offset(midX, midY),
                    strokeWidth = 6f
                )
                // Arrow head
                val headPath = Path().apply {
                    moveTo(midX, midY)
                    lineTo(midX - headSize / 2, midY - headSize)
                    lineTo(midX + headSize / 2, midY - headSize)
                    close()
                }
                drawPath(path = headPath, color = loadColor)
            }
            LoadType.UNIFORMLY_DISTRIBUTED_LOAD -> {
                val arrowCount = 10
                val arrowLen = 30f
                val headSize = 8f
                val spacing = width / (arrowCount - 1)

                // Top boundary line for UDL
                drawLine(
                    color = loadColor,
                    start = Offset(startX, midY - arrowLen),
                    end = Offset(endX, midY - arrowLen),
                    strokeWidth = 2f
                )

                for (i in 0 until arrowCount) {
                    val x = i * spacing
                    drawLine(
                        color = loadColor,
                        start = Offset(x, midY - arrowLen),
                        end = Offset(x, midY),
                        strokeWidth = 3f
                    )
                    // Mini arrow heads
                    val headPath = Path().apply {
                        moveTo(x, midY)
                        lineTo(x - headSize / 2, midY - headSize)
                        lineTo(x + headSize / 2, midY - headSize)
                        close()
                    }
                    drawPath(path = headPath, color = loadColor)
                }
            }
        }
    }
}
