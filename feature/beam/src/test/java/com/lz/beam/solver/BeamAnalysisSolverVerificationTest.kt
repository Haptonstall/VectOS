package com.lz.beam.solver

import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.loads.CombinationType
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.Load
import com.lz.model.structural.LoadCase
import com.lz.model.structural.LoadDirection
import com.lz.model.structural.StructuralMember
import com.lz.model.units.*
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

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

        val moments = spanResult.momentDiagram

        fun momentAt(x: Double): Double {
            return moments
                .minByOrNull { kotlin.math.abs(it.x.inches - x) }!!
                .value
        }

        // Left side of the point load:
        //
        // M = R_A * x
        //
        // Therefore the slope is +666.667 lb.

        val m20 = momentAt(20.0)
        val m30 = momentAt(30.0)

        val leftSlope = (m30 - m20) / 10.0

        // Right side:
        //
        // M = R_A*x - P(x-a)
        //
        // slope = R_A - P
        //       = 666.667 - 1000
        //       = -333.333 lb.

        val m50 = momentAt(50.0)
        val m60 = momentAt(60.0)

        val rightSlope = (m60 - m50) / 10.0

        assertEquals(
            "Moment slope to left of point load should equal +reaction",
            666.6667,
            leftSlope,
            1.0
        )

        assertEquals(
            "Moment slope to right of point load should equal reaction minus load",
            -333.3333,
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

        val spanResult = result.spanResults[0]

        assertEquals(
            "Left reaction should be 666.667 lb",
            666.6667,
            spanResult.reactionStart.inPoundsForce,
            0.1
        )

        assertEquals(
            "Right reaction should be 333.333 lb",
            333.3333,
            spanResult.reactionEnd.inPoundsForce,
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
        val spanResult = result.spanResults[0]

        val totalReaction =
            spanResult.reactionStart.inPoundsForce +
                    spanResult.reactionEnd.inPoundsForce

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
        val spanResult = result.spanResults[0]

        val rightReactionMoment =
            spanResult.reactionEnd.inPoundsForce * 120.0

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
            spanResult.maxMoment.location.inches,
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
        val peakAt40Before = shearPoints.find { Math.abs(it.x.inInches - (40.0 - 1e-5)) < 1e-6 }
        val peakAt40After = shearPoints.find { Math.abs(it.x.inInches - (40.0 + 1e-5)) < 1e-6 }
        val peakAt80Before = shearPoints.find { Math.abs(it.x.inInches - (80.0 - 1e-5)) < 1e-6 }
        val peakAt80After = shearPoints.find { Math.abs(it.x.inInches - (80.0 + 1e-5)) < 1e-6 }

        Assert.assertNotNull("Peak at 40 (before) missing", peakAt40Before)
        Assert.assertNotNull("Peak at 40 (after) missing", peakAt40After)
        Assert.assertNotNull("Peak at 80 (before) missing", peakAt80Before)
        Assert.assertNotNull("Peak at 80 (after) missing", peakAt80After)
        
        val delta40 = peakAt40Before!!.value - peakAt40After!!.value
        val delta80 = peakAt80Before!!.value - peakAt80After!!.value

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
}