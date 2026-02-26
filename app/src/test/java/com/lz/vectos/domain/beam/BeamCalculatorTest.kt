package com.lz.vectos.domain.beam

import com.lz.vectos.domain.calculation.CalculationMetadata
import com.lz.vectos.domain.project.Project
import com.lz.vectos.domain.units.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID
import java.time.LocalDateTime

class BeamCalculatorTest {

    private val now = LocalDateTime.now()
    private val projectId = UUID.randomUUID()
    
    private val metadata = CalculationMetadata(
        id = UUID.randomUUID(),
        name = "Test Calculation",
        createdAt = now
    )

    private val project = Project(
        id = projectId,
        name = "Test Project",
        description = "Testing Units System",
        clientName = "Test Client",
        engineerName = "Test Engineer",
        createdAt = now
    )

    private val delta = 1e-6

    @Test
    fun testMetricPointLoadMidspan() {
        val inputs = BeamInputs(
            project = project,
            span = 5.0.meters,
            loadValue = 1000.0, // Newtons (Base Unit)
            material = Material.STEEL,
            momentOfInertia = 0.0001.m4,
            loadType = LoadType.POINT_LOAD_MIDSPAN,
            unitSystem = UnitSystem.METRIC
        )

        val calculation = BeamCalculator.calculate(metadata, inputs)
        val results = calculation.results

        // M = PL/4 = 1000 * 5 / 4 = 1250 Nm
        assertEquals(1250.0, results.maxBendingMoment.inNewtonMeters, delta)
        
        // V = P/2 = 1000 / 2 = 500 N
        assertEquals(500.0, results.maxShear.inNewtons, delta)
        
        // Delta = PL^3 / (48EI) = 1000 * 5^3 / (48 * 200e9 * 0.0001) = 125000 / 960000000 = 0.00013020833 m
        assertEquals(0.00013020833, results.maxDeflection.inMeters, 1e-10)
    }

    @Test
    fun testMetricUDL() {
        val inputs = BeamInputs(
            project = project,
            span = 5.0.meters,
            loadValue = 200.0, // N/m (Base Unit)
            material = Material.STEEL,
            momentOfInertia = 0.0001.m4,
            loadType = LoadType.UNIFORMLY_DISTRIBUTED_LOAD,
            unitSystem = UnitSystem.METRIC
        )

        val calculation = BeamCalculator.calculate(metadata, inputs)
        val results = calculation.results

        // M = wL^2 / 8 = 200 * 5^2 / 8 = 625 Nm
        assertEquals(625.0, results.maxBendingMoment.inNewtonMeters, delta)
        
        // V = wL / 2 = 200 * 5 / 2 = 500 N
        assertEquals(500.0, results.maxShear.inNewtons, delta)
        
        // Delta = 5wL^4 / (384EI) = 5 * 200 * 5^4 / (384 * 200e9 * 0.0001) = 625000 / 7680000000 = 0.0000813802 m
        assertEquals(0.0000813802, results.maxDeflection.inMeters, 1e-10)
    }

    @Test
    fun testImperialPointLoadMidspan() {
        // Inputs in Imperial, but converted to Base Units for the Calculator
        val span = 10.0.feet
        val load = 1000.0.poundsForce
        val i = 10.0.in4

        val inputs = BeamInputs(
            project = project,
            span = span,
            loadValue = load.newtons, // Calculator expects numeric part of Base Unit
            material = Material.STEEL,
            momentOfInertia = i,
            loadType = LoadType.POINT_LOAD_MIDSPAN,
            unitSystem = UnitSystem.IMPERIAL
        )

        val calculation = BeamCalculator.calculate(metadata, inputs)
        val results = calculation.results

        // M = PL/4 = 1000 lbf * 10 ft / 4 = 2500 lb-ft
        // We verify by converting the result Moment back to lb-ft
        // 1 lb-ft = 1.3558179483314004 Nm
        val momentInLbFt = results.maxBendingMoment.inNewtonMeters / 1.3558179483314004
        assertEquals(2500.0, momentInLbFt, 0.01)
        
        // V = P/2 = 1000 lbf / 2 = 500 lbf
        assertEquals(500.0, results.maxShear.inPoundsForce, 0.01)
        
        // Delta = PL^3 / (48EI)
        // L = 120 in, P = 1000 lbf, E = 200 GPa
        // Expected Delta (in) = (1000 * 120^3) / (48 * (200 * 145037.738) * 10) = 0.124116
        val expectedDeltaInInches = 0.124116
        assertEquals(expectedDeltaInInches, results.maxDeflection.inInches, 0.001)
    }
}
