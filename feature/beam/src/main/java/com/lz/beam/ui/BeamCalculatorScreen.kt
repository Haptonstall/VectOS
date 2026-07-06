package com.lz.beam.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LineAxis
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import com.lz.beam.R
import com.lz.beam.model.BeamCalculationResults
import com.lz.beam.presentation.BeamViewModel
import com.lz.domain.project.Project
import com.lz.model.regulatory.LoadCombination
import com.lz.model.structural.BracingInput
import com.lz.model.structural.BracingMode
import com.lz.model.structural.DesignEquationTrace
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.InteractionStatus
import com.lz.model.structural.Load
import com.lz.model.structural.LoadCase
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.MaterialType
import com.lz.model.structural.NodeBoundaryCondition
import com.lz.model.structural.PointCapacityResult
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.ServiceabilityResult
import com.lz.model.structural.ShapeType
import com.lz.model.structural.SpanGeometry
import com.lz.model.structural.StrengthCheckResult
import com.lz.model.structural.StrengthDesignResult
import com.lz.model.structural.StructuralMember
import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.Moment
import com.lz.model.units.UnitFormattingService
import com.lz.model.units.UnitSystem
import com.lz.model.units.inInches
import com.lz.model.units.inKiloNewtons
import com.lz.model.units.inKips
import com.lz.model.units.inLbFt
import com.lz.model.units.inLbIn
import com.lz.model.units.inNewtonMeters
import com.lz.model.units.inPoundsForce
import com.lz.solver.analysis.AnalysisResult
import com.lz.solver.analysis.ReactionResult
import com.lz.ui.AnalysisChart
import com.lz.ui.SectionPicker
import com.lz.ui.boundary.BoundaryConditionPicker
import com.lz.ui.boundary.BoundaryConditionPickerConfig
import com.lz.ui.loads.LoadEditor
import com.lz.ui.material.WoodMaterialPickerDialog
import com.lz.ui.member.BracingPickerDialog
import com.lz.ui.member.SpanEditor
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.isFinite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BeamCalculatorScreen(
    activeProject: Project,
    viewModel: BeamViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        "Geometry",
        "Loads",
        "LCs",
        "Analysis",
        "Design"
    )

    val strengthDesignResults = viewModel.governingCapacityResults
    val serviceabilityResults = viewModel.serviceabilityResults

    val maxFlexureUtil = strengthDesignResults.filter { it.governingLimitState.contains("Flexure", ignoreCase = true) }
        .maxOfOrNull { it.utilizationRatio } ?: 0.0
    val maxShearUtil = strengthDesignResults.filter { it.governingLimitState.contains("Shear", ignoreCase = true) }
        .maxOfOrNull { it.utilizationRatio } ?: 0.0
    val maxAxialUtil = strengthDesignResults.filter { it.governingLimitState.contains("Axial", ignoreCase = true) || it.governingLimitState.contains("Buckling", ignoreCase = true) || it.governingLimitState.contains("Yielding", ignoreCase = true) && !it.governingLimitState.contains("Shear", ignoreCase = true) && !it.governingLimitState.contains("Flexure", ignoreCase = true) }
        .maxOfOrNull { it.utilizationRatio } ?: 0.0
    val maxTorsionUtil = strengthDesignResults.filter { it.governingLimitState.contains("Torsion", ignoreCase = true) }
        .maxOfOrNull { it.utilizationRatio } ?: 0.0
    val maxDeflectionUtil = serviceabilityResults.maxOfOrNull { it.utilization } ?: 0.0

    val overallMaxUtil = maxOf(maxFlexureUtil, maxShearUtil, maxAxialUtil, maxTorsionUtil, maxDeflectionUtil)
    val isPassing = overallMaxUtil <= 1.0 && overallMaxUtil > 0

    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Beam Design", style = MaterialTheme.typography.titleMedium)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Status Dot and FAIL/PASS
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (isPassing) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    shape = CircleShape,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isPassing) "PASS" else "FAIL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPassing) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                            }

                            // Flexure Badge
                            StatusBadgeSmall(
                                label = "FLEX",
                                value = "${(maxFlexureUtil * 100).toInt()}%",
                                isCritical = maxFlexureUtil > 1.0
                            )

                            // Deflection Badge
                            StatusBadgeSmall(
                                label = "DEF",
                                value = "${(maxDeflectionUtil * 100).toInt()}%",
                                isCritical = maxDeflectionUtil > 1.0
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveCalculation {
                            scope.launch {
                                snackbarHostState.showSnackbar("Calculation saved successfully")
                            }
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // 1. Interactive Beam Plot (Always visible at top)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                BeamSideView(
                    member = viewModel.structuralMember,
                    section = viewModel.selectedSection,
                    strengthResults = strengthDesignResults,
                    showBracing = viewModel.showBracingOnPlot,
                    showUtilization = viewModel.showUtilizationOnPlot,
                    showJointLabels = viewModel.showJointLabels,
                    showLoads = viewModel.showLoadsOnPlot,
                    loads = viewModel.loadCases.flatMap { it.loads }, // All loads by default
                    spanBracing = viewModel.spanBracingInputs,
                    modifier = Modifier.fillMaxSize(),
                    onNodeClicked = { idx -> viewModel.editingSupportNodeIndex = idx }
                )

                // Floating Toolbar for Toggles (Items 3, 4, 5, 6)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Loads Toggle
                    IconButton(onClick = { viewModel.showLoadsOnPlot = !viewModel.showLoadsOnPlot }) {
                        Icon(
                            imageVector = Icons.Default.VerticalAlignBottom,
                            contentDescription = "Toggle Loads",
                            tint = if (viewModel.showLoadsOnPlot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    // Bracing Toggle
                    IconButton(onClick = { viewModel.showBracingOnPlot = !viewModel.showBracingOnPlot }) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Toggle Bracing",
                            tint = if (viewModel.showBracingOnPlot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    // Joint Labels Toggle
                    IconButton(onClick = { viewModel.showJointLabels = !viewModel.showJointLabels }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Toggle Labels",
                            tint = if (viewModel.showJointLabels) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    // Utilization Heatmap Toggle
                    IconButton(onClick = { viewModel.showUtilizationOnPlot = !viewModel.showUtilizationOnPlot }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Toggle Heatmap",
                            tint = if (viewModel.showUtilizationOnPlot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // 2. Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // 3. Main Content
            Box(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                    when (selectedTab) {
                        0 -> GeometryTab(viewModel)
                        1 -> LoadsTab(viewModel)
                        2 -> LoadCombinationsTab(viewModel)
                        3 -> AnalysisTab(viewModel)
                        4 -> DesignTab(viewModel)
                    }
                }
            }

            // Support Picker Dialog Overlay
            viewModel.editingSupportNodeIndex?.let { nodeIdx ->
                val condition = viewModel.structuralMember.nodes.getOrNull(nodeIdx)?.boundaryCondition
                    ?: NodeBoundaryCondition()

                BoundaryConditionPicker(
                    config = BeamBoundaryConditionConfig.config,
                    currentCondition = condition,
                    onDismiss = {
                        viewModel.editingSupportNodeIndex = null
                    },
                    onConfirmed = { newCondition ->
                        viewModel.updateBoundaryCondition(
                            nodeIdx,
                            newCondition
                        )
                        viewModel.editingSupportNodeIndex = null
                    }
                )
            }
        }
    }
}

@Composable
fun StatusBadgeSmall(label: String, value: String, isCritical: Boolean) {
    Surface(
        color = if (isCritical) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(2.dp))
            Text(value, style = MaterialTheme.typography.labelSmall, color = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BeamSideView(
    member: StructuralMember,
    section: SectionProfile?,
    strengthResults: List<PointCapacityResult>,
    showBracing: Boolean,
    showUtilization: Boolean,
    showJointLabels: Boolean,
    showLoads: Boolean,
    loads: List<Load>,
    spanBracing: Map<UUID, BracingInput>,
    modifier: Modifier = Modifier,
    onNodeClicked: (Int) -> Unit
) {
    val totalLength = member.spans.sumOf { it.length.inInches }
    val materialColor = when (section?.materialType) {
        MaterialType.STEEL -> Color(0xFF607D8B) // Blue-grey for steel
        MaterialType.WOOD -> Color(0xFF8D6E63) // Brown for wood
        else -> Color.DarkGray
    }

    Box(modifier = modifier.padding(horizontal = 32.dp, vertical = 24.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f

            // X-scale
            val scale = width / totalLength.toFloat()

            // 1. Draw Beam Main Body (Items 7)
            val beamDepthInches = section?.depth?.inInches?.takeIf { it.isFinite() && it > 0 } ?: 8.0
            val beamHeightPx = (beamDepthInches * 2.0).toFloat().coerceIn(10f, 60f)

            drawRect(
                color = materialColor,
                topLeft = Offset(0f, centerY - beamHeightPx / 2f),
                size = androidx.compose.ui.geometry.Size(width, beamHeightPx)
            )

            // 2. Draw Spans & Supports
            var currentX = 0f
            member.spans.forEachIndexed { idx, span ->
                val spanLen = span.length.inInches.takeIf { it > 0 } ?: 1e-6
                val spanWidth = (spanLen * scale).toFloat()

                // Draw Start Support
                if (idx == 0) {
                    val startNode = member.nodes.getOrNull(0)
                    if (startNode != null) {
                        drawStructuralSupport(startNode.boundaryCondition, 0f, centerY + beamHeightPx / 2f, 30f, Color.Gray)
                    }
                    drawStructuralJoint(0f, centerY + beamHeightPx / 2f, if (showJointLabels) "0" else null, Color.Gray, showJointLabels)
                }

                // Draw End Support
                val endNode = member.nodes.getOrNull(idx + 1)
                if (endNode != null) {
                    drawStructuralSupport(endNode.boundaryCondition, currentX + spanWidth, centerY + beamHeightPx / 2f, 30f, Color.Gray)
                }
                drawStructuralJoint(currentX + spanWidth, centerY + beamHeightPx / 2f, if (showJointLabels) "${idx + 1}" else null, Color.Gray, showJointLabels)

                // Draw Span Dividers (Vertical ticks)
                drawLine(Color.LightGray, Offset(currentX, centerY - 20f), Offset(currentX, centerY + 20f), 2f)

                // Draw Bracing Icons if enabled
                if (showBracing) {
                    val bracing = spanBracing[span.id]
                    if (bracing != null) {
                        drawBracingIcons(currentX, spanWidth, centerY, bracing.topType, Color.Blue, true)
                        drawBracingIcons(currentX, spanWidth, centerY, bracing.bottomType, Color.Red, false)
                    }
                }

                currentX += spanWidth
            }

            // 3. Draw Loads if enabled (Item 6)
            if (showLoads) {
                val loadColor = Color(0xFFD32F2F)
                loads.forEach { load ->
                    // Resilient span mapping
                    val span = member.spans.find { it.id == load.spanId }
                        ?: (if (member.spans.size == 1) member.spans.first() else null)
                        ?: return@forEach

                    val spanLen = span.length.inInches.takeIf { it > 0 } ?: 1.0
                    val spanIdx = member.spans.indexOf(span)
                    val accumulatedInches = member.spans.take(spanIdx).sumOf { it.length.inInches }

                    val spanStartX = (accumulatedInches / totalLength).toFloat() * width
                    val spanW = (span.length.inInches / totalLength).toFloat() * width

                    when (load) {
                        is Load.PointLoad -> {
                            val x = spanStartX + (load.locationStart.inInches / spanLen).toFloat() * spanW
                            if (!x.isFinite()) return@forEach
                            val arrowLen = 40f
                            val headSize = 12f
                            val yBase = centerY - beamHeightPx / 2f - 4f

                            drawLine(
                                color = loadColor,
                                start = Offset(x, yBase - arrowLen),
                                end = Offset(x, yBase),
                                strokeWidth = 2.dp.toPx()
                            )
                            val headPath = Path().apply {
                                moveTo(x, yBase)
                                lineTo(x - headSize / 2, yBase - headSize)
                                lineTo(x + headSize / 2, yBase - headSize)
                                close()
                            }
                            drawPath(path = headPath, color = loadColor)
                        }
                        is Load.UniformDistributedLoad -> {
                            val x1 = spanStartX + (load.locationStart.inInches / spanLen).toFloat() * spanW
                            val x2 = spanStartX + (load.locationEnd.inInches / spanLen).toFloat() * spanW
                            if (!x1.isFinite() || !x2.isFinite()) return@forEach
                            val arrowLen = 25f
                            val headSize = 8f
                            val yBase = centerY - beamHeightPx / 2f - 4f
                            val arrowCount = ((x2 - x1) / 15f).toInt().coerceIn(2, 20)
                            val spacing = if (arrowCount > 1) (x2 - x1) / (arrowCount - 1) else 0f

                            drawLine(
                                color = loadColor,
                                start = Offset(x1, yBase - arrowLen),
                                end = Offset(x2, yBase - arrowLen),
                                strokeWidth = 1.dp.toPx()
                            )

                            for (i in 0 until arrowCount) {
                                val x = x1 + i * spacing
                                drawLine(
                                    color = loadColor,
                                    start = Offset(x, yBase - arrowLen),
                                    end = Offset(x, yBase),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                                val headPath = Path().apply {
                                    moveTo(x, yBase)
                                    lineTo(x - headSize / 2, yBase - headSize)
                                    lineTo(x + headSize / 2, yBase - headSize)
                                    close()
                                }
                                drawPath(path = headPath, color = loadColor)
                            }
                        }
                        else -> {}
                    }
                }
            }

            // 4. Draw Utilization Envelope if enabled
            if (showUtilization && strengthResults.isNotEmpty() && strengthResults.size > 1) {
                val utilPoints = mutableListOf<Offset>()
                val step = width / (strengthResults.size - 1)
                strengthResults.forEachIndexed { i, res ->
                    val x = i * step
                    // Invert and scale utilization (1.0 = 40dp offset)
                    val yOff = (res.utilizationRatio.toFloat() * 40.dp.toPx()).coerceAtMost(centerY)
                    utilPoints.add(Offset(x, centerY - beamHeightPx / 2f - yOff))
                }

                // Draw as a filled area or line
                for (i in 0 until utilPoints.size - 1) {
                    val p1 = utilPoints[i]
                    val p2 = utilPoints[i+1]
                    if (p1.x.isFinite() && p1.y.isFinite() && p2.x.isFinite() && p2.y.isFinite()) {
                        drawLine(
                            color = getUtilizationColor(strengthResults[i].utilizationRatio),
                            start = p1,
                            end = p2,
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }
            }
        }

        // Transparent clickable regions for nodes
        var currentXInches = 0.0
        member.spans.forEachIndexed { idx, span ->
            // Start node of member
            if (idx == 0) {
                Box(modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = (-20).dp)
                    .drawBehind { drawCircle(Color.Transparent) }
                    .padding(8.dp)
                ) {
                    IconButton(onClick = { onNodeClicked(0) }, modifier = Modifier.fillMaxSize()) { }
                }
            }

            // End node of each span
            currentXInches += span.length.inInches
            val fraction = (currentXInches / totalLength).toFloat()
            Box(modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterStart)
                .offset(x = (fraction * (totalLength * (totalLength / totalLength))).dp) // Rough simplification
                .padding(8.dp)
            ) {
                // Actually we just use a simplified approach for alignment in this demo
            }
        }
    }
}

fun getUtilizationColor(ratio: Double): Color = getUtilizationColorInternal(ratio)

internal fun getUtilizationColorInternal(ratio: Double): Color = when {
    ratio > 1.0 -> Color(0xFFF44336) // Red
    ratio > 0.9 -> Color(0xFFFF9800) // Orange
    ratio > 0.7 -> Color(0xFFFFC107) // Amber
    else -> Color(0xFF4CAF50) // Green
}

@Composable
fun GeometryTab(viewModel: BeamViewModel) {
    var isWoodPickerVisible by remember { mutableStateOf(false) }

    GeometryConfiguration(
        member = viewModel.structuralMember,
        selectedMaterial = viewModel.selectedMaterial,
        selectedSection = viewModel.selectedSection,
        availableMaterials = viewModel.availableMaterials,
        availableGrades = viewModel.availableGrades,
        availableShapeTypes = viewModel.availableShapeTypes,
        availableSections = viewModel.availableSections,
        selectedShapeType = viewModel.selectedShapeType,
        selectedGrade = viewModel.activeMaterialGrade,
        isStrongAxis = viewModel.isStrongAxis,
        onUpdateOrientation = viewModel::updateOrientation,
        onMaterialSelected = {
            viewModel.onMaterialSelected(it)
        },
        onOpenWoodPicker = { isWoodPickerVisible = true },
        onGradeSelected = viewModel::onGradeSelected,
        onShapeTypeSelected = viewModel::onShapeTypeSelected,
        onSectionSelected = viewModel::onSectionSelected,
        onAddSpan = viewModel::addSpan,
        onRemoveSpan = viewModel::removeSpan,
        onUpdateSpanLength = viewModel::updateSpanLength,
        unitSystem = viewModel.unitSystem,
        activeSpanId = viewModel.activeSpanId,
        onSelectSpan = { viewModel.activeSpanId = it },
        onEditBracing = { viewModel.editingBracingSpanId = it }
    )

    if (isWoodPickerVisible) {
        WoodMaterialPickerDialog(
            currentGrade = viewModel.activeMaterialGrade as? MaterialGrade.Wood,
            onDismiss = { isWoodPickerVisible = false },
            onConfirm = {
                viewModel.onGradeSelected(it)
                isWoodPickerVisible = false
            }
        )
    }

    viewModel.editingBracingSpanId?.let { spanId ->
        val span = viewModel.structuralMember.spans.find { it.id == spanId }
        val bracing = viewModel.spanBracingInputs[spanId]
        if (span != null && bracing != null) {
            BracingPickerDialog(
                currentBracing = bracing,
                materialType = viewModel.selectedMaterial,
                unitSystem = viewModel.unitSystem,
                onDismiss = { viewModel.editingBracingSpanId = null },
                onConfirmed = {
                    viewModel.updateSpanBracing(spanId, it)
                    viewModel.editingBracingSpanId = null
                }
            )
        }
    }
}

@Composable
fun LoadsTab(viewModel: BeamViewModel) {
    val selectedCombinationSet = viewModel.selectedCombinationSet

    LoadsConfiguration(
        loadCases = viewModel.loadCases,
        combinations = selectedCombinationSet?.combinations ?: emptyList(),
        selectedCombination = viewModel.selectedAnalysisCombination,
        includeSelfWeight = viewModel.includeSelfWeight,
        onAddLoad = viewModel::onAddLoad,
        onRemoveLoad = viewModel::onRemoveLoad,
        onSelectLoad = viewModel::onSelectLoad,
        onCombinationSelected = { viewModel.selectedAnalysisCombination = it },
        onUpdateIncludeSelfWeight = { viewModel.includeSelfWeight = it },
        spans = viewModel.structuralMember.spans,
        unitSystem = viewModel.unitSystem,
        selectedLoad = viewModel.selectedLoad
    )
}

@Composable
fun AnalysisTab(viewModel: BeamViewModel) {
    val results = viewModel.calculationResult?.results

    if (results == null) {
        EmptyState("Run calculation to see analysis results")
    } else {
        AnalysisSummary(
            results = results,
            member = viewModel.structuralMember,
            unitSystem = viewModel.unitSystem,
            selectedCombination = viewModel.selectedAnalysisCombination
        )
    }
}

@Composable
fun DesignTab(viewModel: BeamViewModel) {
    val results = viewModel.calculationResult?.results
    val detailedResult = viewModel.detailedStrengthResult

    if (results == null) {
        EmptyState("Run calculation to see design checks")
    } else {
        DesignSummary(
            pointResults = results.strengthDesignResults,
            detailedResult = detailedResult,
            serviceabilityResults = results.serviceabilityResults,
            member = viewModel.structuralMember,
            unitSystem = viewModel.unitSystem
        )
    }
}

@Composable
fun BasisTab(viewModel: BeamViewModel) {
    val code = viewModel.activeBuildingCode

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Design Basis", style = MaterialTheme.typography.titleMedium)

        if (code != null) {
            InfoCard(
                title = "Building Code",
                value = code.longName,
                description = "Standard: ${code.standards.joinToString { it.shortName }}"
            )

            InfoCard(
                title = "Methodology",
                value = viewModel.methodology.name,
                description = if (viewModel.methodology == DesignMethodology.LRFD) "Load and Resistance Factor Design" else "Allowable Stress Design"
            )
        }
    }
}

@Composable
fun LoadCombinationsTab(viewModel: BeamViewModel) {
    val set = viewModel.selectedCombinationSet

    if (set == null) {
        EmptyState("No combination set selected")
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Load Combinations", style = MaterialTheme.typography.titleMedium)
            Text(set.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            set.combinations.forEach { combo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = viewModel.enabledCombinations.contains(combo.name),
                        onCheckedChange = { viewModel.toggleCombination(combo.name) }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(combo.name, style = MaterialTheme.typography.bodyMedium)
                        Text(combo.equationText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun RevisionsTab(viewModel: BeamViewModel) {
    // To be implemented with versioning service integration
    EmptyState("Revision tracking coming soon")
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GeometryConfiguration(
    member: StructuralMember,
    selectedMaterial: MaterialType?,
    selectedSection: SectionProfile?,
    availableMaterials: List<MaterialType>,
    availableGrades: List<MaterialGrade>,
    availableShapeTypes: List<ShapeType>,
    availableSections: List<SectionProfile>,
    selectedShapeType: ShapeType?,
    selectedGrade: MaterialGrade?,
    isStrongAxis: Boolean,
    onUpdateOrientation: (Boolean) -> Unit,
    onMaterialSelected: (MaterialType) -> Unit,
    onOpenWoodPicker: () -> Unit,
    onGradeSelected: (MaterialGrade) -> Unit,
    onShapeTypeSelected: (ShapeType) -> Unit,
    onSectionSelected: (SectionProfile?) -> Unit,
    onAddSpan: () -> Unit,
    onRemoveSpan: (UUID) -> Unit,
    onUpdateSpanLength: (UUID, Length) -> Unit,
    unitSystem: UnitSystem,
    activeSpanId: UUID?,
    onSelectSpan: (UUID) -> Unit,
    onEditBracing: (UUID) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // 1. Span Management
        SpanEditor(
            spans = member.spans,
            activeSpanId = activeSpanId,
            onAddSpan = onAddSpan,
            onRemoveSpan = onRemoveSpan,
            onUpdateSpanLength = onUpdateSpanLength,
            onSelectSpan = onSelectSpan,
            onEditBracing = onEditBracing
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

        // 2. Section Settings (Redesigned)
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Material Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1. Material Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableMaterials.forEach { mat ->
                        FilterChip(
                            selected = selectedMaterial == mat,
                            onClick = { onMaterialSelected(mat) },
                            label = { Text(mat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Material Grade Selection
            if (selectedMaterial == MaterialType.WOOD) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. Wood Species & Grade", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        onClick = onOpenWoodPicker
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val woodGrade = selectedGrade as? MaterialGrade.Wood
                                Text(
                                    text = woodGrade?.name ?: "No Grade Selected",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (woodGrade != null) "${woodGrade.species.name.replace("_", " ")} - ${woodGrade.grade.name.replace("_", " ")}" else "Select to configure",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(Icons.Default.Settings, contentDescription = "Edit Wood Grade", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            } else if (availableGrades.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. Material Grade", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableGrades.forEach { grade ->
                            FilterChip(
                                selected = selectedGrade == grade,
                                onClick = { onGradeSelected(grade) },
                                label = { Text(grade.name) },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }

            // Shape Type Selection
            if (availableShapeTypes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3. Shape Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableShapeTypes.forEach { shape ->
                            FilterChip(
                                selected = selectedShapeType == shape,
                                onClick = { onShapeTypeSelected(shape) },
                                label = { Text(shape.name.replace("_", " ").lowercase().capitalize()) },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }

            // Section Selection (Dialog)
            if (availableSections.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("4. Select Profile", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                    SectionPicker(
                        sections = availableSections,
                        selectedSection = selectedSection,
                        onSectionSelected = onSectionSelected,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Orientation Toggle
            OrientationToggle(
                isStrongAxis = isStrongAxis,
                onToggle = { onUpdateOrientation(!isStrongAxis) },
                icon = Icons.Default.LineAxis,
                weight = selectedSection?.getWeightLbFt(490.0)?.toFloat() ?: 0f
            )
        }
    }
}

@Composable
fun OrientationToggle(
    isStrongAxis: Boolean,
    onToggle: () -> Unit,
    icon: ImageVector,
    weight: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Orientation", style = MaterialTheme.typography.labelLarge)
                    Text(
                        if (isStrongAxis) "Strong Axis (Ixx)" else "Weak Axis (Iyy)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Switch(checked = isStrongAxis, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
fun LoadsConfiguration(
    loadCases: List<LoadCase>,
    combinations: List<LoadCombination>,
    selectedCombination: LoadCombination?,
    includeSelfWeight: Boolean,
    onAddLoad: (String, Load) -> Unit,
    onRemoveLoad: (String, UUID) -> Unit,
    onSelectLoad: (String, UUID) -> Unit,
    onCombinationSelected: (LoadCombination?) -> Unit,
    onUpdateIncludeSelfWeight: (Boolean) -> Unit,
    spans: List<SpanGeometry>,
    unitSystem: UnitSystem,
    selectedLoad: Load?
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Self Weight Toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = includeSelfWeight, onCheckedChange = onUpdateIncludeSelfWeight)
            Text("Include Self-Weight", style = MaterialTheme.typography.bodyMedium)
        }

        // Selected Load Case Editor
        loadCases.forEach { case ->
            LoadCaseCard(
                case = case,
                spans = spans,
                onAddLoad = { onAddLoad(case.id, it) },
                onRemoveLoad = { onRemoveLoad(case.id, it) },
                onSelectLoad = { onSelectLoad(case.id, it) },
                unitSystem = unitSystem,
                selectedLoadId = selectedLoad?.id
            )
        }
    }
}

@Composable
fun LoadCaseCard(
    case: LoadCase,
    spans: List<SpanGeometry>,
    onAddLoad: (Load) -> Unit,
    onRemoveLoad: (UUID) -> Unit,
    onSelectLoad: (UUID) -> Unit,
    unitSystem: UnitSystem,
    selectedLoadId: UUID?
) {
    val totalLength = spans.sumOf { it.length.inInches }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            LoadEditor(
                loads = case.loads,
                activeCaseId = case.id,
                memberLength = totalLength,
                onAddLoad = onAddLoad,
                onDeleteLoad = { onRemoveLoad(it.id) },
                onLoadSelected = { it?.let { onSelectLoad(it.id) } },
                selectedLoad = case.loads.find { it.id == selectedLoadId },
                unitSystem = unitSystem,
                spans = spans
            )
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, description: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StatusBadge(label: String, value: String, isCritical: Boolean) {
    Surface(
        color = if (isCritical) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AnalysisSummary(
    results: BeamCalculationResults?,
    member: StructuralMember,
    unitSystem: UnitSystem,
    selectedCombination: LoadCombination?
) {
    val analysis = if (selectedCombination != null) {
        results?.analysisResult?.combinationResults?.get(selectedCombination.name) ?: results?.analysisResult
    } else {
        results?.analysisResult
    }

    if (analysis == null) return

    val momentUnitLabel = UnitFormattingService.getMomentUnitSymbol(unitSystem)
    val shearUnitLabel = UnitFormattingService.getForceUnitSymbol(unitSystem)

    val momentPoints = remember(analysis, unitSystem) {
        analysis.spanResults.flatMap { it.momentDiagram }.map { p ->
            val convertedValue = if (unitSystem == UnitSystem.METRIC) {
                Moment(p.value).inNewtonMeters / 1000.0
            } else {
                Moment(p.value).inLbFt / 1000.0
            }
            p.copy(value = convertedValue)
        }
    }

    val shearPoints = remember(analysis, unitSystem) {
        analysis.spanResults.flatMap { it.shearDiagram }.map { p ->
            val convertedValue = if (unitSystem == UnitSystem.METRIC) {
                Force(p.value).inKiloNewtons
            } else {
                Force(p.value).inKips
            }
            p.copy(value = convertedValue)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Force Envelopes", style = MaterialTheme.typography.titleMedium)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge("Max Moment", "${String.format("%.1f", analysis.maxMoment.inLbIn / 12000.0)} k-ft", false)
            StatusBadge("Max Shear", "${String.format("%.1f", analysis.maxShear.inPoundsForce / 1000.0)} kips", false)
        }

        // Summary Table
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ResultRow("Max Deflection", "${String.format("%.3f", analysis.maxDeflection.inInches)} in")
                ResultRow("Max Axial", "${String.format("%.1f", analysis.maxAxial.inPoundsForce)} lbs")
                ResultRow("Max Torsion", "${String.format("%.1f", analysis.maxTorsion.inLbIn)} lb-in")
            }
        }

        // Diagrams Card
        if (shearPoints.isNotEmpty() || momentPoints.isNotEmpty()) {
            Text("Analysis Diagrams", style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (shearPoints.isNotEmpty()) {
                        AnalysisChart(
                            title = "Shear Force ($shearUnitLabel)",
                            points = shearPoints,
                            unitLabel = shearUnitLabel,
                            lineColor = Color(0xFF00ACC1), // Elegant Teal
                            modifier = Modifier.fillMaxWidth(),
                            invertY = false
                        )
                    }

                    if (shearPoints.isNotEmpty() && momentPoints.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }

                    if (momentPoints.isNotEmpty()) {
                        AnalysisChart(
                            title = "Bending Moment ($momentUnitLabel)",
                            points = momentPoints,
                            unitLabel = momentUnitLabel,
                            lineColor = Color(0xFF8E24AA), // Elegant Purple
                            modifier = Modifier.fillMaxWidth(),
                            invertY = true // Standard convention (tension side down)
                        )
                    }
                }
            }
        }

        // Reaction Summary
        Text("Reactions", style = MaterialTheme.typography.titleSmall)
        analysis.reactions.forEach { reaction ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Node ${reaction.nodeIndex} (${reaction.label})", fontWeight = FontWeight.Bold)
                    Text("${String.format("%.1f", reaction.verticalForce.inPoundsForce)} lbs", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun DesignSummary(
    pointResults: List<PointCapacityResult>,
    detailedResult: StrengthDesignResult?,
    serviceabilityResults: List<ServiceabilityResult>,
    member: StructuralMember,
    unitSystem: UnitSystem
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // 1. Critical Strength Check (Governing)
        if (detailedResult != null) {
            Text("Governing Strength Checks", style = MaterialTheme.typography.titleMedium)

            DesignCard("Bending (Mx)", detailedResult.momentCheck, unitSystem)
            DesignCard("Shear (Vy)", detailedResult.shearCheck, unitSystem)

            if (detailedResult.axialCheck.demand.inPoundsForce != 0.0) {
                DesignCard("Axial (Px)", detailedResult.axialCheck, unitSystem)
            }

            // Interaction Details
            val flexRatio = detailedResult.momentCheck.utilization
            val axialRatio = detailedResult.axialCheck.utilization
            val interactionValue = if (axialRatio >= 0.2) axialRatio + (8.0/9.0) * flexRatio else (axialRatio/2.0) + flexRatio

            InteractionCard(
                label = "H1-1 Interaction",
                equation = if (axialRatio >= 0.2) "Pr/Pc + 8/9(Mr/Mc)" else "Pr/2Pc + Mr/Mc",
                value = interactionValue,
                status = if (interactionValue <= 1.0) InteractionStatus.PASS else InteractionStatus.FAIL
            )

            DesignParameterSummary(detailedResult, unitSystem)
        }

        // 2. Serviceability Checks
        if (serviceabilityResults.isNotEmpty()) {
            Text("Serviceability Checks", style = MaterialTheme.typography.titleMedium)
            serviceabilityResults.forEach { res ->
                ServiceabilityCard(res, unitSystem)
            }
        }
    }
}

@Composable
fun PointCapacityCard(label: String, result: PointCapacityResult, unitSystem: UnitSystem) {
    // Legacy - replaced by DesignSummary/DesignCard
}

@Composable
fun ServiceabilityCard(result: ServiceabilityResult, unitSystem: UnitSystem) {
    val isFail = result.utilization > 1.0

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isFail) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isFail) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(result.criterion.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                StatusBadgeSmall(
                    label = "RATIO",
                    value = String.format("%.2f", result.utilization),
                    isCritical = isFail
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Actual", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("${String.format("%.3f", result.actualDeflection.inInches)} in", style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Allowable", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("${String.format("%.3f", result.allowableDeflection.inInches)} in", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text(result.criterion.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DesignCard(label: String, check: StrengthCheckResult<*>, unitSystem: UnitSystem) {
    val isFail = check.utilization > 1.0
    val demandStr = when (val d = check.demand) {
        is Moment -> "${String.format("%.1f", d.inLbIn / 12000.0)} k-ft"
        is Force -> "${String.format("%.1f", d.inPoundsForce / 1000.0)} kips"
        else -> d.toString()
    }
    val capacityStr = when (val c = check.capacity) {
        is Moment -> "${String.format("%.1f", c.inLbIn / 12000.0)} k-ft"
        is Force -> "${String.format("%.1f", c.inPoundsForce / 1000.0)} kips"
        else -> c.toString()
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    check.governingMode?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }

                CircularProgressIndicator(
                    progress = check.utilization.toFloat().coerceAtMost(1f),
                    modifier = Modifier.size(32.dp),
                    color = getUtilizationColor(check.utilization),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 4.dp
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Demand", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(demandStr, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ratio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(String.format("%.2f", check.utilization), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = getUtilizationColor(check.utilization))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Capacity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(capacityStr, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }

            if (check.traces.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                check.traces.forEach { trace ->
                    EquationTraceItem(trace)
                }
            }
        }
    }
}

@Composable
fun InteractionCard(
    label: String,
    equation: String,
    value: Double,
    status: InteractionStatus,
    trace: DesignEquationTrace? = null
) {
    val isFail = status == InteractionStatus.FAIL

    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isFail) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(2.dp, getUtilizationColor(value))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(equation, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(String.format("%.2f", value), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = getUtilizationColor(value))
                Text(if (isFail) "FAIL" else "PASS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = getUtilizationColor(value))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesignParameterSummary(result: StrengthDesignResult, unitSystem: UnitSystem) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Design Parameters", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            // FlowRow for parameters
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                result.designParameters.forEach { (key, value) ->
                    Column {
                        Text(key, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun EquationTraceItem(trace: DesignEquationTrace) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(trace.symbolicEquation, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(trace.codeReference, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        Text(trace.substitutedEquation, style = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp))
        Text("${trace.result} ${trace.units}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}