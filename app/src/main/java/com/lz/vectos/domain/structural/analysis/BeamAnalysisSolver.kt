package com.lz.vectos.domain.structural.analysis

import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.SpanGeometry
import com.lz.model.structural.SupportCondition
import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.Moment
import com.lz.model.units.inInches
import com.lz.model.units.inLbIn
import com.lz.model.units.inPoundsForce
import com.lz.model.units.inches
import com.lz.vectos.domain.structural.*
import com.lz.vectos.domain.structural.analysis.core.StructuralSolver
import com.lz.vectos.domain.structural.analysis.core.StructuralResult
import com.lz.vectos.domain.structural.analysis.core.StructuralSystem
import com.lz.vectos.domain.structural.analysis.core.DofConfig
import com.lz.vectos.domain.structural.analysis.core.DofType
import com.lz.model.units.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

import com.lz.model.util.UUIDSerializer
import kotlinx.serialization.Serializable


@Serializable
data class SpanAnalysisResult(
    val spanId: @Serializable(with = UUIDSerializer::class) java.util.UUID,
    val maxMoment: Moment,
    val maxShear: Force,
    val maxDeflection: Length,
    val maxAxial: Force = Force(0.0),
    val minMoment: Moment = Moment(0.0),
    val minShear: Force = Force(0.0),
    val minAxial: Force = Force(0.0),
    val extremePoints: SpanExtremePoints,
    val shearDiagram: List<AnalysisPoint>,
    val momentDiagram: List<AnalysisPoint>,
    val deflectionDiagram: List<AnalysisPoint>,
    val shearYDiagram: List<AnalysisPoint> = emptyList(),
    val momentYDiagram: List<AnalysisPoint> = emptyList(),
    val torqueDiagram: List<AnalysisPoint> = emptyList(),
    val stationDemands: List<StationDemand> = emptyList(),
    val utilizationDiagram: List<UtilizationPoint> = emptyList(),
    val governingCombinationName: String? = null
)

/**
 * A single point on a utilization diagram.
 */
@Serializable
data class UtilizationPoint(
    val x: Length,
    val demand: Double,
    val capacity: Double,
    val ratio: Double
)

/**
 * Reaction at a support.
 */
@Serializable
data class ReactionResult(
    val nodeIndex: Int,
    val verticalForce: Force,
    val moment: Moment,
    val axialForce: Force = Force(0.0),
    val lateralForce: Force = Force(0.0),
    val momentY: Moment = Moment(0.0),
    val torque: Moment = Moment(0.0),
    val label: String
)

/**
 * A single point on a diagram.
 */
@Serializable
data class AnalysisPoint(val x: Length, val value: Double) // Value is primitive base unit (lb, lb-in, or in)

/**
 * Comprehensive analysis result for a structural member.
 */
@Serializable
data class BeamAnalysisResult(
    val maxMoment: Moment,
    val maxMomentY: Moment = Moment(0.0),
    val maxShear: Force,
    val maxShearY: Force = Force(0.0),
    val maxTorsion: Moment = Moment(0.0),
    val maxAxial: Force = Force(0.0),
    val maxDeflection: Length,
    val minMoment: Moment = Moment(0.0),
    val minMomentY: Moment = Moment(0.0),
    val minShear: Force = Force(0.0),
    val minShearY: Force = Force(0.0),
    val minTorsion: Moment = Moment(0.0),
    val minAxial: Force = Force(0.0),
    val spanResults: List<SpanAnalysisResult>,
    val reactions: List<ReactionResult> = emptyList(),
    val combinationResults: Map<String, BeamAnalysisResult> = emptyMap(),
    val governingCombinationName: String? = null
)

@Serializable
data class SpanExtremePoints(
    val maxMomentX: Length,
    val minMomentX: Length,
    val maxShearX: Length,
    val minShearX: Length,
    val maxAxialX: Length = Length(0.0),
    val minAxialX: Length = Length(0.0),
    val maxDeflectionX: Length
)

interface SectionPropertyProvider {
    fun getE(x: Double): Double
    fun getI(x: Double): Double
    fun getIy(x: Double): Double
    fun getG(x: Double): Double
    fun getAv(x: Double): Double?
    fun getA(x: Double): Double
    fun getJ(x: Double): Double
}

class ConstantSectionProvider(
    val e: Double,
    val i: Double,
    val iy: Double,
    val g: Double,
    val a: Double,
    val j: Double,
    val av: Double? = null
) : SectionPropertyProvider {
    override fun getE(x: Double): Double = e
    override fun getI(x: Double): Double = i
    override fun getIy(x: Double): Double = iy
    override fun getG(x: Double): Double = g
    override fun getAv(x: Double): Double? = av
    override fun getA(x: Double): Double = a
    override fun getJ(x: Double): Double = j
}

data class SpanContext(
    val span: SpanGeometry,
    val loads: List<Load>,
    val lengthInches: Double,
    val index: Int,
    val startNode: Int,
    val endNode: Int,
    val propertyProvider: SectionPropertyProvider
)

data class SpanForces(
    val vyStart: Double,
    val mzStart: Double,
    val vyEnd: Double,
    val mzEnd: Double,
    val axialStart: Double = 0.0,
    val axialEnd: Double = 0.0,
    val vzStart: Double = 0.0,
    val myStart: Double = 0.0,
    val vzEnd: Double = 0.0,
    val myEnd: Double = 0.0,
    val txStart: Double = 0.0,
    val txEnd: Double = 0.0
)

/**
 * 3D Beam Analysis Solver supporting 6-DOF per node.
 */
object BeamAnalysisSolver {

    fun solve(config: BeamAnalysisConfig): BeamAnalysisResult {
        // 1. Determine Global Stations of Interest (POIs) across ALL loads
        val globalPois = collectGlobalPois(config)

        if (config.combinations.isEmpty()) {
            // Legacy behavior if no combinations provided: Solve all loads as a single case
            return solveInternal(config, config.allLoads, globalPois)
        }

        // 2. Identify all unique categories in the input loads
        val activeCategories = config.allLoads.map { it.category }.distinct()

        // 3. Solve for each category independently (Unfactored/Service Pass)
        val resultsByCategory = activeCategories.associateWith { category ->
            val categoryLoads = config.allLoads.filter { it.category == category }
            solveInternal(config, categoryLoads, globalPois)
        }

        // 4. Resolve Envelopes using LoadCombinationEngine
        // Map category results to StationDemand lists for the engine
        val demandsByCategory = resultsByCategory.mapKeys { it.key }.mapValues { (_, result) ->
            result.spanResults.flatMap { it.stationDemands }
        }

        val envelopeStations = LoadCombinationEngine.resolveEnvelope(demandsByCategory, config.combinations)

        // 5. Build Individual Combination Results
        val combinationResults = config.combinations.associate { combo ->
            val comboDemands = envelopeStations.map { it.allCombinationsResults[combo.name]!! }
            
            // Apply factors to reactions
            val comboReactions = resultsByCategory.entries.fold(mutableMapOf<Int, ReactionResult>()) { acc, entry ->
                val category = entry.key
                val result = entry.value
                val factor = combo.factors[category] ?: 0.0
                
                result.reactions.forEach { rxn ->
                    val existing = acc[rxn.nodeIndex]
                    if (existing == null) {
                        acc[rxn.nodeIndex] = rxn.copy(
                            verticalForce = rxn.verticalForce * factor,
                            moment = rxn.moment * factor,
                            axialForce = rxn.axialForce * factor,
                            lateralForce = rxn.lateralForce * factor,
                            momentY = rxn.momentY * factor,
                            torque = rxn.torque * factor
                        )
                    } else {
                        acc[rxn.nodeIndex] = existing.copy(
                            verticalForce = existing.verticalForce + (rxn.verticalForce * factor),
                            moment = existing.moment + (rxn.moment * factor),
                            axialForce = existing.axialForce + (rxn.axialForce * factor),
                            lateralForce = existing.lateralForce + (rxn.lateralForce * factor),
                            momentY = existing.momentY + (rxn.momentY * factor),
                            torque = existing.torque + (rxn.torque * factor)
                        )
                    }
                }
                acc
            }.values.toList()

            combo.name to buildResultFromDemands(config, comboDemands, combo.name).copy(reactions = comboReactions)
        }

        // 6. Build Final Governing Result
        val governingDemands = envelopeStations.map { it.governingDemand }
        
        // Compute Governing Reactions
        val governingReactions = mutableMapOf<Int, ReactionResult>()
        combinationResults.values.forEach { comboResult ->
            comboResult.reactions.forEach { rxn ->
                val existing = governingReactions[rxn.nodeIndex]
                if (existing == null) {
                    governingReactions[rxn.nodeIndex] = rxn
                } else {
                    // This is a simplification: pick max absolute vertical force for governing
                    // In a full implementation, we might want max/min for every component.
                    if (abs(rxn.verticalForce.inPoundsForce) > abs(existing.verticalForce.inPoundsForce)) {
                        governingReactions[rxn.nodeIndex] = rxn
                    }
                }
            }
        }

        val governingResult = buildResultFromDemands(config, governingDemands, "Governing Envelope")

        return governingResult.copy(
            combinationResults = combinationResults,
            governingCombinationName = "Multiple (See Spans)",
            reactions = governingReactions.values.toList()
        )
    }

    private fun collectGlobalPois(config: BeamAnalysisConfig): List<Double> {
        val pois = mutableSetOf<Double>()
        var currentX = 0.0
        pois.add(0.0)
        config.member.spans.forEach { span ->
            currentX += span.length.inches
            pois.add(currentX)
        }

        config.allLoads.forEach { load ->
            val spanOffset = config.member.spans.takeWhile { it.id != load.spanId }.sumOf { it.length.inches }
            val start = spanOffset + load.locationStart.inches
            val end = spanOffset + load.locationEnd.inches
            
            pois.add(start)
            if (load is Load.PointLoad || load is Load.PointMoment || load is Load.PointTorque) {
                pois.add(start - 1e-5)
                pois.add(start + 1e-5)
            }
            pois.add(end)
        }

        config.braceState.forEach { pois.add(it.x) }

        // Grid
        val totalLength = config.member.spans.sumOf { it.length.inches }
        val gridCount = 100
        if (totalLength > 0) {
            for (i in 0..gridCount) {
                pois.add(i * totalLength / gridCount)
            }
        }

        return pois.filter { it in 0.0..totalLength }.sorted().fold(mutableListOf<Double>()) { acc, x ->
            if (acc.isEmpty() || x - acc.last() > 1e-7) acc.add(x)
            acc
        }
    }

    private fun solveInternal(config: BeamAnalysisConfig, loads: List<Load>, globalStations: List<Double>): BeamAnalysisResult {
        val solver = StructuralSolver()
        val system = StructuralSystem()

        // 1. Build Nodes
        var currentX = 0.0
        val nodeIndices = mutableListOf<Int>()
        nodeIndices.add(system.addNode(0.0, 0.0, 0.0))

        config.member.spans.forEach { span ->
            currentX += span.length.inches
            nodeIndices.add(system.addNode(currentX, 0.0, 0.0))
        }

        // 2. Build Elements & Apply Contexts
        val spanContexts = mutableListOf<SpanContext>()
        config.member.spans.forEachIndexed { idx, span ->
            val startNode = nodeIndices[idx]
            val endNode = nodeIndices[idx + 1]

            val e = config.modulusOfElasticityPsi
            val i = config.momentOfInertiaIn4
            val iy = config.momentOfInertiaYIn4 ?: (i * 0.1)
            val a = config.areaIn2 ?: 1.0
            val g = config.shearModulusPsi ?: (e / (2 * (1 + 0.3)))
            val j = config.torsionalConstantIn4 ?: (i + iy)

            val propProvider = ConstantSectionProvider(e, i, iy, g, a, j)

            system.addElement(
                startNode,
                endNode,
                e,
                i,
                iy,
                g,
                a,
                j,
                dofConfig = DofConfig.SIX_DOF
            )

            spanContexts.add(SpanContext(
                span,
                loads.filter { it.spanId == span.id },
                span.length.inches,
                idx,
                startNode,
                endNode,
                propProvider
            ))
        }

        // 3. Apply Boundary Conditions
        config.member.spans.forEachIndexed { idx, span ->
            if (idx == 0) applySupport(system, nodeIndices[0], span.startSupport)
            applySupport(system, nodeIndices[idx + 1], span.endSupport)
        }

        // 4. Apply Loads
        spanContexts.forEach { ctx ->
            ctx.loads.forEach { load ->
                applyLoadToSystem(system, ctx, load)
            }
        }

        // 5. Solve
        val structuralResult = solver.solve(system)

        // 6. Post-Process Spans
        var globalXOffset = 0.0
        
        val spanResults = spanContexts.map { ctx ->
            val spanStations = globalStations.filter { it >= globalXOffset - 1e-7 && it <= globalXOffset + ctx.lengthInches + 1e-7 }
                .map { it - globalXOffset }
            
            val result = analyzeSpan(ctx, structuralResult, globalXOffset, config.braceState, spanStations, ctx.lengthInches / config.totalLoadDeflectionLimitRatio)
            globalXOffset += ctx.lengthInches
            result
        }

        val reactions = nodeIndices.mapIndexed { idx, nodeIdx ->
            val rxn = structuralResult.reactions[nodeIdx] ?: emptyMap()
            ReactionResult(
                nodeIndex = idx,
                verticalForce = Force(rxn[DofType.UY] ?: 0.0),
                moment = Moment(rxn[DofType.RZ] ?: 0.0),
                axialForce = Force(rxn[DofType.UX] ?: 0.0),
                lateralForce = Force(rxn[DofType.UZ] ?: 0.0),
                momentY = Moment(rxn[DofType.RY] ?: 0.0),
                torque = Moment(rxn[DofType.RX] ?: 0.0),
                label = if (idx < config.member.spans.size) config.member.spans[idx].startSupport.name else config.member.spans.last().endSupport.name
            )
        }

        return createSummaryResult(spanResults, reactions)
    }

    private fun buildResultFromDemands(
        config: BeamAnalysisConfig,
        demands: List<StationDemand>,
        combinationName: String
    ): BeamAnalysisResult {
        val totalLength = config.member.spans.sumOf { it.length.inches }
        
        // 1. Enrich with Bracing Logic (Lb, Cb)
        val enrichedDemands = BracingLogic.calculateDiscreteLb(
            demands,
            config.braceState,
            totalLength,
            member = config.member
        )

        // 2. Map back to Span Results
        val spanResults = config.member.spans.map { span ->
            val spanDemands = enrichedDemands.filter { it.spanId == span.id }
            
            // Calculate Utilization for this combination across all 6-DOF limit states
            val utilizationPoints = config.sectionProfile?.let { profile ->
                val material = config.material ?: return@let emptyList() // Require material for full checks
                
                val calculator: CapacityCalculator? = when (material) {
                    is MaterialGrade.Steel -> com.lz.vectos.domain.structural.aisc.AiscSteelCapacityCalculator(profile, material)
                    is MaterialGrade.Wood -> com.lz.vectos.domain.structural.nds.NdsWoodCapacityCalculator(profile, material)
                    else -> null
                }
                
                if (calculator == null) return@let emptyList()

                CapacityEngine.evaluate(
                    spanDemands,
                    profile,
                    config.designMethodology
                ) { demand -> calculator.evaluate(demand) }.map { cap ->
                    UtilizationPoint(
                        x = cap.demand.x,
                        demand = cap.utilizationRatio * cap.designCapacity, // Governing demand
                        capacity = cap.designCapacity,
                        ratio = cap.utilizationRatio
                    )
                }
            } ?: emptyList()

            val shearPoints = spanDemands.map { AnalysisPoint(it.x, it.shear.inPoundsForce) }
            val momentPoints = spanDemands.map { AnalysisPoint(it.x, it.moment.inLbIn) }
            val shearYPoints = spanDemands.map { AnalysisPoint(it.x, it.shearY.inPoundsForce) }
            val momentYPoints = spanDemands.map { AnalysisPoint(it.x, it.momentY.inLbIn) }
            val axialPoints = spanDemands.map { AnalysisPoint(it.x, it.axial.inPoundsForce) }
            val torquePoints = spanDemands.map { AnalysisPoint(it.x, it.torque.inLbIn) }
            val deflectionPoints = spanDemands.map { AnalysisPoint(it.x, it.deflection.inInches) }

            val maxMomentPoint = momentPoints.maxByOrNull { it.value } ?: AnalysisPoint(0.0.inches, 0.0)
            val minMomentPoint = momentPoints.minByOrNull { it.value } ?: AnalysisPoint(0.0.inches, 0.0)
            val maxShearPoint = shearPoints.maxByOrNull { it.value } ?: AnalysisPoint(0.0.inches, 0.0)
            val minShearPoint = shearPoints.minByOrNull { it.value } ?: AnalysisPoint(0.0.inches, 0.0)
            val maxAxialPoint = axialPoints.maxByOrNull { it.value } ?: AnalysisPoint(0.0.inches, 0.0)
            val maxDeflectionPoint = deflectionPoints.maxByOrNull { abs(it.value) } ?: AnalysisPoint(0.0.inches, 0.0)

            SpanAnalysisResult(
                spanId = span.id,
                maxMoment = Moment(maxMomentPoint.value),
                maxShear = Force(maxShearPoint.value),
                maxDeflection = Length(abs(maxDeflectionPoint.value)),
                maxAxial = Force(maxAxialPoint.value),
                minMoment = Moment(minMomentPoint.value),
                minShear = Force(minShearPoint.value),
                minAxial = Force(axialPoints.minOfOrNull { it.value } ?: 0.0),
                extremePoints = SpanExtremePoints(
                    maxMomentX = maxMomentPoint.x,
                    minMomentX = minMomentPoint.x,
                    maxShearX = maxShearPoint.x,
                    minShearX = minShearPoint.x,
                    maxAxialX = maxAxialPoint.x,
                    minAxialX = 0.0.inches,
                    maxDeflectionX = maxDeflectionPoint.x
                ),
                shearDiagram = shearPoints,
                momentDiagram = momentPoints,
                deflectionDiagram = deflectionPoints, 
                shearYDiagram = shearYPoints,
                momentYDiagram = momentYPoints,
                torqueDiagram = torquePoints,
                stationDemands = spanDemands,
                utilizationDiagram = utilizationPoints,
                governingCombinationName = combinationName
            )
        }

        return createSummaryResult(spanResults, emptyList()) // Reactions handled separately
    }

    private fun createSummaryResult(spanResults: List<SpanAnalysisResult>, reactions: List<ReactionResult>): BeamAnalysisResult {
        val allMoments = spanResults.flatMap { it.momentDiagram.map { p -> p.value } }
        val allShears = spanResults.flatMap { it.shearDiagram.map { p -> p.value } }
        val allAxials = spanResults.flatMap { res -> res.stationDemands.map { d -> d.axial.inPoundsForce } }

        return BeamAnalysisResult(
            maxMoment = Moment(allMoments.maxByOrNull { it } ?: 0.0),
            maxMomentY = Moment(spanResults.maxOfOrNull {
                it.momentYDiagram.maxOfOrNull { p -> p.value } ?: 0.0
            } ?: 0.0),
            maxShear = Force(allShears.maxByOrNull { it } ?: 0.0),
            maxShearY = Force(spanResults.maxOfOrNull {
                it.shearYDiagram.maxOfOrNull { p -> p.value } ?: 0.0
            } ?: 0.0),
            maxTorsion = Moment(spanResults.maxOfOrNull {
                it.torqueDiagram.maxOfOrNull { p -> p.value } ?: 0.0
            } ?: 0.0),
            maxAxial = Force(allAxials.maxByOrNull { it } ?: 0.0),
            maxDeflection = Length(spanResults.maxOfOrNull { it.maxDeflection.inInches } ?: 0.0),
            minMoment = Moment(allMoments.minByOrNull { it } ?: 0.0),
            minMomentY = Moment(spanResults.minOfOrNull {
                it.momentYDiagram.minOfOrNull { p -> p.value } ?: 0.0
            } ?: 0.0),
            minShear = Force(allShears.minByOrNull { it } ?: 0.0),
            minShearY = Force(spanResults.minOfOrNull {
                it.shearYDiagram.minOfOrNull { p -> p.value } ?: 0.0
            } ?: 0.0),
            minTorsion = Moment(spanResults.minOfOrNull {
                it.torqueDiagram.minOfOrNull { p -> p.value } ?: 0.0
            } ?: 0.0),
            minAxial = Force(allAxials.minByOrNull { it } ?: 0.0),
            spanResults = spanResults,
            reactions = reactions
        )
    }

    private fun applySupport(system: StructuralSystem, nodeIdx: Int, support: SupportCondition) {
        when (support) {
            SupportCondition.PINNED -> {
                system.fixNode(nodeIdx, DofType.UX)
                system.fixNode(nodeIdx, DofType.UY)
                system.fixNode(nodeIdx, DofType.UZ)
                system.fixNode(nodeIdx, DofType.RX)
            }
            SupportCondition.FIXED -> {
                system.fixNode(nodeIdx, DofType.UX)
                system.fixNode(nodeIdx, DofType.UY)
                system.fixNode(nodeIdx, DofType.UZ)
                system.fixNode(nodeIdx, DofType.RX)
                system.fixNode(nodeIdx, DofType.RY)
                system.fixNode(nodeIdx, DofType.RZ)
            }
            SupportCondition.ROLLER -> {
                system.fixNode(nodeIdx, DofType.UY)
                system.fixNode(nodeIdx, DofType.RX)
                system.fixNode(nodeIdx, DofType.UZ)
            }
            SupportCondition.FREE -> { /* No-op */ }
            SupportCondition.CUSTOM -> {
                // If CUSTOM is used without specific DOF overrides, default to pinned
                system.fixNode(nodeIdx, DofType.UX)
                system.fixNode(nodeIdx, DofType.UY)
                system.fixNode(nodeIdx, DofType.UZ)
                system.fixNode(nodeIdx, DofType.RX)
            }
        }
    }

    private fun applyLoadToSystem(system: StructuralSystem, ctx: SpanContext, load: Load) {
        val l = ctx.lengthInches
        if (l <= 0.0) return

        when (load) {
            is Load.PointLoad -> {
                val a = load.locationStart.inches
                val p = load.value.inPoundsForce

                when (load.direction) {
                    LoadDirection.VERTICAL_DOWN, LoadDirection.VERTICAL_UP -> {
                        val sign = if (load.direction == LoadDirection.VERTICAL_DOWN) -1.0 else 1.0
                        val (fems, reactions) = computePointLoadFems(p * sign, a, l)
                        println("PointLoad vertical: element=${ctx.index} a=$a l=$l p=$p sign=$sign reactions=${reactions} fems=${fems}")
                        // Equivalent Nodal Loads (NOT reactions)
                        system.addElementEquivalentForce(ctx.index, 1, reactions.vStart)
                        system.addElementEquivalentForce(ctx.index, 5, fems.mStart)
                        system.addElementEquivalentForce(ctx.index, 7, reactions.vEnd)
                        system.addElementEquivalentForce(ctx.index, 11, fems.mEnd)
                    }
                    LoadDirection.LATERAL_LEFT, LoadDirection.LATERAL_RIGHT -> {
                        val sign = if (load.direction == LoadDirection.LATERAL_LEFT) -1.0 else 1.0
                        val (fems, reactions) = computePointLoadFems(p * sign, a, l)
                        system.addElementEquivalentForce(ctx.index, 2, reactions.vStart)
                        system.addElementEquivalentForce(ctx.index, 4, fems.mStart)
                        system.addElementEquivalentForce(ctx.index, 8, reactions.vEnd)
                        system.addElementEquivalentForce(ctx.index, 10, fems.mEnd)
                    }
                    LoadDirection.AXIAL_COMPRESSION, LoadDirection.AXIAL_TENSION -> {
                        val sign = if (load.direction == LoadDirection.AXIAL_COMPRESSION) -1.0 else 1.0
                        val axialStart = (p * sign * (l - a)) / l
                        val axialEnd = (p * sign * a) / l
                        system.addElementEquivalentForce(ctx.index, 0, axialStart)
                        system.addElementEquivalentForce(ctx.index, 6, axialEnd)
                    }
                    else -> {}
                }
            }
            is Load.UniformDistributedLoad -> {
                val w = load.value.pli
                val a = load.locationStart.inches
                val b = load.locationEnd.inches
                val length = b - a
                if (length <= 0.0) return

                when (load.direction) {
                    LoadDirection.VERTICAL_DOWN, LoadDirection.VERTICAL_UP -> {
                        val sign = if (load.direction == LoadDirection.VERTICAL_UP) 1.0 else -1.0
                        val (v1, m1, v2, m2) = computePartialUdlFems(w * sign, a, b, l)
                        system.addElementEquivalentForce(ctx.index, 1, v1)
                        system.addElementEquivalentForce(ctx.index, 5, m1)
                        system.addElementEquivalentForce(ctx.index, 7, v2)
                        system.addElementEquivalentForce(ctx.index, 11, m2)
                    }
                    LoadDirection.LATERAL_LEFT, LoadDirection.LATERAL_RIGHT -> {
                        val sign = if (load.direction == LoadDirection.LATERAL_LEFT) -1.0 else 1.0
                        val (v1, m1, v2, m2) = computePartialUdlFems(w * sign, a, b, l)
                        system.addElementEquivalentForce(ctx.index, 2, v1)
                        system.addElementEquivalentForce(ctx.index, 4, m1)
                        system.addElementEquivalentForce(ctx.index, 8, v2)
                        system.addElementEquivalentForce(ctx.index, 10, m2)
                    }
                    LoadDirection.AXIAL_COMPRESSION, LoadDirection.AXIAL_TENSION -> {
                        val sign = if (load.direction == LoadDirection.AXIAL_COMPRESSION) -1.0 else 1.0
                        val total = w * sign * length
                        val mid = (a + b) / 2.0
                        val f1 = total * (1.0 - mid / l)
                        val f2 = total * (mid / l)
                        system.addElementEquivalentForce(ctx.index, 0, f1)
                        system.addElementEquivalentForce(ctx.index, 6, f2)
                    }
                    LoadDirection.TORSION_CLOCKWISE, LoadDirection.TORSION_COUNTER_CLOCKWISE -> {
                        val sign = if (load.direction == LoadDirection.TORSION_CLOCKWISE) 1.0 else -1.0
                        val total = w * sign * length
                        val mid = (a + b) / 2.0
                        val t1 = total * (1.0 - mid / l)
                        val t2 = total * (mid / l)
                        system.addElementEquivalentForce(ctx.index, 3, t1)
                        system.addElementEquivalentForce(ctx.index, 9, t2)
                    }
                    else -> {}
                }
            }
            is Load.TrapezoidalLoad -> {
                val w1 = load.valueStart.pli
                val w2 = load.valueEnd.pli
                val a = load.locationStart.inches
                val b = load.locationEnd.inches

                when (load.direction) {
                    LoadDirection.VERTICAL_DOWN, LoadDirection.VERTICAL_UP -> {
                        val sign = if (load.direction == LoadDirection.VERTICAL_UP) 1.0 else -1.0
                        val (v1, m1, v2, m2) = computeTrapezoidalFems(w1 * sign, w2 * sign, a, b, l)
                        system.addElementEquivalentForce(ctx.index, 1, v1)
                        system.addElementEquivalentForce(ctx.index, 5, m1)
                        system.addElementEquivalentForce(ctx.index, 7, v2)
                        system.addElementEquivalentForce(ctx.index, 11, m2)
                    }
                    LoadDirection.LATERAL_LEFT, LoadDirection.LATERAL_RIGHT -> {
                        val sign = if (load.direction == LoadDirection.LATERAL_LEFT) -1.0 else 1.0
                        val (v1, m1, v2, m2) = computeTrapezoidalFems(w1 * sign, w2 * sign, a, b, l)
                        system.addElementEquivalentForce(ctx.index, 2, v1)
                        system.addElementEquivalentForce(ctx.index, 4, m1)
                        system.addElementEquivalentForce(ctx.index, 8, v2)
                        system.addElementEquivalentForce(ctx.index, 10, m2)
                    }
                    LoadDirection.AXIAL_COMPRESSION, LoadDirection.AXIAL_TENSION -> {
                        val sign = if (load.direction == LoadDirection.AXIAL_COMPRESSION) -1.0 else 1.0
                        val length = b - a
                        val total = (w1 + w2) / 2.0 * length * sign
                        val centroid = a + (length / 3.0) * (w1 + 2 * w2) / (w1 + w2)
                        val f1 = total * (1.0 - centroid / l)
                        val f2 = total * (centroid / l)
                        system.addElementEquivalentForce(ctx.index, 0, f1)
                        system.addElementEquivalentForce(ctx.index, 6, f2)
                    }
                    LoadDirection.TORSION_CLOCKWISE, LoadDirection.TORSION_COUNTER_CLOCKWISE -> {
                        val sign = if (load.direction == LoadDirection.TORSION_CLOCKWISE) 1.0 else -1.0
                        val length = b - a
                        val total = (w1 + w2) / 2.0 * length * sign
                        val centroid = a + (length / 3.0) * (w1 + 2 * w2) / (w1 + w2)
                        val t1 = total * (1.0 - centroid / l)
                        val t2 = total * (centroid / l)
                        system.addElementEquivalentForce(ctx.index, 3, t1)
                        system.addElementEquivalentForce(ctx.index, 9, t2)
                    }
                    else -> {}
                }
            }
            is Load.PointMoment -> {
                val m = load.value.inLbIn
                val a = load.locationStart.inches
                val sign = if (load.direction == LoadDirection.MOMENT_CLOCKWISE) -1.0 else 1.0
                val b = l - a
                val mStart = m * sign * b * (b - 2*a) / (l*l)
                val mEnd = m * sign * a * (a - 2*b) / (l*l)
                val vStart = -6 * m * sign * a * b / (l*l*l)
                val vEnd = 6 * m * sign * a * b / (l*l*l)
                system.addElementEquivalentForce(ctx.index, 1, -vStart)
                system.addElementEquivalentForce(ctx.index, 5, -mStart)
                system.addElementEquivalentForce(ctx.index, 7, -vEnd)
                system.addElementEquivalentForce(ctx.index, 11, -mEnd)
            }
            is Load.PointTorque -> {
                val t = load.value.inLbIn
                val a = load.locationStart.inches
                val sign = if (load.direction == LoadDirection.TORSION_CLOCKWISE) 1.0 else -1.0
                val tStart = t * sign * (l - a) / l
                val tEnd = t * sign * a / l
                system.addElementEquivalentForce(ctx.index, 3, tStart)
                system.addElementEquivalentForce(ctx.index, 9, tEnd)
            }
            is Load.AxialLoad -> {
                val pForce = load.value.inPoundsForce
                val sign = if (load.direction == LoadDirection.AXIAL_COMPRESSION) -1.0 else 1.0
                system.addElementEquivalentForce(ctx.index, 0, -pForce * sign)
                system.addElementEquivalentForce(ctx.index, 6, pForce * sign)
            }
            is Load.TributaryLoad -> {
                applyLoadToSystem(system, ctx, load.toTrapezoidal())
            }
        }
    }

    private fun computePartialUdlFems(w: Double, a: Double, b: Double, l: Double): Fems4 {
        fun fv1(x: Double) = l.pow(3)*x - l*x.pow(3) + 0.5*x.pow(4)
        fun fm1(x: Double) = 0.5*l.pow(2)*x.pow(2) - (2.0/3.0)*l*x.pow(3) + 0.25*x.pow(4)
        fun fv2(x: Double) = l*x.pow(3) - 0.5*x.pow(4)
        fun fm2(x: Double) = (1.0/3.0)*l*x.pow(3) - 0.25*x.pow(4)

        val v1 = (w / l.pow(3)) * (fv1(b) - fv1(a))
        val m1 = (w / l.pow(2)) * (fm1(b) - fm1(a))
        val v2 = (w / l.pow(3)) * (fv2(b) - fv2(a))
        val m2 = (-w / l.pow(2)) * (fm2(b) - fm2(a))

        return Fems4(v1, m1, v2, m2)
    }

    private fun computeTrapezoidalFems(w1: Double, w2: Double, a: Double, b: Double, l: Double): Fems4 {
        val mid = (a + b) / 2.0
        val halfLen = (b - a) / 2.0

        val nodes = doubleArrayOf(-sqrt(0.6), 0.0, sqrt(0.6))
        val weights = doubleArrayOf(5.0/9.0, 8.0/9.0, 5.0/9.0)

        var v1 = 0.0; var m1 = 0.0; var v2 = 0.0; var m2 = 0.0

        for (i in 0 until 3) {
            val x = mid + nodes[i] * halfLen
            val w = w1 + (w2 - w1) * (x - a) / (b - a)

            v1 += w * weights[i] * (l-x).pow(2) * (2*x + l) / l.pow(3)
            m1 += w * weights[i] * x * (l-x).pow(2) / l.pow(2)
            v2 += w * weights[i] * x.pow(2) * (3*l - 2*x) / l.pow(3)
            m2 -= w * weights[i] * x.pow(2) * (l-x) / l.pow(2)
        }

        return Fems4(v1 * halfLen, m1 * halfLen, v2 * halfLen, m2 * halfLen)
    }

    data class Fems4(val v1: Double, val m1: Double, val v2: Double, val m2: Double)

    private fun computePointLoadFems(p: Double, a: Double, l: Double): Pair<Fems, Reactions> {
        val b = l - a
        val mStart = (p * a * b * b) / (l * l)
        val mEnd = -(p * a * a * b) / (l * l)
        val vStart = (p * b * b * (3 * a + b)) / (l * l * l)
        val vEnd = (p * a * a * (a + 3 * b)) / (l * l * l)
        return Fems(mStart, mEnd) to Reactions(vStart, vEnd)
    }

    data class Fems(val mStart: Double, val mEnd: Double)
    data class Reactions(val vStart: Double, val vEnd: Double)

    private fun analyzeSpan(
        ctx: SpanContext,
        result: StructuralResult,
        globalXOffset: Double,
        braceState: List<NormalizedBraceState>,
        sortedStations: List<Double>,
        allowableDeflectionInches: Double
    ): SpanAnalysisResult {
        val elForces = result.elementEndForces[ctx.index] ?: List(12) { 0.0 }

        val v1 = result.displacements[ctx.startNode to DofType.UY] ?: 0.0
        val v2 = result.displacements[ctx.endNode to DofType.UY] ?: 0.0
        val theta1 = result.displacements[ctx.startNode to DofType.RZ] ?: 0.0
        val theta2 = result.displacements[ctx.endNode to DofType.RZ] ?: 0.0

        val forces = SpanForces(
            vyStart = -elForces[1],
            mzStart = -elForces[5],
            vyEnd = elForces[7],
            mzEnd = elForces[11],
            axialStart = elForces[0],
            axialEnd = -elForces[6],
            vzStart = -elForces[2],
            myStart = -elForces[4],
            vzEnd = elForces[8],
            myEnd = elForces[10],
            txStart = elForces[3],
            txEnd = -elForces[9]
        )

        // Stations are already computed, sorted, and deduplicated in collectGlobalPois.
        // We use the passed sortedStations parameter directly to maintain exact point load offsets.

        val shearPoints = mutableListOf<AnalysisPoint>()
        val momentPoints = mutableListOf<AnalysisPoint>()
        val shearYPoints = mutableListOf<AnalysisPoint>()
        val momentYPoints = mutableListOf<AnalysisPoint>()
        val axialPoints = mutableListOf<AnalysisPoint>()
        val torquePoints = mutableListOf<AnalysisPoint>()
        val deflectionPoints = mutableListOf<AnalysisPoint>()

        fun getHomogeneousDeflection(x: Double): Double {
            val l = ctx.lengthInches
            if (l <= 0.0) return v1
            val xi = x / l
            val h1 = 1 - 3*xi.pow(2) + 2*xi.pow(3)
            val h2 = xi - 2*xi.pow(2) + xi.pow(3)
            val h3 = 3*xi.pow(2) - 2*xi.pow(3)
            val h4 = -xi.pow(2) + xi.pow(3)
            return h1*v1 + h2*l*theta1 + h3*v2 + h4*l*theta2
        }

        val stationDemands = mutableListOf<StationDemand>()

        sortedStations.forEach { xLocal ->
            val xGlobal = globalXOffset + xLocal
            val internal = computeInternalForces(ctx, forces, xLocal)

            shearPoints.add(AnalysisPoint(xGlobal.inches, internal.vy))
            momentPoints.add(AnalysisPoint(xGlobal.inches, internal.mz))
            shearYPoints.add(AnalysisPoint(xGlobal.inches, internal.vz))
            momentYPoints.add(AnalysisPoint(xGlobal.inches, internal.my))
            axialPoints.add(AnalysisPoint(xGlobal.inches, internal.fx))
            torquePoints.add(AnalysisPoint(xGlobal.inches, internal.tx))

            val vp = computeParticularDeflection(ctx, xLocal)
            val vTotal = getHomogeneousDeflection(xLocal) + vp
            deflectionPoints.add(AnalysisPoint(xGlobal.inches, vTotal))

            stationDemands.add(
                StationDemand(
                    x = xGlobal.inches,
                    moment = Moment(internal.mz),
                    shear = Force(internal.vy),
                    axial = Force(internal.fx),
                    momentY = Moment(internal.my),
                    shearY = Force(internal.vz),
                    torque = Moment(internal.tx),
                    deflection = Length(vTotal),
                    allowableDeflection = Length(allowableDeflectionInches),
                    cb = 1.0,
                    lbTop = 0.0.inches,
                    lbBottom = 0.0.inches,
                    spanId = ctx.span.id
                )
            )
        }

        val maxMomentPoint = momentPoints.maxByOrNull { it.value } ?: AnalysisPoint(0.0.inches, 0.0)
        val minMomentPoint = momentPoints.minByOrNull { it.value } ?: AnalysisPoint(0.0.inches, 0.0)
        val maxShearPoint = shearPoints.maxByOrNull { it.value } ?: AnalysisPoint(0.0.inches, 0.0)
        val minShearPoint = shearPoints.minByOrNull { it.value } ?: AnalysisPoint(0.0.inches, 0.0)
        val maxAxialPoint = axialPoints.maxByOrNull { it.value } ?: AnalysisPoint(0.0.inches, 0.0)
        val minAxialPoint = axialPoints.minByOrNull { it.value } ?: AnalysisPoint(0.0.inches, 0.0)
        val maxDeflectionPoint = deflectionPoints.maxByOrNull { abs(it.value) } ?: AnalysisPoint(0.0.inches, 0.0)

        return SpanAnalysisResult(
            spanId = ctx.span.id,
            maxMoment = Moment(maxMomentPoint.value),
            maxShear = Force(maxShearPoint.value),
            maxDeflection = Length(abs(maxDeflectionPoint.value)),
            maxAxial = Force(maxAxialPoint.value),
            minMoment = Moment(minMomentPoint.value),
            minShear = Force(minShearPoint.value),
            minAxial = Force(minAxialPoint.value),
            extremePoints = SpanExtremePoints(
                maxMomentX = maxMomentPoint.x,
                minMomentX = minMomentPoint.x,
                maxShearX = maxShearPoint.x,
                minShearX = minShearPoint.x,
                maxAxialX = maxAxialPoint.x,
                minAxialX = minAxialPoint.x,
                maxDeflectionX = maxDeflectionPoint.x
            ),
            shearDiagram = shearPoints,
            momentDiagram = momentPoints,
            deflectionDiagram = deflectionPoints,
            shearYDiagram = shearYPoints,
            momentYDiagram = momentYPoints,
            torqueDiagram = torquePoints,
            stationDemands = stationDemands,
            utilizationDiagram = emptyList()
        )
    }

    private fun computeParticularDeflection(ctx: SpanContext, x: Double): Double {
        val l = ctx.lengthInches
        val ei = ctx.propertyProvider.getE(x) * ctx.propertyProvider.getI(x)
        if (ei <= 0.0) return 0.0

        var vp = 0.0
        ctx.loads.forEach { load ->
            vp += when (load) {
                is Load.PointLoad -> {
                    if (load.direction == LoadDirection.VERTICAL_DOWN || load.direction == LoadDirection.VERTICAL_UP) {
                        val p = load.value.inPoundsForce * (if (load.direction == LoadDirection.VERTICAL_DOWN) -1.0 else 1.0)
                        val a = load.locationStart.inches
                        val b = l - a
                        if (x <= a) {
                            (p * b * b * x * x) / (6 * l.pow(3) * ei) * (3 * a * l - x * (3 * a + b))
                        } else {
                            val xRev = l - x
                            (p * a * a * xRev * xRev) / (6 * l.pow(3) * ei) * (3 * b * l - xRev * (3 * b + a))
                        }
                    } else 0.0
                }
                is Load.UniformDistributedLoad -> {
                    if (load.direction == LoadDirection.VERTICAL_DOWN || load.direction == LoadDirection.VERTICAL_UP) {
                        val w = load.value.pli * (if (load.direction == LoadDirection.VERTICAL_DOWN) -1.0 else 1.0)
                        val a = load.locationStart.inches
                        val b = load.locationEnd.inches
                        if (a <= 0.01 && b >= l - 0.01) {
                            (w * x * x * (l - x).pow(2)) / (24 * ei)
                        } else {
                            val steps = 10
                            val dx = (b - a) / steps
                            var sum = 0.0
                            for (j in 0 until steps) {
                                val loadPos = a + (j + 0.5) * dx
                                val loadMag = w * dx
                                val bLoad = l - loadPos
                                sum += if (x <= loadPos) {
                                    (loadMag * bLoad * bLoad * x * x) / (6 * l.pow(3) * ei) * (3 * loadPos * l - x * (3 * loadPos + bLoad))
                                } else {
                                    val xRev = l - x
                                    val aLoad = loadPos
                                    (loadMag * aLoad * aLoad * xRev * xRev) / (6 * l.pow(3) * ei) * (3 * bLoad * l - xRev * (3 * bLoad + aLoad))
                                }
                            }
                            sum
                        }
                    } else 0.0
                }
                else -> 0.0
            }
        }
        return vp
    }

    data class InternalForces(
        val fx: Double,
        val vy: Double,
        val vz: Double,
        val tx: Double,
        val my: Double,
        val mz: Double
    )

    private fun computeInternalForces(ctx: SpanContext, endForces: SpanForces, x: Double): InternalForces {
        var fx = endForces.axialStart
        var vy = endForces.vyStart
        var vz = endForces.vzStart
        var tx = endForces.txStart
        var my = endForces.myStart - (endForces.vzStart * x)
        var mz = endForces.mzStart + (endForces.vyStart * x)

        ctx.loads.forEach { load ->
            val start = load.locationStart.inches
            val end = load.locationEnd.inches
            
            if (x >= start) {
                when (load) {
                    is Load.PointLoad -> {
                        val pVal = load.value.inPoundsForce
                        val arm = x - start
                        when (load.direction) {
                            LoadDirection.VERTICAL_DOWN -> { vy -= pVal; mz -= pVal * arm }
                            LoadDirection.VERTICAL_UP -> { vy += pVal; mz += pVal * arm }
                            LoadDirection.LATERAL_LEFT -> { vz -= pVal; my += pVal * arm }
                            LoadDirection.LATERAL_RIGHT -> { vz += pVal; my -= pVal * arm }
                            LoadDirection.AXIAL_COMPRESSION -> { fx -= pVal }
                            LoadDirection.AXIAL_TENSION -> { fx += pVal }
                            LoadDirection.TORSION_CLOCKWISE -> { tx -= pVal }
                            LoadDirection.TORSION_COUNTER_CLOCKWISE -> { tx += pVal }
                            else -> {}
                        }
                    }
                    is Load.UniformDistributedLoad -> {
                        val w = load.value.pli
                        val loadX = minOf(x, end) - start
                        if (loadX > 0) {
                            val total = w * loadX
                            val arm = x - (start + loadX / 2.0)
                            when (load.direction) {
                                LoadDirection.VERTICAL_DOWN -> { vy -= total; mz -= total * arm }
                                LoadDirection.VERTICAL_UP -> { vy += total; mz += total * arm }
                                LoadDirection.LATERAL_LEFT -> { vz -= total; my += total * arm }
                                LoadDirection.LATERAL_RIGHT -> { vz += total; my -= total * arm }
                                LoadDirection.AXIAL_COMPRESSION -> { fx -= total }
                                LoadDirection.AXIAL_TENSION -> { fx += total }
                                LoadDirection.TORSION_CLOCKWISE -> { tx += total }
                                LoadDirection.TORSION_COUNTER_CLOCKWISE -> { tx -= total }
                                else -> {}
                            }
                        }
                    }
                    is Load.TrapezoidalLoad -> {
                        val w1 = load.valueStart.pli
                        val w2 = load.valueEnd.pli
                        val loadX = minOf(x, end) - start
                        if (loadX > 0) {
                            val length = end - start
                            val wEndAtX = w1 + (w2 - w1) * (loadX / length)
                            val total = (w1 + wEndAtX) / 2.0 * loadX
                            val arm = x - (start + (loadX / 3.0) * (w1 + 2 * wEndAtX) / (w1 + wEndAtX))
                            when (load.direction) {
                                LoadDirection.VERTICAL_DOWN -> { vy -= total; mz -= total * arm }
                                LoadDirection.VERTICAL_UP -> { vy += total; mz += total * arm }
                                LoadDirection.LATERAL_LEFT -> { vz -= total; my += total * arm }
                                LoadDirection.LATERAL_RIGHT -> { vz += total; my -= total * arm }
                                LoadDirection.AXIAL_COMPRESSION -> { fx -= total }
                                LoadDirection.AXIAL_TENSION -> { fx += total }
                                LoadDirection.TORSION_CLOCKWISE -> { tx += total }
                                LoadDirection.TORSION_COUNTER_CLOCKWISE -> { tx -= total }
                                else -> {}
                            }
                        }
                    }
                    is Load.PointMoment -> {
                        val mVal = load.value.inLbIn
                        val sign = if (load.direction == LoadDirection.MOMENT_CLOCKWISE) -1.0 else 1.0
                        mz += mVal * sign
                    }
                    is Load.PointTorque -> {
                        val tVal = load.value.inLbIn
                        val sign = if (load.direction == LoadDirection.TORSION_CLOCKWISE) 1.0 else -1.0
                        tx += tVal * sign
                    }
                    is Load.AxialLoad -> {
                        val pVal = load.value.inPoundsForce
                        when (load.direction) {
                            LoadDirection.AXIAL_COMPRESSION -> { fx -= pVal }
                            LoadDirection.AXIAL_TENSION -> { fx += pVal }
                            else -> {}
                        }
                    }
                    else -> {}
                }
            }
        }

        return InternalForces(fx, vy, vz, tx, my, mz)
    }
}
