package com.lz.vectos.domain.structural.analysis

import com.lz.model.regulatory.LoadCategory
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.StructuralMember
import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.inInches
import com.lz.model.units.inPoundsForce
import com.lz.vectos.domain.structural.*
import com.lz.model.units.*
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class BeamAnalysisSolverVerificationTest {

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
                name = "1.2D + 1.6L",
                methodology = DesignMethodology.LRFD,
                equation = "1.2D + 1.6L",
                factors = mapOf(LoadCategory.DEAD to 1.2, LoadCategory.LIVE to 1.6),
                codeReference = "ASCE 7-16"
            )
        )
        
        val config = BeamAnalysisConfig(
            member = member,
            loadCases = listOf(
                LoadCase("D", "Dead", listOf(deadLoad)),
                LoadCase("L", "Live", listOf(liveLoad))
            ),
            combinations = combinations,
            modulusOfElasticityPsi = 29000000.0,
            momentOfInertiaIn4 = 100.0
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
        
        assertNotNull("Peak at 40 (before) missing", peakAt40Before)
        assertNotNull("Peak at 40 (after) missing", peakAt40After)
        assertNotNull("Peak at 80 (before) missing", peakAt80Before)
        assertNotNull("Peak at 80 (after) missing", peakAt80After)
        
        val delta40 = peakAt40Before!!.value - peakAt40After!!.value
        val delta80 = peakAt80Before!!.value - peakAt80After!!.value
        
        assertEquals(1200.0, delta40, 1.0)
        assertEquals(3200.0, delta80, 1.0)
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
            modulusOfElasticityPsi = 29000000.0,
            momentOfInertiaIn4 = 100.0,
            areaIn2 = 10.0
        )
        
        val result = BeamAnalysisSolver.solve(config)
        val spanResult = result.spanResults[0]

        println("Axial station demands:")
        result.spanResults[0].stationDemands.forEach { println("x=${it.x.inches} axial=${it.axial.inPoundsForce}") }
        
        // Internal axial force should be -1000
        assertTrue("Axial compression should be negative: ${spanResult.maxAxial.inPoundsForce}", 
            spanResult.stationDemands.all { it.axial.inPoundsForce <= 0.0 })
        assertEquals(-1000.0, spanResult.minAxial.inPoundsForce, 0.1)
    }
}
