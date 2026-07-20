package com.lz.beam.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.lz.model.structural.Load
import com.lz.model.structural.StructuralMember
import com.lz.model.units.inInches

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

        val totalLengthInches =
            member.spans.sumOf { it.length.inInches }

        if (totalLengthInches <= 0.0) return@Canvas

        val supportSize = 30f

        var currentXInches = 0.0

        member.spans.forEachIndexed { index, span ->

            val startNode =
                member.nodes.firstOrNull {
                    it.id == span.startNodeId
                }

            val endNode =
                member.nodes.firstOrNull {
                    it.id == span.endNodeId
                }

            val startX =
                (currentXInches / totalLengthInches).toFloat() * width

            currentXInches += span.length.inInches

            val endX =
                (currentXInches / totalLengthInches).toFloat() * width

            //
            // Start node
            //
            if (index == 0) {

                drawStructuralJoint(
                    x = startX,
                    y = midY,
                    label = null,
                    color = supportColor,
                    isVisible = false
                )

                startNode?.let {

                    drawStructuralSupport(
                        condition = it.boundaryCondition,
                        x = startX,
                        y = midY,
                        size = supportSize,
                        color = supportColor
                    )

                }
            }

            //
            // Beam span
            //
            drawLine(
                color = beamColor,
                start = Offset(startX, midY),
                end = Offset(endX, midY),
                strokeWidth = 8f
            )

            //
            // End node
            //
            drawStructuralJoint(
                x = endX,
                y = midY,
                label = null,
                color = supportColor,
                isVisible = false
            )

            endNode?.let {

                drawStructuralSupport(
                    condition = it.boundaryCondition,
                    x = endX,
                    y = midY,
                    size = supportSize,
                    color = supportColor
                )

            }
        }

        //
        // Draw loads
        //
        loads.forEach { load ->

            val span =
                member.spans.find { it.id == load.spanId }
                    ?: return@forEach

            val spanIndex =
                member.spans.indexOf(span)

            val accumulatedInches =
                member.spans
                    .take(spanIndex)
                    .sumOf { it.length.inInches }

            val spanStartX =
                (accumulatedInches / totalLengthInches)
                    .toFloat() * width

            val spanWidth =
                (span.length.inInches / totalLengthInches)
                    .toFloat() * width

            when (load) {

                is Load.PointLoad -> {

                    val x =
                        spanStartX +
                                (load.locationStart.inInches /
                                        span.length.inInches).toFloat() *
                                spanWidth

                    val arrowLength = 50f
                    val headSize = 15f

                    drawLine(
                        color = loadColor,
                        start = Offset(x, midY - arrowLength),
                        end = Offset(x, midY - 4f),
                        strokeWidth = 6f
                    )

                    drawPath(

                        path = Path().apply {

                            moveTo(x, midY - 4f)
                            lineTo(x - headSize / 2, midY - headSize - 4f)
                            lineTo(x + headSize / 2, midY - headSize - 4f)
                            close()

                        },

                        color = loadColor

                    )
                }

                is Load.UniformDistributedLoad -> {

                    val x1 =
                        spanStartX +
                                (load.locationStart.inInches /
                                        span.length.inInches).toFloat() *
                                spanWidth

                    val x2 =
                        spanStartX +
                                (load.locationEnd.inInches /
                                        span.length.inInches).toFloat() *
                                spanWidth

                    val arrowLength = 30f
                    val headSize = 8f

                    val arrowCount =
                        ((x2 - x1) / 20f)
                            .toInt()
                            .coerceIn(2, 10)

                    val spacing =
                        if (arrowCount > 1)
                            (x2 - x1) / (arrowCount - 1)
                        else
                            0f

                    drawLine(
                        color = loadColor,
                        start = Offset(x1, midY - arrowLength),
                        end = Offset(x2, midY - arrowLength),
                        strokeWidth = 2f
                    )

                    repeat(arrowCount) { i ->

                        val x = x1 + i * spacing

                        drawLine(
                            color = loadColor,
                            start = Offset(x, midY - arrowLength),
                            end = Offset(x, midY - 2f),
                            strokeWidth = 3f
                        )

                        drawPath(

                            path = Path().apply {

                                moveTo(x, midY - 2f)
                                lineTo(x - headSize / 2, midY - headSize - 2f)
                                lineTo(x + headSize / 2, midY - headSize - 2f)
                                close()

                            },

                            color = loadColor

                        )
                    }
                }

                else -> Unit
            }
        }
    }
}