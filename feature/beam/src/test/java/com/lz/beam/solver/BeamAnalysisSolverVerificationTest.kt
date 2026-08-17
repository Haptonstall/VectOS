package com.lz.beam.solver

import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.loads.CombinationType
import com.lz.model.structural.BracingInput
import com.lz.model.structural.BracingMode
import com.lz.model.structural.BracingResolver
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.DiscreteBracePoint
import com.lz.model.structural.Flange
import com.lz.model.structural.Load
import com.lz.model.structural.LoadCase
import com.lz.model.structural.LoadDirection
import com.lz.model.structural.NodeBoundaryCondition
import com.lz.model.structural.SpanGeometry
import com.lz.model.structural.StructuralMember
import com.lz.model.structural.StructuralNode
import com.lz.model.units.*
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.abs
import java.util.UUID

class BeamAnalysisSolverVerificationTest {

    @Test
    fun `point load produces correct simply supported moment diagram`() {
        val member = StructuralMember.createSimple(Length(120.0))
        val spanId = member.spans.first().id

        val pointLoad = Load.PointLoad(
            value = Force(1000.0),
            spanId = spanId,
            locationStart = Length(40.0),
            category = LoadCategory.DEAD
        )

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(
                LoadCase(
                    "D",
                    "Dead",
                    listOf(pointLoad)
                )
            ),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )

        val result = BeamAnalysisSolver.solve(config)

        val spanResult = result.spanResults[0]

        val momentDiagram = spanResult.momentDiagram

        fun momentAt(x: Double): Double {
            return momentDiagram
                .minByOrNull { kotlin.math.abs(it.x.inches - x) }!!
                .value
        }

        // For a 1,000 lb point load at 40 in on a 120 in simply supported span:
        //
        // R_A = P(L-a)/L = 1000(120-40)/120 = 666.667 lb
        // R_B = Pa/L     = 1000(40)/120     = 333.333 lb
        //
        // Maximum moment occurs directly under the point load:
        //
        // M_max = R_A * 40
        //       = 666.667 * 40
        //       = 26,666.667 lb-in
        //
        // Solver convention currently reports this as negative.

        assertEquals(
            "Moment at left support should be zero",
            0.0,
            momentAt(0.0),
            1.0
        )

        assertEquals(
            "Moment under point load should be -26,666.67 lb-in",
            -26666.6667,
            momentAt(40.0),
            1.0
        )

        assertEquals(
            "Moment at right support should be zero",
            0.0,
            momentAt(120.0),
            1.0
        )
    }

    @Test
    fun `point load causes correct moment slope change at load location`() {
        val member = StructuralMember.createSimple(Length(120.0))
        val spanId = member.spans.first().id

        val pointLoad = Load.PointLoad(
            value = Force(1000.0),
            spanId = spanId,
            locationStart = Length(40.0),
            category = LoadCategory.DEAD
        )

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(LoadCase("D", "Dead", listOf(pointLoad))),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )

        val result = BeamAnalysisSolver.solve(config)
        val spanResult = result.spanResults[0]
        val moments = spanResult.momentDiagram

        fun nearestPoint(x: Double) =
            moments.minByOrNull { kotlin.math.abs(it.x.inches - x) }!!

        val p20 = nearestPoint(20.0)
        val p30 = nearestPoint(30.0)
        val leftSlope = (p30.value - p20.value) / (p30.x.inches - p20.x.inches)

        val p50 = nearestPoint(50.0)
        val p60 = nearestPoint(60.0)
        val rightSlope = (p60.value - p50.value) / (p60.x.inches - p50.x.inches)

        assertEquals(
            "Moment slope to left of point load should equal -reaction (solver sign convention)",
            -666.6667,
            leftSlope,
            1.0
        )

        assertEquals(
            "Moment slope to right of point load should equal load minus reaction (solver sign convention)",
            333.3333,
            rightSlope,
            1.0
        )
    }

    @Test
    fun `point load produces correct support reactions`() {
        val member = StructuralMember.createSimple(Length(120.0))
        val spanId = member.spans.first().id

        val pointLoad = Load.PointLoad(
            value = Force(1000.0),
            spanId = spanId,
            locationStart = Length(40.0),
            category = LoadCategory.DEAD
        )

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(
                LoadCase(
                    "D",
                    "Dead",
                    listOf(pointLoad)
                )
            ),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )

        val result = BeamAnalysisSolver.solve(config)

        assertEquals(
            "Left reaction should be 666.667 lb",
            666.6667,
            result.reactions.first { it.nodeIndex == 0 }.verticalForce.inPoundsForce,
            0.1
        )

        assertEquals(
            "Right reaction should be 333.333 lb",
            333.3333,
            result.reactions.first { it.nodeIndex == member.nodes.lastIndex }.verticalForce.inPoundsForce,
            0.1
        )
    }

    @Test
    fun `point load satisfies vertical force equilibrium`() {
        val member = StructuralMember.createSimple(Length(120.0))
        val spanId = member.spans.first().id

        val pointLoad = Load.PointLoad(
            value = Force(1000.0),
            spanId = spanId,
            locationStart = Length(40.0),
            category = LoadCategory.DEAD
        )

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(
                LoadCase(
                    "D",
                    "Dead",
                    listOf(pointLoad)
                )
            ),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )

        val result = BeamAnalysisSolver.solve(config)

        val totalReaction =
            result.reactions.first { it.nodeIndex == 0 }.verticalForce.inPoundsForce +
                    result.reactions.first { it.nodeIndex == member.nodes.lastIndex }.verticalForce.inPoundsForce

        assertEquals(
            "Support reactions must equal total applied vertical load",
            1000.0,
            totalReaction,
            0.1
        )
    }

    @Test
    fun `point load satisfies moment equilibrium about left support`() {
        val member = StructuralMember.createSimple(Length(120.0))
        val spanId = member.spans.first().id

        val pointLoad = Load.PointLoad(
            value = Force(1000.0),
            spanId = spanId,
            locationStart = Length(40.0),
            category = LoadCategory.DEAD
        )

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(
                LoadCase(
                    "D",
                    "Dead",
                    listOf(pointLoad)
                )
            ),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )

        val result = BeamAnalysisSolver.solve(config)

        val rightReactionMoment =
            result.reactions.first { it.nodeIndex == member.nodes.lastIndex }.verticalForce.inPoundsForce * 120.0

        val appliedLoadMoment =
            1000.0 * 40.0

        assertEquals(
            "Moments about left support must be in equilibrium",
            appliedLoadMoment,
            rightReactionMoment,
            0.1
        )
    }

    @Test
    fun `combined point loads produce correct governing moment`() {
        val member = StructuralMember.createSimple(Length(120.0))
        val spanId = member.spans.first().id

        val deadLoad = Load.PointLoad(
            value = Force(1000.0),
            spanId = spanId,
            locationStart = Length(40.0),
            category = LoadCategory.DEAD
        )

        val liveLoad = Load.PointLoad(
            value = Force(2000.0),
            spanId = spanId,
            locationStart = Length(80.0),
            category = LoadCategory.LIVE
        )

        val combination = LoadCombination(
            id = "LC1",
            name = "1.2D + 1.6L",
            methodology = DesignMethodology.LRFD,
            equationText = "1.2D + 1.6L",
            factors = mapOf(
                LoadCategory.DEAD to 1.2,
                LoadCategory.LIVE to 1.6
            ),
            codeReference = "ASCE 7-16",
            type = CombinationType.STRENGTH
        )

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(
                LoadCase("D", "Dead", listOf(deadLoad)),
                LoadCase("L", "Live", listOf(liveLoad))
            ),
            combinations = listOf(combination),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )

        val result = BeamAnalysisSolver.solve(config)

        val combinationResult =
            result.combinationResults["1.2D + 1.6L"]!!

        val spanResult =
            combinationResult.spanResults[0]

        val momentDiagram = spanResult.momentDiagram

        fun momentAt(x: Double): Double {
            return momentDiagram
                .minByOrNull { kotlin.math.abs(it.x.inches - x) }!!
                .value
        }

        assertEquals(
            "Moment at 40 in",
            -74666.6667,
            momentAt(40.0),
            2.0
        )

        assertEquals(
            "Moment at 80 in",
            -101333.3333,
            momentAt(80.0),
            2.0
        )

        assertEquals(
            "Governing moment should occur at 80 in",
            80.0,
            spanResult.extremePoints.maxMomentX.inches,
            0.5
        )
    }

    @Test
    fun `combined load categories with point loads reflect correct peaks`() {
        val member = StructuralMember.createSimple(Length(120.0)) // 10 ft
        val spanId = member.spans.first().id
        
        val deadLoad = Load.PointLoad(
            value = Force(1000.0),
            spanId = spanId,
            locationStart = Length(40.0), // at 40 inches
            category = LoadCategory.DEAD
        )
        
        val liveLoad = Load.PointLoad(
            value = Force(2000.0),
            spanId = spanId,
            locationStart = Length(80.0), // at 80 inches
            category = LoadCategory.LIVE
        )
        
        val combinations = listOf(
            LoadCombination(
                id = "LC1",
                name = "1.2D + 1.6L",
                methodology = DesignMethodology.LRFD,
                equationText = "1.2D + 1.6L",
                factors = mapOf(LoadCategory.DEAD to 1.2, LoadCategory.LIVE to 1.6),
                codeReference = "ASCE 7-16",
                type = CombinationType.STRENGTH
            )
        )
        
        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(
                LoadCase("D", "Dead", listOf(deadLoad)),
                LoadCase("L", "Live", listOf(liveLoad))
            ),
            combinations = combinations,
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )
        
        val result = BeamAnalysisSolver.solve(config)
        
        val comboResult = result.combinationResults["1.2D + 1.6L"]!!
        val spanResult = comboResult.spanResults[0]
        
        // Expected Shear:
        // P_dead = 1000, R_dead_start = 1000 * 80 / 120 = 666.67, R_dead_end = 333.33
        // P_live = 2000, R_live_start = 2000 * 40 / 120 = 666.67, R_live_end = 1333.33
        
        // Factored Reactions:
        // R_start = 1.2 * 666.67 + 1.6 * 666.67 = 800 + 1066.67 = 1866.67
        // R_end = 1.2 * 333.33 + 1.6 * 1333.33 = 400 + 2133.33 = 2533.33
        
        // At 40": V changes by 1.2 * 1000 = 1200
        // At 80": V changes by 1.6 * 2000 = 3200
        
        val shearPoints = spanResult.shearDiagram

        // Debug output for failing assertions
        println("Shear points (x -> value):")
        shearPoints.forEach { println("${it.x.inches} -> ${it.value}") }

        // Check for peaks (steps) at 40 and 80
        val peakAt40Before = shearPoints.find { abs(it.x.inInches - (40.0 - 1e-5)) < 1e-6 }
        val peakAt40After = shearPoints.find { abs(it.x.inInches - (40.0 + 1e-5)) < 1e-6 }
        val peakAt80Before = shearPoints.find { abs(it.x.inInches - (80.0 - 1e-5)) < 1e-6 }
        val peakAt80After = shearPoints.find { abs(it.x.inInches - (80.0 + 1e-5)) < 1e-6 }

        Assert.assertNotNull("Peak at 40 (before) missing", peakAt40Before)
        Assert.assertNotNull("Peak at 40 (after) missing", peakAt40After)
        Assert.assertNotNull("Peak at 80 (before) missing", peakAt80Before)
        Assert.assertNotNull("Peak at 80 (after) missing", peakAt80After)
        
        val delta40 = peakAt40After!!.value - peakAt40Before!!.value
        val delta80 = peakAt80After!!.value - peakAt80Before!!.value

        Assert.assertEquals(1200.0, delta40, 1.0)
        Assert.assertEquals(3200.0, delta80, 1.0)
    }

    @Test
    fun `axial compression follows negative sign convention`() {
        val member = StructuralMember.createSimple(Length(100.0))
        val spanId = member.spans.first().id
        
        val axialLoad = Load.AxialLoad(
            value = Force(1000.0),
            spanId = spanId,
            direction = LoadDirection.AXIAL_COMPRESSION
        )
        
        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(LoadCase("D", "Dead", listOf(axialLoad))),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4,
            crossSectionalArea = 10.0.in2
        )
        
        val result = BeamAnalysisSolver.solve(config)
        val spanResult = result.spanResults[0]

        println("Axial station demands:")
        result.spanResults[0].stationDemands.forEach { println("x=${it.x.inches} axial=${it.axial.inPoundsForce}") }
        
        // Internal axial force should be -1000
        Assert.assertTrue(
            "Axial compression should be negative: ${spanResult.maxAxial.inPoundsForce}",
            spanResult.stationDemands.all { it.axial.inPoundsForce <= 0.0 })
        Assert.assertEquals(-1000.0, spanResult.minAxial.inPoundsForce, 0.1)
    }

    @Test
    fun `two span continuous beam with point loads matches known reactions`() {
        val leftNode = StructuralNode(boundaryCondition = NodeBoundaryCondition.pinned())
        val midNode  = StructuralNode(boundaryCondition = NodeBoundaryCondition.roller())
        val rightNode = StructuralNode(boundaryCondition = NodeBoundaryCondition.roller())

        val span1 = SpanGeometry(length = Length(120.0), startNodeId = leftNode.id, endNodeId = midNode.id)
        val span2 = SpanGeometry(length = Length(120.0), startNodeId = midNode.id, endNodeId = rightNode.id)

        val member = StructuralMember(
            nodes = listOf(leftNode, midNode, rightNode),
            spans = listOf(span1, span2)
        )

        val load1 = Load.PointLoad(value = Force(1000.0), spanId = span1.id, locationStart = Length(60.0), category = LoadCategory.DEAD)
        val load2 = Load.PointLoad(value = Force(1000.0), spanId = span2.id, locationStart = Length(60.0), category = LoadCategory.DEAD)

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(LoadCase("D", "Dead", listOf(load1, load2))),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )

        val result = BeamAnalysisSolver.solve(config)

        val rLeft  = result.reactions.first { it.nodeIndex == 0 }.verticalForce.inPoundsForce
        val rMid   = result.reactions.first { it.nodeIndex == 1 }.verticalForce.inPoundsForce
        val rRight = result.reactions.first { it.nodeIndex == 2 }.verticalForce.inPoundsForce

        // Center point load on each span, equal spans: R_end = 5P/16, R_mid = 11P/8
        assertEquals("Left reaction should be 5P/16 = 312.5 lb", 312.5, rLeft, 1.0)
        assertEquals("Middle reaction should be 11P/8 = 1375.0 lb", 1375.0, rMid, 1.0)
        assertEquals("Right reaction should be 5P/16 = 312.5 lb", 312.5, rRight, 1.0)

        assertEquals("Sum of reactions must equal total applied load", 2000.0, rLeft + rMid + rRight, 1.0)
    }

    @Test
    fun `discrete brace at midspan splits unbraced length correctly`() {
        val member = StructuralMember.createSimple(Length(240.0))
        val span = member.spans.first()

        val bracingInput = BracingInput.Steel(
            topMode = BracingMode.DISCRETE,
            bottomMode = BracingMode.UNBRACED,
            discreteTable = listOf(
                DiscreteBracePoint(x = Length(120.0), isTopBraced = true, isBottomBraced = false)
            )
        )

        val segments = BracingResolver.resolveSegments(bracingInput, span.length)

        println("Unbraced segments:")
        segments.forEach { println("  ${it.startX.inches} to ${it.endX.inches}: lbTop=${it.lbTop.inches}, lbBottom=${it.lbBottom.inches}") }

        // Top flange: braced at 0, 120, 240 -> two 120" segments
        assertEquals("Should produce 2 segments (split at brace point)", 2, segments.size)
        assertEquals("First segment lbTop should be 120 in (braced-to-braced)", 120.0, segments[0].lbTop.inches, 0.1)
        assertEquals("Second segment lbTop should be 120 in (braced-to-braced)", 120.0, segments[1].lbTop.inches, 0.1)

        // Bottom flange: fully UNBRACED mode -> lbBottom = full span length regardless of split
        assertEquals("First segment lbBottom should equal full span (unbraced)", 240.0, segments[0].lbBottom.inches, 0.1)
        assertEquals("Second segment lbBottom should equal full span (unbraced)", 240.0, segments[1].lbBottom.inches, 0.1)
    }

    @Test
    fun `solver enriches station demands with correct Lb from span unbraced segments`() {
        val baseMember = StructuralMember.createSimple(Length(240.0))
        val baseSpan = baseMember.spans.first()

        val bracingInput = BracingInput.Steel(
            topMode = BracingMode.DISCRETE,
            bottomMode = BracingMode.UNBRACED,
            discreteTable = listOf(
                DiscreteBracePoint(x = Length(120.0), isTopBraced = true, isBottomBraced = false)
            )
        )

        val segments = BracingResolver.resolveSegments(bracingInput, baseSpan.length)
        val bracedSpan = baseSpan.copy(unbracedSegments = segments)
        val member = baseMember.copy(spans = listOf(bracedSpan))

        val pointLoad = Load.PointLoad(
            value = Force(1000.0),
            spanId = bracedSpan.id,
            locationStart = Length(120.0),
            category = LoadCategory.DEAD
        )

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(LoadCase("D", "Dead", listOf(pointLoad))),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )

        val result = BeamAnalysisSolver.solve(config)
        val demands = result.spanResults[0].stationDemands

        fun demandAt(x: Double) = demands.minByOrNull { kotlin.math.abs(it.x.inches - x) }!!

        val d60 = demandAt(60.0)
        assertEquals("lbTop in first segment should be 120 in", 120.0, d60.lbTop.inches, 0.5)
        assertEquals("lbBottom in first segment should be full span (unbraced)", 240.0, d60.lbBottom.inches, 0.5)
        assertEquals(
            "Sagging beam under gravity load: compression flange must be TOP",
            Flange.TOP,
            d60.compressionFlange
        )

        val d180 = demandAt(180.0)
        assertEquals("lbTop in second segment should be 120 in", 120.0, d180.lbTop.inches, 0.5)
        assertEquals("lbBottom in second segment should be full span (unbraced)", 240.0, d180.lbBottom.inches, 0.5)
        assertEquals(
            "Sagging beam under gravity load: compression flange must be TOP",
            Flange.TOP,
            d180.compressionFlange
        )
    }

    @Test
    fun `cantilever moment diagram returns to zero at free tip`() {
        val length = Length(100.0)
        val fixedNode = StructuralNode(boundaryCondition = NodeBoundaryCondition.fixed())
        val freeNode  = StructuralNode(boundaryCondition = NodeBoundaryCondition.free())

        val span = SpanGeometry(length = length, startNodeId = fixedNode.id, endNodeId = freeNode.id)
        val member = StructuralMember(nodes = listOf(fixedNode, freeNode), spans = listOf(span))

        val tipLoad = Load.PointLoad(
            value = Force(1000.0),
            spanId = span.id,
            locationStart = Length(100.0),
            category = LoadCategory.DEAD
        )

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(LoadCase("D", "Dead", listOf(tipLoad))),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )

        val result = BeamAnalysisSolver.solve(config)
        val moments = result.spanResults[0].momentDiagram

        fun momentAt(x: Double) = moments.minByOrNull { kotlin.math.abs(it.x.inches - x) }!!.value

        assertEquals(
            "Moment at the free tip must be zero",
            0.0,
            momentAt(100.0),
            1.0
        )

        assertEquals(
            "Moment at fixed end should be +P*L = +100,000 (solver's negated-sagging convention)",
            100000.0,
            momentAt(0.0),
            1.0
        )

        assertEquals(
            "Moment at midspan should be +50,000 (halfway between +100,000 and 0)",
            50000.0,
            momentAt(50.0),
            50.0
        )
    }

    @Test
    fun `full span uniform load produces correct reactions shear and moment`() {
        val member = StructuralMember.createSimple(Length(120.0))
        val spanId = member.spans.first().id

        val uniformLoad = Load.UniformDistributedLoad(
            value = 100.0.lbPerIn,
            spanId = spanId,
            locationStart = Length(0.0),
            locationEnd = Length(120.0),
            category = LoadCategory.DEAD,
            direction = LoadDirection.VERTICAL_DOWN
        )

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(
                LoadCase(
                    "D",
                    "Dead",
                    listOf(uniformLoad)
                )
            ),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )

        val result = BeamAnalysisSolver.solve(config)
        val spanResult = result.spanResults[0]

        // ------------------------------------------------------------
        // Reactions
        //
        // Total load = wL = 100 * 120 = 12,000 lb
        // Symmetric loading gives:
        //
        // RA = RB = 6,000 lb
        // ------------------------------------------------------------

        assertEquals(
            "Left reaction should be 6000 lb",
            6000.0,
            result.reactions
                .first { it.nodeIndex == 0 }
                .verticalForce
                .inPoundsForce,
            0.1
        )

        assertEquals(
            "Right reaction should be 6000 lb",
            6000.0,
            result.reactions
                .first { it.nodeIndex == member.nodes.lastIndex }
                .verticalForce
                .inPoundsForce,
            0.1
        )

        // ------------------------------------------------------------
        // Vertical force equilibrium
        // ------------------------------------------------------------

        val totalReaction =
            result.reactions
                .first { it.nodeIndex == 0 }
                .verticalForce
                .inPoundsForce +
                    result.reactions
                        .first { it.nodeIndex == member.nodes.lastIndex }
                        .verticalForce
                        .inPoundsForce

        assertEquals(
            "Support reactions must equal total applied load",
            12000.0,
            totalReaction,
            0.1
        )

        // ------------------------------------------------------------
        // Moment diagram
        // ------------------------------------------------------------

        val momentDiagram = spanResult.momentDiagram

        fun momentAt(x: Double): Double {
            return momentDiagram
                .minByOrNull { kotlin.math.abs(it.x.inches - x) }!!
                .value
        }

        assertEquals(
            "Moment at left support should be zero",
            0.0,
            momentAt(0.0),
            1.0
        )

        assertEquals(
            "Moment at midspan should be -180,000 lb-in",
            -180000.0,
            momentAt(60.0),
            2.0
        )

        assertEquals(
            "Moment at right support should be zero",
            0.0,
            momentAt(120.0),
            1.0
        )

        // ------------------------------------------------------------
        // Governing moment location
        // ------------------------------------------------------------

        assertEquals(
            "Maximum moment should occur at midspan",
            60.0,
            spanResult.extremePoints.maxMomentX.inches,
            0.5
        )

        // ------------------------------------------------------------
        // Shear diagram
        // ------------------------------------------------------------

        val shearDiagram = spanResult.shearDiagram

        fun shearAt(x: Double): Double {
            return shearDiagram
                .minByOrNull { kotlin.math.abs(it.x.inches - x) }!!
                .value
        }

        assertEquals(
            "Shear at left support should be -6000 lb",
            -6000.0,
            shearAt(0.0),
            1.0
        )

        assertEquals(
            "Shear at midspan should be zero",
            0.0,
            shearAt(60.0),
            1.0
        )

        assertEquals(
            "Shear at right support should be +6000 lb",
            6000.0,
            shearAt(120.0),
            1.0
        )
    }

    @Test
    fun `simply supported beam with midspan point load produces correct reactions and moment`() {
        val member = StructuralMember.createSimple(Length(120.0))
        val spanId = member.spans.first().id

        val pointLoad = Load.PointLoad(
            id = UUID.randomUUID(),
            value = Force(6000.0),
            spanId = spanId,
            locationStart = Length(60.0),
            category = LoadCategory.DEAD
        )

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(
                LoadCase(
                    "D",
                    "Dead",
                    listOf(pointLoad)
                )
            ),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )

        val result = BeamAnalysisSolver.solve(config)

        val spanResult = result.spanResults[0]

        // ------------------------------------------------------------
        // Expected analytical solution
        //
        // L = 120 in
        // P = 6000 lb at midspan
        //
        // RA = RB = P/2 = 3000 lb
        // Mmax = P*L/4 = 180,000 lb-in
        // ------------------------------------------------------------

        // Verify the recovered moment field at the supports.
        val startMoment = spanResult.stationDemands
            .minByOrNull { it.x.inches }
            ?.moment
            ?: error("No start station demand found")

        val endMoment = spanResult.stationDemands
            .maxByOrNull { it.x.inches }
            ?.moment
            ?: error("No end station demand found")

        assertEquals(
            "Moment at left support should be zero",
            0.0,
            startMoment.lbIn,
            1.0
        )

        assertEquals(
            "Moment at right support should be zero",
            0.0,
            endMoment.lbIn,
            1.0
        )

        // Find the station at midspan.
        val midspan = spanResult.stationDemands.minByOrNull {
            kotlin.math.abs(it.x.inches - 60.0)
        } ?: error("No midspan station found")

        assertEquals(
            "Moment at midspan should be -180,000 lb-in",
            -180000.0,
            midspan.moment.lbIn,
            1.0
        )
    }

    @Test
    fun `discrete brace point redundant with span boundary does not create degenerate segment`() {
        val spanLength = Length(240.0)

        val bracingInput = BracingInput.Steel(
            topMode = BracingMode.DISCRETE,
            bottomMode = BracingMode.UNBRACED,
            discreteTable = listOf(
                // Redundant — these coincide with the boundaries BracingResolver already seeds.
                DiscreteBracePoint(x = Length(0.0), isTopBraced = true, isBottomBraced = false),
                DiscreteBracePoint(x = Length(240.0), isTopBraced = true, isBottomBraced = false),
                // The one genuine interior brace.
                DiscreteBracePoint(x = Length(120.0), isTopBraced = true, isBottomBraced = false)
            )
        )

        val segments = BracingResolver.resolveSegments(bracingInput, spanLength)

        println("Segments with redundant boundary braces:")
        segments.forEach { println("  ${it.startX.inches} to ${it.endX.inches}: lbTop=${it.lbTop.inches}, lbBottom=${it.lbBottom.inches}") }

        assertEquals("Should produce exactly 2 segments, not 4 (redundant boundary points must not split)", 2, segments.size)
        assertEquals("No degenerate zero-length segment at the start", 0.0, segments[0].startX.inches, 0.001)
        assertEquals("First segment should run the full 120 in to the interior brace", 120.0, segments[0].endX.inches, 0.001)
        assertEquals("Second segment should run the remaining 120 in to the end", 240.0, segments[1].endX.inches, 0.001)

        assertEquals("First segment lbTop should be 120 in", 120.0, segments[0].lbTop.inches, 0.1)
        assertEquals("Second segment lbTop should be 120 in", 120.0, segments[1].lbTop.inches, 0.1)
    }

    @Test
    fun `partial span uniform load produces correct reactions shear and moment`() {
        val member = StructuralMember.createSimple(Length(120.0))
        val spanId = member.spans.first().id

        val uniformLoad = Load.UniformDistributedLoad(
            value = 100.0.lbPerIn,
            spanId = spanId,
            locationStart = Length(30.0),
            locationEnd = Length(90.0),
            category = LoadCategory.DEAD,
            direction = LoadDirection.VERTICAL_DOWN
        )

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(
                LoadCase(
                    "D",
                    "Dead",
                    listOf(uniformLoad)
                )
            ),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )

        val result = BeamAnalysisSolver.solve(config)
        val spanResult = result.spanResults[0]

        // ------------------------------------------------------------
        // Load geometry
        //
        // Loaded region = 30" to 90"
        // Loaded length = 60"
        // w = 100 lb/in
        //
        // Total load = 100 * 60 = 6,000 lb
        //
        // Resultant acts at the center of the loaded region:
        // x = 60"
        //
        // Because the loading is symmetric:
        // RA = RB = 3,000 lb
        // ------------------------------------------------------------

        assertEquals(
            "Left reaction should be 3000 lb",
            3000.0,
            result.reactions
                .first { it.nodeIndex == 0 }
                .verticalForce
                .inPoundsForce,
            0.1
        )

        assertEquals(
            "Right reaction should be 3000 lb",
            3000.0,
            result.reactions
                .first { it.nodeIndex == member.nodes.lastIndex }
                .verticalForce
                .inPoundsForce,
            0.1
        )

        // ------------------------------------------------------------
        // Vertical force equilibrium
        // ------------------------------------------------------------

        val totalReaction =
            result.reactions
                .first { it.nodeIndex == 0 }
                .verticalForce
                .inPoundsForce +
                    result.reactions
                        .first { it.nodeIndex == member.nodes.lastIndex }
                        .verticalForce
                        .inPoundsForce

        assertEquals(
            "Support reactions must equal total applied load",
            6000.0,
            totalReaction,
            0.1
        )

        // ------------------------------------------------------------
        // Diagram helpers
        // ------------------------------------------------------------

        val momentDiagram = spanResult.momentDiagram
        val shearDiagram = spanResult.shearDiagram

        fun momentAt(x: Double): Double {
            return momentDiagram
                .minByOrNull { kotlin.math.abs(it.x.inches - x) }!!
                .value
        }

        fun shearAt(x: Double): Double {
            return shearDiagram
                .minByOrNull { kotlin.math.abs(it.x.inches - x) }!!
                .value
        }

        // ------------------------------------------------------------
        // Shear diagram
        //
        // Current solver convention:
        //
        // x < 30:
        //     V = -3000
        //
        // 30 <= x <= 90:
        //     V = -3000 + 100(x - 30)
        //
        // x > 90:
        //     V = +3000
        // ------------------------------------------------------------

        assertEquals(
            "Shear before loaded region should be -3000 lb",
            -3000.0,
            shearAt(0.0),
            1.0
        )

        assertEquals(
            "Shear immediately before load begins should be -3000 lb",
            -3000.0,
            shearAt(30.0),
            1.0
        )

        assertEquals(
            "Shear at center of loaded region should be zero",
            0.0,
            shearAt(60.0),
            1.0
        )

        assertEquals(
            "Shear at end of loaded region should be +3000 lb",
            3000.0,
            shearAt(90.0),
            1.0
        )

        assertEquals(
            "Shear after loaded region should be +3000 lb",
            3000.0,
            shearAt(120.0),
            1.0
        )

        // ------------------------------------------------------------
        // Moment diagram
        //
        // Outside loaded region:
        //
        // 0 <= x <= 30:
        //     M = -3000x
        //
        // Therefore:
        //     M(0)  = 0
        //     M(30) = -90,000 lb-in
        //
        // Within loaded region:
        //
        // M(x) = -3000x + 50(x - 30)^2
        //
        // At x = 60:
        //
        // M = -3000(60) + 50(30)^2
        //   = -180000 + 45000
        //   = -135000 lb-in
        //
        // At x = 90:
        //
        // M = -90,000 lb-in
        //
        // Finally:
        //
        // 90 <= x <= 120:
        //     M = -90000 + 3000(x - 90)
        //
        // Therefore M(120) = 0.
        // ------------------------------------------------------------

        assertEquals(
            "Moment at left support should be zero",
            0.0,
            momentAt(0.0),
            1.0
        )

        assertEquals(
            "Moment at beginning of loaded region should be -90000 lb-in",
            -90000.0,
            momentAt(30.0),
            2.0
        )

        assertEquals(
            "Moment at midspan should be -135000 lb-in",
            -135000.0,
            momentAt(60.0),
            2.0
        )

        assertEquals(
            "Moment at end of loaded region should be -90000 lb-in",
            -90000.0,
            momentAt(90.0),
            2.0
        )

        assertEquals(
            "Moment at right support should be zero",
            0.0,
            momentAt(120.0),
            1.0
        )

        // ------------------------------------------------------------
        // Governing moment
        //
        // Shear changes sign at x = 60", so the maximum absolute
        // moment occurs at the center of the loaded region.
        // ------------------------------------------------------------

        assertEquals(
            "Maximum moment should occur at midspan",
            60.0,
            spanResult.extremePoints.maxMomentX.inches,
            0.5
        )

        assertEquals(
            "Maximum moment should be -135000 lb-in",
            -135000.0,
            spanResult.maxMoment.inLbIn,
            2.0
        )
    }

    @Test
    fun `point moment produces correct moment jump without shear change`() {
        val member = StructuralMember.createSimple(Length(120.0))
        val spanId = member.spans.first().id

        val pointMoment = Load.PointMoment(
            value = 100000.0.lbIn,
            spanId = spanId,
            locationStart = Length(60.0),
            category = LoadCategory.DEAD,
            direction = LoadDirection.MOMENT_CLOCKWISE
        )

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(
                LoadCase(
                    "D",
                    "Dead",
                    listOf(pointMoment)
                )
            ),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )

        val result = BeamAnalysisSolver.solve(config)
        val spanResult = result.spanResults[0]

        val momentDiagram = spanResult.momentDiagram
        val shearDiagram = spanResult.shearDiagram

        fun momentAt(x: Double): Double {
            return momentDiagram
                .minByOrNull { kotlin.math.abs(it.x.inches - x) }!!
                .value
        }

        fun shearAt(x: Double): Double {
            return shearDiagram
                .minByOrNull { kotlin.math.abs(it.x.inches - x) }!!
                .value
        }

        // ------------------------------------------------------------
        // A concentrated moment produces no net vertical force.
        //
        // Therefore the support reactions should be equal and
        // opposite, with magnitude:
        //
        // R = M / L = 100,000 / 120 = 833.333 lb
        // ------------------------------------------------------------

        val leftReaction =
            result.reactions
                .first { it.nodeIndex == 0 }
                .verticalForce
                .inPoundsForce

        val rightReaction =
            result.reactions
                .first { it.nodeIndex == member.nodes.lastIndex }
                .verticalForce
                .inPoundsForce

        assertEquals(
            "Left reaction should be +833.33 lb",
            833.333,
            leftReaction,
            0.1
        )

        assertEquals(
            "Right reaction should be -833.33 lb",
            -833.333,
            rightReaction,
            0.1
        )

        // ------------------------------------------------------------
        // Vertical equilibrium
        // ------------------------------------------------------------

        assertEquals(
            "Net vertical reaction should be zero",
            0.0,
            leftReaction + rightReaction,
            0.1
        )

        // ------------------------------------------------------------
        // Shear
        //
        // A point moment does NOT create a shear discontinuity.
        // Shear should therefore be constant throughout the span.
        // ------------------------------------------------------------

        val shearLeft = shearAt(0.0)
        val shearBeforeMoment = shearAt(60.0 - 1e-5)
        val shearAfterMoment = shearAt(60.0 + 1e-5)
        val shearRight = shearAt(120.0)

        assertEquals(
            "Shear should be constant before point moment",
            shearLeft,
            shearBeforeMoment,
            1.0
        )

        assertEquals(
            "Point moment should not change shear",
            shearBeforeMoment,
            shearAfterMoment,
            1.0
        )

        assertEquals(
            "Shear should remain constant after point moment",
            shearAfterMoment,
            shearRight,
            1.0
        )

        // ------------------------------------------------------------
        // Moment
        //
        // A concentrated moment creates a discontinuity in the
        // moment diagram equal to the applied moment.
        // ------------------------------------------------------------

        val momentBefore = momentAt(60.0 - 1e-5)
        val momentAfter = momentAt(60.0 + 1e-5)

        assertEquals(
            "Moment immediately before clockwise point moment should be -50,000 lb-in",
            -50000.0,
            momentBefore,
            2.0
        )

        assertEquals(
            "Moment immediately after clockwise point moment should be +50,000 lb-in",
            50000.0,
            momentAfter,
            2.0
        )

        assertEquals(
            "Clockwise point moment should produce a +100,000 lb-in moment jump",
            100000.0,
            momentAfter - momentBefore,
            2.0
        )

        // ------------------------------------------------------------
        // Support moments
        //
        // Simply supported ends must remain zero.
        // ------------------------------------------------------------

        assertEquals(
            "Moment at left support should be zero",
            0.0,
            momentAt(0.0),
            1.0
        )

        assertEquals(
            "Moment at right support should be zero",
            0.0,
            momentAt(120.0),
            1.0
        )
    }

    @Test
    fun `axial compression produces constant negative axial force`() {
        val member = StructuralMember.createSimple(Length(120.0))
        val spanId = member.spans.first().id

        val axialLoad = Load.AxialLoad(
            value = 10000.0.poundsForce,
            spanId = spanId,
            direction = LoadDirection.AXIAL_COMPRESSION
        )

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(
                LoadCase(
                    "D",
                    "Dead",
                    listOf(axialLoad)
                )
            ),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4,
            crossSectionalArea = 10.0.in2
        )

        val result = BeamAnalysisSolver.solve(config)
        val spanResult = result.spanResults[0]

        // ------------------------------------------------------------
        // Axial force
        //
        // Compression is negative by the solver sign convention.
        // A pure axial load should produce constant axial force
        // throughout the span.
        // ------------------------------------------------------------

        assertEquals(
            "Maximum axial force should be -10,000 lb",
            -10000.0,
            spanResult.maxAxial.inPoundsForce,
            1.0
        )

        assertEquals(
            "Minimum axial force should be -10,000 lb",
            -10000.0,
            spanResult.minAxial.inPoundsForce,
            1.0
        )

        // ------------------------------------------------------------
        // Every station should carry the same axial force.
        // ------------------------------------------------------------

        spanResult.stationDemands.forEach { station ->
            assertEquals(
                "Axial force should remain constant at x=${station.x.inches}\"",
                -10000.0,
                station.axial.inPoundsForce,
                1.0
            )
        }

        // ------------------------------------------------------------
        // Pure axial loading must not create bending or shear.
        // ------------------------------------------------------------

        spanResult.stationDemands.forEach { station ->
            assertEquals(
                "Axial load should not create shear at x=${station.x.inches}\"",
                0.0,
                station.shear.inPoundsForce,
                1.0
            )

            assertEquals(
                "Axial load should not create moment at x=${station.x.inches}\"",
                0.0,
                station.moment.inLbIn,
                1.0
            )
        }
    }

    @Test
    fun `simply supported beam with midspan point load produces correct maximum deflection`() {
        val member = StructuralMember.createSimple(Length(120.0))
        val spanId = member.spans.first().id

        val pointLoad = Load.PointLoad(
            value = 6000.0.poundsForce,
            spanId = spanId,
            locationStart = Length(60.0),
            category = LoadCategory.DEAD,
            direction = LoadDirection.VERTICAL_DOWN
        )

        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(
                LoadCase(
                    "D",
                    "Dead",
                    listOf(pointLoad)
                )
            ),
            modulusOfElasticity = 29000000.0.psiModulus,
            momentOfInertiaX = 100.0.in4
        )

        val result = BeamAnalysisSolver.solve(config)
        val spanResult = result.spanResults[0]

        // ------------------------------------------------------------
        // Maximum deflection
        //
        // δmax = PL³ / (48EI)
        //
        // = 6000(120³) / (48 × 29,000,000 × 100)
        // = 0.0744828 in
        //
        // Downward deflection is negative under the solver convention.
        // ------------------------------------------------------------

        assertEquals(
            "Maximum deflection should be -0.07448 in",
            0.0744828,
            spanResult.maxDeflection.inches,
            0.001
        )

        // ------------------------------------------------------------
        // Governing deflection location
        // ------------------------------------------------------------

        assertEquals(
            "Maximum deflection should occur at midspan",
            60.0,
            spanResult.extremePoints.maxDeflectionX.inches,
            0.5
        )

        // ------------------------------------------------------------
        // Support deflections
        // ------------------------------------------------------------

        fun deflectionAt(x: Double): Double {
            return spanResult.deflectionDiagram
                .minByOrNull { kotlin.math.abs(it.x.inches - x) }!!
                .value
        }

        assertEquals(
            "Deflection at left support should be zero",
            0.0,
            deflectionAt(0.0),
            0.001
        )

        assertEquals(
            "Deflection at midspan should be -0.07448 in",
            -0.0744828,
            deflectionAt(60.0),
            0.001
        )

        assertEquals(
            "Deflection at right support should be zero",
            0.0,
            deflectionAt(120.0),
            0.001
        )
    }
}